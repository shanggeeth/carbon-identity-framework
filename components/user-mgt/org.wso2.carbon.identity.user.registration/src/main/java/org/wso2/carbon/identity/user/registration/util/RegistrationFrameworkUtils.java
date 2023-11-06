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
import org.wso2.carbon.identity.user.registration.DefaultRegistrationSequenceHandler;
import org.wso2.carbon.identity.user.registration.RegistrationSequenceHandler;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.config.AuthSequenceBasedConfigLoader;
import org.wso2.carbon.identity.user.registration.config.RegistrationSequence;
import org.wso2.carbon.identity.user.registration.config.RegistrationStep;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
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

        RegistrationSequence sequence = AuthSequenceBasedConfigLoader.getInstance().deriveRegistrationSequence(appId);
        context.setRegistrationSequence(sequence);

        return context;

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
                || RegistrationFlowConstants.DEFAULT_FLOW_DEFINITION.equals(sequence.getFlowDefinition())) {
            return DefaultRegistrationSequenceHandler.getInstance();
        }
        return null;
    }
}
