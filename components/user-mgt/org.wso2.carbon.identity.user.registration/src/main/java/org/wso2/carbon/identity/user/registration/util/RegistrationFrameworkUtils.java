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

import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.config.RegistrationSequence;
import org.wso2.carbon.identity.user.registration.config.RegistrationStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegistrationFrameworkUtils {

    private static int getServiceProviderId(String reqType, String clientId, String tenantDomain) throws RegistrationFrameworkException {

        if ("oidc".equals(reqType)) {
            reqType = "oauth2";
        }

        ApplicationManagementService appMgtService = UserRegistrationServiceDataHolder.getApplicationManagementService();
        try {
            ServiceProvider serviceProvider = appMgtService.getServiceProviderByClientId(clientId, reqType,
                    tenantDomain);
            return serviceProvider.getApplicationID();
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error while retrieving service provider for client id: " +
                    clientId + " and request type: " + reqType, e);
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
        if ("app2".equals(appId)) {
            context.setRegistrationSequence(buildFlow2(appId));

        } else {
            context.setRegistrationSequence(buildFlow1(appId));
        }
        return context;

    }

    public static RegistrationSequence buildFlow1(String appId) throws RegistrationFrameworkException {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(buildAttributeCollector1());
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(false);
        step2.setSelectedExecutor(null);
        step2.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

        List<RegistrationStepExecutorConfig> executors2 = new ArrayList<>();
        executors2.add(buildPasswordOnboarder());
        step2.setConfiguredExecutors(executors2);

        RegistrationStep step3 = new RegistrationStep();
        step3.setOrder(2);
        step3.setMultiOption(false);
        step3.setSelectedExecutor(null);
        step3.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

        List<RegistrationStepExecutorConfig> executors3 = new ArrayList<>();
        executors3.add(buildEmailOTPVerifier());
        step3.setConfiguredExecutors(executors3);

        // Todo: Add step 3 when email otp is done.

        Map<Integer, RegistrationStep> stepMap =  new HashMap<>();
        stepMap.put(1, step1);
        stepMap.put(2, step2);
        sequence.setStepMap(stepMap);

        return sequence;
    }

    public static RegistrationSequence buildFlow2(String appId) throws RegistrationFrameworkException {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(buildAttributeCollector1());
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(true);
        step2.setSelectedExecutor(null);
        step2.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

        List<RegistrationStepExecutorConfig> executors2 = new ArrayList<>();
        executors2.add(buildPasswordOnboarder());
        executors2.add(buildEmailOTPVerifier());
        step2.setConfiguredExecutors(executors2);

        RegistrationStep step3 = new RegistrationStep();
        step3.setOrder(2);
        step3.setMultiOption(false);
        step3.setSelectedExecutor(null);
        step3.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);

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

    private static RegistrationStepExecutorConfig buildAttributeCollector1() {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setGivenName("AttributeCollector1");
        config.setName("AttributeCollector");
        config.setId("uuid_attr_1");

        ClaimMapping[] claimMappings = new ClaimMapping[4];

        ClaimMapping claimMapping1 = ClaimMapping.build("http://wso2.org/claims/username", "username",
                null, true, true);
        ClaimMapping claimMapping2 = ClaimMapping.build("http://wso2.org/claims/firstname", "email",
                null, true, true);
        ClaimMapping claimMapping3 = ClaimMapping.build("http://wso2.org/claims/lastname", "lastname",
                null, true, true);
        ClaimMapping claimMapping4 = ClaimMapping.build("http://wso2.org/claims/emailaddress", "email",
                null, true, true);
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
        config.setGivenName("AttributeCollector2");
        config.setName("AttributeCollector");
        config.setId("uuid_attr_2");

        ClaimMapping[] claimMappings = new ClaimMapping[2];

        ClaimMapping claimMapping1 = ClaimMapping.build("http://wso2.org/claims/country", "country",
                null, true, true);
        ClaimMapping claimMapping2 = ClaimMapping.build("http://wso2.org/claims/dob", "dateofbirth",
                null, true, true);

        claimMappings[0] = claimMapping1;
        claimMappings[1] = claimMapping2;

        config.setRequestedClaims(claimMappings);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));
        return config;
    }

    private static RegistrationStepExecutorConfig buildPasswordOnboarder() {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setGivenName("Password collection");
        config.setName("PasswordOnboarding");
        config.setId("uuid_pwd_1");
        config.setExecutor(getRegStepExecutor("PasswordOnboarding"));
        return config;
    }

    private static RegistrationStepExecutorConfig buildEmailOTPVerifier() throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setGivenName("Email OTP Verification");
        config.setName("EmailOTPVerifier");
        config.setId("uuid_email_otp_1");
        config.setExecutor(getRegStepExecutor("EmailOTPVerifier"));
        config.setIdentityProvider(getIdentityProvider("EmailOTP"));
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

    private static RegistrationStepExecutor getRegStepExecutor(String name) {

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (name.equals(executor.getName())) {
                return executor;
            }
        }
        return null;
    }


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
}
