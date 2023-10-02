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

package org.wso2.carbon.identity.user.registration;

import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.model.response.RegistrationResponse;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PasswordOnboardingRegStepExecutor implements RegistrationStepExecutor {

    private static PasswordOnboardingRegStepExecutor instance = new PasswordOnboardingRegStepExecutor();

    public static PasswordOnboardingRegStepExecutor getInstance() {

        return instance;
    }

    @Override
    public String getName() {

        return "PasswordOnboarding";
    }

    @Override
    public RegistrationFlowConstants.RegistrationExecutorBindingType getBindingType() throws RegistrationFrameworkException {

        return RegistrationFlowConstants.RegistrationExecutorBindingType.NONE;
    }

    @Override
    public String getBoundIdentifier() throws RegistrationFrameworkException {

        return null;
    }

    @Override
    public String getExecutorType() throws RegistrationFrameworkException {

        return "CredentialOnboarding";
    }

    @Override
    public ExecutorResponse execute(RegistrationRequest registrationRequest, RegistrationContext context, RegistrationStepExecutorConfig config)
            throws RegistrationFrameworkException {

        ExecutorResponse response = new ExecutorResponse();
        response.setGivenName(config.getGivenName());
        response.setName(this.getName());
        response.setId("password-onboarding");

        if ( registrationRequest == null || registrationRequest.getInputs() == null ) {

            List<RequiredParam> params = new ArrayList<>();
            RequiredParam param1 = new RequiredParam();
            param1.setName("password");
            param1.setConfidential(true);
            param1.setMandatory(true);
            params.add(param1);

            response.setStatus(RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED);
            response.setRequiredParams(params);
        } else if (registrationRequest.getInputs() != null) {

            Map<String, String> inputs = registrationRequest.getInputs();
            RegistrationRequestedUser user =  context.getRegisteringUser();
            if (user == null) {
                throw new RegistrationFrameworkException("User not found in the registration context");
            }

            if (inputs.get("password") == null) {
                throw new RegistrationFrameworkException("Password is not set as expected in the step");
            }
            user.setCredential(inputs.get("password"));
            response.setStatus(RegistrationFlowConstants.StepStatus.COMPLETE);
        }
        return response;
    }
}
