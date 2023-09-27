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

package org.wso2.carbon.identity.user.registration.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;

import java.util.ArrayList;
import java.util.List;

public class AuthSequenceBasedConfigLoader {

    private static final Log LOG = LogFactory.getLog(AuthSequenceBasedConfigLoader.class);
    private static AuthSequenceBasedConfigLoader instance = new AuthSequenceBasedConfigLoader();

    private AuthSequenceBasedConfigLoader() {

    }

    public static AuthSequenceBasedConfigLoader getInstance() {

        return instance;
    }

    public List<RegistrationStepExecutor> getRegExecutorsFromAuthSeq() {

        return null;
    }

    public List<String> getConfiguredAuthenticators(String appId, String tenantDomain) throws RegistrationFrameworkException {

        List<String> authenticators = new ArrayList<>();
        AuthenticationStep[] authenticationSteps;
        try {
            LOG.debug("Attempting to retrieve configured authenticators for appId: " + appId);

            ApplicationManagementService appMgtService =
                    UserRegistrationServiceDataHolder.getApplicationManagementService();
            if (appMgtService != null) {
                authenticationSteps = appMgtService.getConfiguredAuthenticators(appId, tenantDomain);
            } else {
                throw new RegistrationFrameworkException("ApplicationManagementService is null.");
            }
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error while retrieving configured authenticators for appId: " + appId, e);
        }
        if (authenticationSteps != null) {
            for (AuthenticationStep authenticationStep : authenticationSteps) {
                LocalAuthenticatorConfig[] configs = authenticationStep.getLocalAuthenticatorConfigs();
                if (configs != null) {
                    for (LocalAuthenticatorConfig config : configs) {
                        if (!authenticators.contains(config.getName())) {
                            authenticators.add(config.getName());
                        }
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Configured authenticators for appId: %s : %s", appId,
                    StringUtils.join(authenticators, ",")));
        }
        return authenticators;
    }
}
