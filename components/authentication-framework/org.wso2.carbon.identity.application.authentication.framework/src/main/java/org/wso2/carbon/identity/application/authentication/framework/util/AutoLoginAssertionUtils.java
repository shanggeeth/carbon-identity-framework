/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for generating and verifying the auto login assertion for self registered users.
 */
public class AutoLoginAssertionUtils {

    private static final Log LOG = LogFactory.getLog(AutoLoginAssertionUtils.class);
    private static final Map<Integer, Key> PRIVATE_KEYS = new ConcurrentHashMap<Integer, Key>();
    private static final Map<Integer, Certificate> PUBLIC_CERTS = new ConcurrentHashMap<Integer, Certificate>();

    public static String generateSignedUserAssertion(JWTClaimsSet jwtClaimsSet, String tenantDomain)
            throws FrameworkException {

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            Key privateKey = getPrivateKey(tenantDomain, tenantId);

            JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
            JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).build();

            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new FrameworkException("Error occurred while signing JWT", e);
        }
    }

    public static Optional<JWTClaimsSet> retrieveClaimsFromAutoLoginUserAssertion(String token, String tenantDomain)
            throws FrameworkException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        X509Certificate x509Certificate = (X509Certificate) getCertificate(tenantDomain, tenantId);
        PublicKey publicKey = x509Certificate.getPublicKey();

        SignedJWT signedJWT;
        JWSVerifier verifier;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new FrameworkException("Error while verifying the user assertion.");
        }
        if (publicKey instanceof RSAPublicKey) {
            verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        } else {
            throw new FrameworkException("Public key is not an RSA public key.");
        }

        try {
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            if (signedJWT.verify(verifier) && !claimsSet.getExpirationTime().before(new Date())) {
                return Optional.of(claimsSet);
            }
        } catch (ParseException | JOSEException ex) {
            throw new FrameworkException("Error while validating the user assertion.");
        }
        return Optional.empty();
    }

    private static Key getPrivateKey(String tenantDomain, int tenantId) throws FrameworkException {

        Key privateKey;
        if (!(PRIVATE_KEYS.containsKey(tenantId))) {

            try {
                IdentityTenantUtil.initializeRegistry(tenantId);
            } catch (IdentityException e) {
                throw new FrameworkException("Error occurred while loading registry for tenant " + tenantId, e);
            }

            // get tenant's key store manager
            KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                // derive key store name
                String ksName = tenantDomain.trim().replace(".", "-");
                String jksName = ksName + ".jks";
                // obtain private key
                privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
            } else {
                try {
                    privateKey = tenantKSM.getDefaultPrivateKey();
                } catch (Exception e) {
                    throw new FrameworkException("Error while obtaining private key for super tenant", e);
                }
            }
            //privateKey will not be null always
            PRIVATE_KEYS.put(tenantId, privateKey);
        } else {
            //privateKey will not be null because containsKey() true says given key is exist and ConcurrentHashMap
            // does not allow to store null values
            privateKey = PRIVATE_KEYS.get(tenantId);
        }
        return privateKey;
    }

    private static Certificate getCertificate(String tenantDomain, int tenantId) throws FrameworkException {

        Certificate publicCert = null;

        if (!(PUBLIC_CERTS.containsKey(tenantId))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Obtaining certificate for the tenant %s", tenantDomain));
            }
            try {
                IdentityTenantUtil.initializeRegistry(tenantId);
            } catch (IdentityException e) {
                throw new FrameworkException("Error occurred while loading registry for tenant " + tenantDomain, e);
            }

            // get tenant's key store manager
            KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

            KeyStore keyStore = null;
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                // derive key store name
                String ksName = tenantDomain.trim().replace(".", "-");
                String jksName = ksName + ".jks";
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Loading default tenant certificate for tenant : %s from the KeyStore" +
                            " %s", tenantDomain, ksName));
                }
                try {
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                } catch (KeyStoreException e) {
                    throw new FrameworkException("Error occurred while loading public certificate for tenant: " +
                            tenantDomain, e);
                } catch (Exception e) {
                    throw new FrameworkException("Error occurred while loading Keystore for tenant: " +
                            tenantDomain, e);
                }
            } else {
                try {
                    publicCert = tenantKSM.getDefaultPrimaryCertificate();
                } catch (Exception e) {
                    throw new FrameworkException("Error occurred while loading default public " +
                            "certificate for tenant: " + tenantDomain, e);
                }
            }
            if (publicCert != null) {
                PUBLIC_CERTS.put(tenantId, publicCert);
            }
        } else {
            publicCert = PUBLIC_CERTS.get(tenantId);
        }
        return publicCert;
    }
}
