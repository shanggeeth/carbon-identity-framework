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

package org.wso2.carbon.identity.user.self.registration.executor.impl;

import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The executor class that handles the user onboarding to the system.
 */
public class UserOnboardingExecutor {

    public String getName() {

        return "user-onboarding-executor";
    }

    public List<InputMetaData> declareRequiredData() {

        return null;
    }

    public ExecutorResponse execute(Map<String, String> input, RegistrationContext context)
            throws RegistrationFrameworkException {

        String tenantDomain = context.getTenantDomain();
        RegistrationRequestedUser user = context.getRegisteringUser();
        UserStoreManager userStoreManager = getUserstoreManager(tenantDomain);

//        Map<String, String> claims = new HashMap<>(user.getClaims());
        Map<String, String> claims = new HashMap<>();

        String password = String.valueOf(new DefaultPasswordGenerator().generatePassword());
//        if (!user.isPasswordless()) {
//            password = user.getCredential();
//        } else {
//            password = String.valueOf(new DefaultPasswordGenerator().generatePassword());
//        }
        try {
            userStoreManager
                    .addUser(IdentityUtil.addDomainToName(user.getUsername(), "PRIMARY"), password, null, claims, null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            return null;
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while creating user", e);
        }
    }

    private UserStoreManager getUserstoreManager(String tenantDomain) throws RegistrationFrameworkException {

        RealmService realmService = UserRegistrationServiceDataHolder.getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistrationFrameworkException("Error while retrieving user store manager", e);
        }
    }
}
