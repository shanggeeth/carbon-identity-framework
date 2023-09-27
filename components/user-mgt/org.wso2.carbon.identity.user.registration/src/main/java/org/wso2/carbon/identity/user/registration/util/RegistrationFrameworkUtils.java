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

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.model.RegistrationSequence;
import org.wso2.carbon.identity.user.registration.model.RegistrationStep;

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
                                    RegistrationFlowConstants.SupportedProtocol type) {

        RegistrationContext context = new RegistrationContext();
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setFlowStatus(RegistrationFlowConstants.Status.INCOMPLETE);
        context.setCurrentStep(0);

        RegistrationRequestedUser user = new RegistrationRequestedUser();
        context.setRegisteringUser(user);
        context.setRegistrationSequence(buildDefaultRegSequence(appId));
        return context;

    }

    public static RegistrationSequence buildDefaultRegSequence(String appId) {

        RegistrationSequence sequence = new RegistrationSequence();
        sequence.setApplicationId(appId);

        RegistrationStep step1 = new RegistrationStep();
        step1.setOrder(1);
        step1.setMultiOption(false);
        step1.setSelectedExecutor(null);
        step1.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);
        List<RegistrationStepExecutor> executors = new ArrayList<>();
        executors.add(UserRegistrationServiceDataHolder.getRegistrationStepExecutors().get(0));
        step1.setConfiguredExecutors(executors);

        RegistrationStep step2 = new RegistrationStep();
        step2.setOrder(2);
        step2.setMultiOption(false);
        step2.setSelectedExecutor(null);
        step2.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);
        List<RegistrationStepExecutor> executors2 = new ArrayList<>();
        executors2.add(UserRegistrationServiceDataHolder.getRegistrationStepExecutors().get(0));
        executors2.add(UserRegistrationServiceDataHolder.getRegistrationStepExecutors().get(1));
        step2.setConfiguredExecutors(executors2);

        Map<Integer, RegistrationStep> stepMap =  new HashMap<>();
        stepMap.put(1, step1);
        stepMap.put(2, step2);

        sequence.setStepMap(stepMap);
        return sequence;
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
