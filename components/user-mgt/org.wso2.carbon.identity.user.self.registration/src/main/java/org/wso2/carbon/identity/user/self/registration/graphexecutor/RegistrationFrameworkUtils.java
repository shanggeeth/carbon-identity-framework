/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.self.registration.graphexecutor;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.AutoLoginAssertionUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.DefaultRegistrationSequenceHandler;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.RegistrationSequenceHandler;
import org.wso2.carbon.identity.user.self.registration.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.self.registration.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.self.registration.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config.AuthSequenceBasedConfigLoader;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config.RegistrationSequence;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.RequiredParam;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.ErrorMessages.ERROR_FLOW_ID_NOT_FOUND;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.ErrorMessages.ERROR_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.INITIAL_AUTH_REQUEST;

public class RegistrationFrameworkUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFrameworkUtils.class);

    public static void addRegContextToCache(RegistrationContext context) {

        RegistrationContextCacheEntry cacheEntry = new RegistrationContextCacheEntry(context);
        RegistrationContextCacheKey cacheKey = new RegistrationContextCacheKey(context.getContextIdentifier());
        RegistrationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    public static RegistrationContext retrieveRegContextFromCache(String contextId) throws RegistrationFrameworkException {

        if (contextId == null) {
            throw new RegistrationClientException(ERROR_FLOW_ID_NOT_FOUND.getCode(),
                                                  ERROR_FLOW_ID_NOT_FOUND.getMessage(),
                                                  ERROR_FLOW_ID_NOT_FOUND.getDescription());
        }
        RegistrationContextCacheEntry entry =
                RegistrationContextCache.getInstance().getValueFromCache(new RegistrationContextCacheKey(contextId));
        if (entry == null) {
            throw new RegistrationClientException(ERROR_INVALID_FLOW_ID.getCode(),
                                                  ERROR_INVALID_FLOW_ID.getMessage(),
                                                  String.format(ERROR_INVALID_FLOW_ID.getDescription(), contextId));
        }
        return entry.getContext();
    }

    public static void removeRegContextFromCache(String contextId) {

        RegistrationContextCache.getInstance().clearCacheEntry(new RegistrationContextCacheKey(contextId));
    }

    public static String createUser(RegistrationRequestedUser user, String tenantDomain) throws RegistrationFrameworkException {

        UserStoreManager userStoreManager = getUserstoreManager(tenantDomain);

        Map<String, String> claims = new HashMap<>();
        claims.putAll(user.getClaims());

        String password;
        if (!user.isPasswordless()) {
            password = user.getCredential();
        } else {
            password = String.valueOf(new DefaultPasswordGenerator().generatePassword());
        }
        try {
            userStoreManager
                    .addUser(IdentityUtil.addDomainToName(user.getUsername(), "PRIMARY"), password, null, claims, null);
            return ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while creating user", e);
        }
    }

    private static UserStoreManager getUserstoreManager(String tenantDomain) throws RegistrationFrameworkException {

        RealmService realmService = UserRegistrationServiceDataHolder.getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while retrieving user store manager", e);
        }
    }

    public static RegistrationContext initiateRegContext(String appId, String tenantDomain,
                                    RegistrationConstants.SupportedProtocol type) throws RegistrationFrameworkException {

        RegistrationContext context = new RegistrationContext();
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setTenantDomain(tenantDomain);
        context.setRequestType(type.toString());

        ServiceProvider sp = retrieveSpFromAppId(appId, tenantDomain);

        if (!sp.isAuthSequenceBasedSignupEnabled()) {
            throw new RegistrationFrameworkException("Auth sequence based signup is not enabled for the application: " + appId);
        }
        updateContext(context, sp);

        return context;

    }

    public static RegistrationContext initiateRegContext(HttpServletRequest request) throws RegistrationFrameworkException {

        RegistrationContext context = new RegistrationContext();
        context.setContextIdentifier(request.getParameter("sessionDataKey"));
        context.setTenantDomain(getTenantDomain(request));
        context.setRequestType(request.getParameter("type"));
        context.setProperty(INITIAL_AUTH_REQUEST, request);

        ServiceProvider sp = retrieveSpFromClientId(request.getParameter("clientId"), context.getTenantDomain());
        updateContext(context, sp);

        return context;
    }

    private static void updateContext(RegistrationContext context, ServiceProvider sp) throws RegistrationFrameworkException {

        RegistrationRequestedUser user = new RegistrationRequestedUser();
        context.setRegisteringUser(user);
        context.setCompleted(false);
        context.setCurrentStep(0);

        RegistrationSequence sequence =
                AuthSequenceBasedConfigLoader.getInstance().loadRegistrationSequence(sp);

        context.setServiceProvider(sp);
        context.setRegistrationSequence(sequence);
    }

    private static String getTenantDomain(HttpServletRequest request) throws RegistrationFrameworkException {

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            return IdentityTenantUtil.getTenantDomainFromContext();
        }
        String tenantDomain = request.getParameter(FrameworkConstants.RequestParams.TENANT_DOMAIN);

        if (tenantDomain == null || tenantDomain.isEmpty() || "null".equals(tenantDomain)) {

            String tenantId = request.getParameter(FrameworkConstants.RequestParams.TENANT_ID);

            if (tenantId != null && !"-1234".equals(tenantId)) {
                try {
                    Tenant tenant = UserRegistrationServiceDataHolder.getRealmService().getTenantManager()
                            .getTenant(Integer.parseInt(tenantId));
                    if (tenant != null) {
                        tenantDomain = tenant.getDomain();
                    }
                } catch (Exception e) {
                    throw new RegistrationFrameworkException("Cannot retrieve tenant domain.");
                }
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
        }
        return tenantDomain;
    }

    private static ServiceProvider retrieveSpFromClientId(String clientId, String tenantDomain)
            throws RegistrationFrameworkException {

        ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();

        try {
            return appMgtService.getServiceProviderByClientId(clientId, FrameworkConstants.OAUTH2,
                    tenantDomain);
        } catch (IdentityApplicationManagementClientException e) {
            throw new RegistrationFrameworkException("Unable to find application", e);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Unable to retrieve application", e);
        }
    }

    public static ServiceProvider retrieveSpFromAppId(String appId, String tenantDomain) throws  RegistrationFrameworkException{

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ServiceProvider sp;
        try {
            sp = appInfo.getApplicationByResourceId(appId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error occurred while retrieving service provider", e);
        }
        if (sp == null) {
            throw new RegistrationFrameworkException("Service provider not found for app id: " + appId);
        }

        return sp;
    }

    public static void updateAvailableValuesForRequiredParams(RegistrationContext context, List<RequiredParam> params) {

        Map<String, String> userData = context.getRegisteringUser().getClaims();
        if (userData != null && params != null && params.size() > 0) {
            for (RequiredParam param : params) {
                if (userData.get(param.getName()) != null  ) {
                    param.setAvailableValue(userData.get(param.getName()));
                }
            }
        }
    }

    public static RegistrationSequenceHandler getRegistrationSeqHandler(RegistrationSequence sequence) {

        if (sequence.getFlowDefinition() == null
                || RegistrationConstants.DEFAULT_FLOW_DEFINITION.equals(sequence.getFlowDefinition())) {
            return DefaultRegistrationSequenceHandler.getInstance();
        }
        return null;
    }

    public static Map<String, String> convertClaimsFromIdpToLocalClaims(String tenantDomain,
                                                                        Map<String, String> remoteClaims,
                                                                        ClaimMapping[] idPClaimMappings,
                                                                        String idPStandardDialect)
    throws RegistrationFrameworkException {

        Map<String, String> localToIdPClaimMap;

        if (idPStandardDialect == null) {
            idPStandardDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        }

        try {
            localToIdPClaimMap = getClaimMappings(idPStandardDialect, remoteClaims.keySet(), tenantDomain, true);
        } catch (Exception e) {
            throw new RegistrationFrameworkException("Error occurred while getting claim mappings for", e);
        }
        // Adding remote claims with default values also to the key set because they may not come from the federated IdP
        localToIdPClaimMap.putAll(Arrays.stream(idPClaimMappings)
                .filter(claimMapping -> StringUtils.isNotBlank(claimMapping.getDefaultValue())
                        && !localToIdPClaimMap.containsKey(claimMapping.getLocalClaim().getClaimUri()))
                .collect(Collectors.toMap(claimMapping -> claimMapping.getLocalClaim().getClaimUri(), ClaimMapping::getDefaultValue)));

         Map<String, String> mappedLocalClaimsForIdPClaims = new HashMap<>();

            for (Map.Entry<String, String> entry : localToIdPClaimMap.entrySet()) {
                String localClaimURI = entry.getKey();
                String claimValue = remoteClaims.get(localToIdPClaimMap.get(localClaimURI));
                if (StringUtils.isEmpty(claimValue)) {
                    LOG.debug("Claim " + localClaimURI + " has null value or blank hence not updating.");
                } else {
                    mappedLocalClaimsForIdPClaims.put(localClaimURI, claimValue);
                }
            }

        return mappedLocalClaimsForIdPClaims;
    }

    // Copied from DefaultClaimHandler
    private static Map<String, String> getClaimMappings(String otherDialect, Set<String> keySet,
                                                 String tenantDomain, boolean useLocalDialectAsKey)
            throws FrameworkException {

        Map<String, String> claimMapping = null;
        try {
            claimMapping = ClaimMetadataHandler.getInstance()
                    .getMappingsMapFromOtherDialectToCarbon(otherDialect, keySet, tenantDomain,
                            useLocalDialectAsKey);
        } catch (ClaimMetadataException e) {
            throw new FrameworkException("Error while loading mappings.", e);
        }

        if (claimMapping == null) {
            claimMapping = new HashMap<>();
        }

        return claimMapping;
    }

    public static Optional<String> getSignedUserAssertion(String userId, RegistrationContext context) {

        JWTClaimsSet userAssertion = buildUserAssertionClaimSet(userId, context);
        try {
            return Optional.ofNullable(AutoLoginAssertionUtils.generateSignedUserAssertion(userAssertion,
                    context.getTenantDomain()));
        } catch (FrameworkException e) {
            LOG.error("Error while generating signed user assertion", e);
            return Optional.empty();
        }
    }

    private static JWTClaimsSet buildUserAssertionClaimSet(String userId, RegistrationContext context) {

        long curTimeInMillis = Calendar.getInstance().getTimeInMillis();
        String[] engagedAuthenticators = context.getAuthenticatedMethods().toArray(new String[0]);

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.issuer("wso2");
        jwtClaimsSetBuilder.subject(context.getRegisteringUser().getUsername());
        jwtClaimsSetBuilder.issueTime(new Date(curTimeInMillis));
        jwtClaimsSetBuilder.jwtID(UUID.randomUUID().toString());
        jwtClaimsSetBuilder.notBeforeTime(new Date(curTimeInMillis));
        jwtClaimsSetBuilder.expirationTime(calculateUserAssertionExpiryTime(curTimeInMillis));
        jwtClaimsSetBuilder.claim("amr", engagedAuthenticators );
        jwtClaimsSetBuilder.claim("userId", userId);

        return jwtClaimsSetBuilder.build();
    }

    private static Date calculateUserAssertionExpiryTime(long curTimeInMillis) {

        Date expirationTime;
        // Default value of 5min
        long accessTokenLifeTimeInMillis = 5 * 60 * 1000;
        // When accessTokenLifeTimeInMillis is equal to Long.MAX_VALUE the curTimeInMillis +
        // accessTokenLifeTimeInMillis can be a negative value
        if (curTimeInMillis + accessTokenLifeTimeInMillis < curTimeInMillis) {
            expirationTime = new Date(Long.MAX_VALUE);
        } else {
            expirationTime = new Date(curTimeInMillis + accessTokenLifeTimeInMillis);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("User assertion expiry time : " + expirationTime + "ms.");
        }
        return expirationTime;
    }
}
