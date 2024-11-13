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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationFrameworkUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_LOADING_USERSTORE_MANAGER;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_ONBOARDING_USER;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_CREATED;

/**
 * Implementation of a node specific to onboarding the user.
 */
public class UserOnboardNode extends AbstractNode {

    public UserOnboardNode() {

        super();
    }

    @Override
    public NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException {

        String tenantDomain = context.getTenantDomain();
        RegistrationRequestedUser user = context.getRegisteringUser();
        UserStoreManager userStoreManager = getUserstoreManager(tenantDomain);

        Map<String, String> credentials = user.getUserCredentials();

        String password;
        if (credentials.containsKey(PASSWORD)) {
            password = credentials.get(PASSWORD);
        } else {
            password = String.valueOf(new DefaultPasswordGenerator().generatePassword());
        }

        Map<String, Object> claims = user.getClaims();
        Map<String, String> userClaims = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            userClaims.put(entry.getKey(), entry.getValue().toString());
        }

        // todo Identify the userdomain properly.
        try {
            userStoreManager
                    .addUser(IdentityUtil.addDomainToName(user.getUsername(), "PRIMARY"), password, null, userClaims,
                             null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            Optional<String> userAssertion = RegistrationFrameworkUtils.getSignedUserAssertion(userid, context);
            context.setUserId(userid);
            userAssertion.ifPresent(context::setUserAssertion);
            return new NodeResponse(STATUS_USER_CREATED);
        } catch (UserStoreException e) {
            throw new RegistrationServerException(ERROR_ONBOARDING_USER.getCode(),
                                                  ERROR_ONBOARDING_USER.getMessage(),
                                                  String.format(ERROR_ONBOARDING_USER.getDescription(),
                                                                user.getUsername()));
        }
    }

    private UserStoreManager getUserstoreManager(String tenantDomain) throws RegistrationFrameworkException {

        RealmService realmService = UserRegistrationServiceDataHolder.getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistrationServerException(ERROR_LOADING_USERSTORE_MANAGER.getCode(),
                                                  ERROR_LOADING_USERSTORE_MANAGER.getMessage(),
                                                  String.format(ERROR_LOADING_USERSTORE_MANAGER.getDescription(),
                                                                tenantDomain));
        }
    }
}
