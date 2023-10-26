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

package org.wso2.carbon.identity.user.registration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.config.RegistrationSequence;
import org.wso2.carbon.identity.user.registration.config.RegistrationStep;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.NOT_STARTED;

public class RegistrationFrameworkUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFrameworkUtils.class);

    public static void addRegContextToCache(RegistrationContext context) {

        RegistrationContextCacheEntry cacheEntry = new RegistrationContextCacheEntry(context);
        RegistrationContextCacheKey cacheKey = new RegistrationContextCacheKey(context.getContextIdentifier());
        RegistrationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    public static RegistrationContext retrieveRegContextFromCache(String contextId) throws RegistrationFrameworkException {

        RegistrationContextCacheEntry entry =
                RegistrationContextCache.getInstance().getValueFromCache(new RegistrationContextCacheKey(contextId));
        if (entry == null) {
            throw new RegistrationFrameworkException("Invalid flow id: " + contextId);
        }
        return entry.getContext();
    }

    public static void removeRegContextFromCache(String contextId) {

        RegistrationContextCache.getInstance().clearCacheEntry(new RegistrationContextCacheKey(contextId));
    }

    public static String createUser(RegistrationRequestedUser user) throws RegistrationFrameworkException {

        UserStoreManager userStoreManager = getUserstoreManager();
        Map<String, String> claims = new HashMap<>();

        claims.put("http://wso2.org/claims/username", user.getUsername());

        claims.putAll(user.getClaims());

        String password = "randomPassword";
        if (!user.isPasswordless()) {
            password = user.getCredential();
        }
        try {
            userStoreManager
                    .addUser(IdentityUtil.addDomainToName(user.getUsername(), "PRIMARY"), password, null, claims, null);
            return ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while creating user", e);
        }
    }

    private static UserStoreManager getUserstoreManager() throws RegistrationFrameworkException {

        RealmService realmService = UserRegistrationServiceDataHolder.getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId("carbon.super");
        try {
            return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while retrieving user store manager", e);
        }
    }

    public static RegistrationContext initiateRegContext(String appId,
                                    RegistrationFlowConstants.SupportedProtocol type) throws RegistrationFrameworkException {

        RegistrationContext context = new RegistrationContext();
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setRequestType(type.toString());
        context.setCompleted(false);
        context.setCurrentStep(0);

        RegistrationRequestedUser user = new RegistrationRequestedUser();
        context.setRegisteringUser(user);
        if ("app1".equals(appId)) {
            context.setRegistrationSequence(buildFlow1(appId));
        } else if ("app2".equals(appId)) {
            context.setRegistrationSequence(buildFlow2(appId));
        } else if ("app3".equals(appId)) {
            context.setRegistrationSequence(buildFlow3(appId));
        } else {
            context.setRegistrationSequence(buildSeqFromSp(appId));
        }
        return context;

    }

    private static RegistrationSequence buildFlow1(String appId) throws RegistrationFrameworkException {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(buildAttributeCollector1());
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(false);
        step2.setSelectedExecutor(null);
        step2.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors2 = new ArrayList<>();
        executors2.add(buildPasswordOnboarder());
        step2.setConfiguredExecutors(executors2);

        RegistrationStep step3 = new RegistrationStep();
        step3.setOrder(2);
        step3.setMultiOption(false);
        step3.setSelectedExecutor(null);
        step3.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors3 = new ArrayList<>();
        executors3.add(buildEmailOTPVerifier());
        step3.setConfiguredExecutors(executors3);

        Map<Integer, RegistrationStep> stepMap =  new HashMap<>();
        stepMap.put(1, step1);
        stepMap.put(2, step2);
        stepMap.put(3, step3);
        sequence.setStepMap(stepMap);

        return sequence;
    }

    private static RegistrationSequence buildFlow2(String appId) throws RegistrationFrameworkException {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(buildAttributeCollector1());
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(true);
        step2.setSelectedExecutor(null);
        step2.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors2 = new ArrayList<>();
        executors2.add(buildPasswordOnboarder());
        executors2.add(buildEmailOTPVerifier());
        step2.setConfiguredExecutors(executors2);

        RegistrationStep step3 = new RegistrationStep();
        step3.setOrder(2);
        step3.setMultiOption(false);
        step3.setSelectedExecutor(null);
        step3.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors3 = new ArrayList<>();
        executors3.add(buildAttributeCollector2());
        step3.setConfiguredExecutors(executors3);

        Map<Integer, RegistrationStep> stepMap =  new HashMap<>();
        stepMap.put(1, step1);
        stepMap.put(2, step2);
        stepMap.put(3, step3);
        sequence.setStepMap(stepMap);

        return sequence;
    }

    private static RegistrationSequence buildFlow3(String appId) throws  RegistrationFrameworkException {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(buildAttributeCollector1());
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(true);
        step2.setSelectedExecutor(null);
        step2.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors2 = new ArrayList<>();
        executors2.add(buildPasswordOnboarder());
        executors2.add(buildEmailOTPVerifier());
        step2.setConfiguredExecutors(executors2);

        RegistrationStep step3 = new RegistrationStep();
        step3.setOrder(2);
        step3.setMultiOption(false);
        step3.setSelectedExecutor(null);
        step3.setStatus(NOT_STARTED);

        List<RegistrationStepExecutorConfig> executors3 = new ArrayList<>();
        executors3.add(buildEmailOTPVerifier());
        step3.setConfiguredExecutors(executors3);

        Map<Integer, RegistrationStep> stepMap =  new HashMap<>();
        stepMap.put(1, step1);
        stepMap.put(2, step2);
        stepMap.put(3, step3);
        sequence.setStepMap(stepMap);

        return sequence;
    }

    private static RegistrationSequence buildSeqFromSp(String appId) throws RegistrationFrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ServiceProvider sp;
        try {
            sp = appInfo.getApplicationByResourceId(appId, "carbon.super" );
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error occurred while retrieving service provider", e);
        }
        if (sp == null) {
            throw new RegistrationFrameworkException("Service provider not found for app id: " + appId);
        }
        AuthenticationStep[] authenticationSteps =
                sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
        return getSequenceFromServiceProvider(sp, authenticationSteps);
    }

    private static RegistrationStepExecutorConfig buildAttributeCollector1() {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("AttributeCollector1");
        config.setId("uuid_attr_1");

        ClaimMapping[] claimMappings = new ClaimMapping[4];

        ClaimMapping claimMapping1 = ClaimMapping.build("http://wso2.org/claims/username",
                "http://wso2.org/claims/username", null, true, true);
        ClaimMapping claimMapping2 = ClaimMapping.build("http://wso2.org/claims/firstname",
                "http://wso2.org/claims/givenname", null, true, false);
        ClaimMapping claimMapping3 = ClaimMapping.build("http://wso2.org/claims/lastname",
                "http://wso2.org/claims/lastname", null, true, false);
        ClaimMapping claimMapping4 = ClaimMapping.build("http://wso2.org/claims/emailaddress",
                "http://wso2.org/claims/emailaddress", null, true, true);
        claimMappings[0] = claimMapping1;
        claimMappings[1] = claimMapping2;
        claimMappings[2] = claimMapping3;
        claimMappings[3] = claimMapping4;

        config.setRequestedClaims(claimMappings);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));

        return config;
    }

    private static RegistrationStepExecutorConfig buildAttributeCollector2() {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("AttributeCollector2");
        config.setId("uuid_attr_2");

        ClaimMapping[] claimMappings = new ClaimMapping[2];

        ClaimMapping claimMapping1 = ClaimMapping.build("http://wso2.org/claims/country",
                "http://wso2.org/claims/country", null, true, true);
        ClaimMapping claimMapping2 = ClaimMapping.build("http://wso2.org/claims/dob",
                "http://wso2.org/claims/dob", null, true, true);

        claimMappings[0] = claimMapping1;
        claimMappings[1] = claimMapping2;

        config.setRequestedClaims(claimMappings);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));
        return config;
    }

    private static RegistrationStepExecutorConfig buildPasswordOnboarder() {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("DefaultPasswordCollector");
        config.setId("uuid_pwd_1");
        config.setExecutor(getRegStepExecutor("PasswordOnboarding"));
        return config;
    }

    private static RegistrationStepExecutorConfig buildEmailOTPVerifier() throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("DefaultEmailOTPVerifier");
        config.setId("uuid_email_otp_1");
        config.setExecutor(getRegStepExecutor("EmailOTPVerifier"));
        config.setIdentityProvider(getIdentityProvider("emailOTP"));
        Map<String, String> properties = getAuthenticatorPropertyMapFromIdP(config.getIdentityProvider(), "EmailOTP");

        if (config.getProperties() != null) {
            config.getProperties().putAll(properties);
        } else {
            config.setProperties(properties);
        }

        return config;
    }

    private static IdentityProvider getIdentityProvider(String idpName) throws RegistrationFrameworkException {

        IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
        try {
            return idpdao.getIdentityProvider(idpName);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Cannot get the IdP: " + idpName, e);
        }
    }

    public static Map<String, String> getAuthenticatorPropertyMapFromIdP(IdentityProvider idp, String name) {

        Map<String, String> propertyMap = new HashMap<String, String>();

        if (idp != null) {
            FederatedAuthenticatorConfig[] authenticatorConfigs = idp.getFederatedAuthenticatorConfigs();

            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (authenticatorConfig.getName().equals(name)) {

                    for (Property property : authenticatorConfig.getProperties()) {
                        propertyMap.put(property.getName(), property.getValue());
                    }
                    break;
                }
            }
        }

        return propertyMap;
    }

    private static RegistrationStepExecutor getRegStepExecutor(String name) {

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (name.equals(executor.getName())) {
                return executor;
            }
        }
        return null;
    }

    private static RegistrationSequence getSequenceFromServiceProvider(ServiceProvider serviceProvider,
                                                   AuthenticationStep[] authenticationSteps) throws RegistrationFrameworkException {

        if (serviceProvider == null) {
            throw new RegistrationFrameworkException("ServiceProvider cannot be null");
        }
        RegistrationSequence sequenceConfig = new RegistrationSequence();
        sequenceConfig.setApplicationId(serviceProvider.getApplicationName());

        int stepOrder = 0;

        // No need to specifically have a step for attribute collection.

//        RegistrationStep step1 = new RegistrationStep();
//        step1.setOrder(++stepOrder);
//        step1.setMultiOption(false);
//        step1.setSelectedExecutor(null);
//        step1.setStatus(NOT_STARTED);
//        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
//        executors.add(buildAttributeCollector1());
//        step1.setConfiguredExecutors(executors);
//        sequenceConfig.getStepMap().put(step1.getOrder(), step1);

        if (authenticationSteps == null) {
            return sequenceConfig;
        }

        // for each configured step. We are considering only the first step at the moment.
//        for (AuthenticationStep authenticationStep : authenticationSteps) {
//
//            // loading local authenticators
//            RegistrationStep stepConfig = loadExecutors(authenticationStep);
//
//            if (stepConfig != null) {
//                stepConfig.setStatus(NOT_STARTED);
//                stepConfig.setOrder(++stepOrder);
//                sequenceConfig.getStepMap().put(stepConfig.getOrder(), stepConfig);
//            }
//        }
        AuthenticationStep firstStep = authenticationSteps[0];

        // Load registration executors based on the authenticators.
        RegistrationStep stepConfig = loadExecutors(firstStep);

        if (stepConfig != null) {
            stepConfig.setStatus(NOT_STARTED);
            stepConfig.setOrder(++stepOrder);
            sequenceConfig.getStepMap().put(stepConfig.getOrder(), stepConfig);
        }
        RegistrationStep attributeCollectStep = generateAttributeCollectionStep(serviceProvider);

        if (attributeCollectStep != null) {
            attributeCollectStep.setStatus(NOT_STARTED);
            attributeCollectStep.setOrder(++stepOrder);
            sequenceConfig.getStepMap().put(attributeCollectStep.getOrder(), attributeCollectStep);
        }
        return sequenceConfig;
    }

    public static RegistrationStep loadExecutors(AuthenticationStep authenticationStep)
            throws RegistrationFrameworkException {

        List<RegistrationStepExecutorConfig> executorConfigs = new ArrayList<>();

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            // assign it to the step
            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        localAuthenticator.getName(), localAuthenticator.getDisplayName());
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();
        if (federatedIDPs != null) {
            // for each idp in the step
            for (IdentityProvider federatedIDP : federatedIDPs) {
                FederatedAuthenticatorConfig federatedAuthenticator = federatedIDP.getDefaultAuthenticatorConfig();

                if (federatedAuthenticator == null) {
                    try {
                        federatedAuthenticator = IdentityProviderManager.getInstance()
                                .getIdPByName(federatedIDP.getIdentityProviderName(), "carbon.super")
                                .getDefaultAuthenticatorConfig();
                    } catch (IdentityProviderManagementException e) {
                        throw new RegistrationFrameworkException("Failed to load the default authenticator for IDP : "
                                + federatedIDP.getIdentityProviderName(), e);
                    }
                }
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        federatedAuthenticator.getName(), federatedAuthenticator.getDisplayName()
                );
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        if (executorConfigs.size() == 0) {
            LOG.info("No supported executors in the step");
            return null;
        }
        RegistrationStep stepConfig = new RegistrationStep();
        stepConfig.setConfiguredExecutors(executorConfigs);
        if (executorConfigs.size() > 1) {
            stepConfig.setMultiOption(true);
        }
        return stepConfig;
    }

    private static RegistrationStepExecutorConfig getMappedRegExecutorConfig(String idpName, String givenName)
            throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig regStepConfig = new RegistrationStepExecutorConfig();
        RegistrationStepExecutor mappedRegExecutor = null;

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (RegistrationFlowConstants.RegistrationExecutorBindingType.AUTHENTICATOR.equals(executor.getBindingType())
                    && executor.getBoundIdentifier().equals(idpName)) {
                mappedRegExecutor = executor;
                break;
            }
        }

        if (mappedRegExecutor == null) {
            return null;
        }
        regStepConfig.setName(givenName);
        regStepConfig.setId(idpName);
        regStepConfig.setExecutor(mappedRegExecutor);
        regStepConfig.setIdentityProvider(getIdentityProvider(idpName));

        return regStepConfig;
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

    public static RegistrationStep generateAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }
        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("AttributeCollector");
        config.setId("AttributeCollectorBasedOnAppClaims");
        config.setRequestedClaims(requestedClaims);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(config);

        RegistrationStep step = new RegistrationStep();
        step.setMultiOption(false);
        step.setSelectedExecutor(null);
        step.setConfiguredExecutors(executors);

        return step;
    }
}
