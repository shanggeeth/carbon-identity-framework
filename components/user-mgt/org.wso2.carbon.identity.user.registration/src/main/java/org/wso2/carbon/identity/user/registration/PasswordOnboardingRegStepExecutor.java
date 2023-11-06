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
import org.wso2.carbon.identity.user.registration.model.response.ExecutorMetadata;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.model.response.Message;
import org.wso2.carbon.identity.user.registration.model.response.NextStepResponse;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.COMPLETE;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.NOT_STARTED;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED;

public class PasswordOnboardingRegStepExecutor implements RegistrationStepExecutor {

    private static PasswordOnboardingRegStepExecutor instance = new PasswordOnboardingRegStepExecutor();
    private static final String PASSWORD = "password";
    private static final String USERNAME_URI = "http://wso2.org/claims/username";

    public static PasswordOnboardingRegStepExecutor getInstance() {

        return instance;
    }

    @Override
    public String getName() {

        return "PasswordOnboarding";
    }

    @Override
    public RegistrationFlowConstants.RegistrationExecutorBindingType getBindingType() throws RegistrationFrameworkException {

        return RegistrationFlowConstants.RegistrationExecutorBindingType.AUTHENTICATOR;
    }

    @Override
    public String getBoundIdentifier() throws RegistrationFrameworkException {

        return "BasicAuthenticator";
    }

    @Override
    public String getExecutorType() throws RegistrationFrameworkException {

        return "CredentialOnboarding";
    }

    @Override
    public List<RequiredParam> getRequiredParams() {

        List<RequiredParam> params = new ArrayList<>();
        RequiredParam param1 = new RequiredParam();
        param1.setName(USERNAME_URI);
        param1.setAvailableValue(null);
        param1.setConfidential(false);
        param1.setMandatory(true);
        params.add(param1);

        return params;
    }

    @Override
    public RegistrationFlowConstants.StepStatus execute(RegistrationRequest request, RegistrationContext context,
                                                        NextStepResponse response,
                                                        RegistrationStepExecutorConfig config) throws RegistrationFrameworkException {

        RegistrationFlowConstants.StepStatus status = context.getCurrentStepStatus();
        RegistrationRequestedUser user = context.getRegisteringUser();

        if (user.getUsername() == null) {
            if (NOT_STARTED.equals(status)) {
                Map<String, String> inputs = request.getInputs();
                if (inputs != null && inputs.get(USERNAME_URI) != null) {
                    user.setUsername(inputs.get(USERNAME_URI));
                } else {
                    Message message = new Message();
                    message.setMessage("Define a username");
                    message.setType(RegistrationFlowConstants.MessageType.INFO);
                    updateResponse(response, config, this.getRequiredParams(), message);
                    context.updateRequestedParameterList(this.getRequiredParams());
                    return USER_INPUT_REQUIRED;
                }
            } else if (USER_INPUT_REQUIRED.equals(status)) {
                Map<String, String> inputs = request.getInputs();
                if (inputs != null && inputs.get("http://wso2.org/claims/username") != null) {
                    user.setUsername(inputs.get("http://wso2.org/claims/username"));
                } else {
                    throw new RegistrationFrameworkException("Username is not defined");
                }
                status = INCOMPLETE;
            }
        }
        if (USER_INPUT_REQUIRED.equals(status)) {
            return processPassword(request, context);
        }
        return requestPassword(response, config);
    }

    private void updateResponse(NextStepResponse response, RegistrationStepExecutorConfig config,
                                List<RequiredParam> params, Message message) {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setName(config.getName());
        executorResponse.setExecutorName(this.getName());
        executorResponse.setId(config.getId());

        ExecutorMetadata metadata = new ExecutorMetadata();
        metadata.setI18nKey("executor.passwordOnboarding");
        metadata.setPromptType(RegistrationFlowConstants.PromptType.USER_PROMPT);
        metadata.setRequiredParams(params);
        executorResponse.setMetadata(metadata);

        response.addExecutor(executorResponse);
        response.addMessage(message);
    }

    private RegistrationFlowConstants.StepStatus processPassword(RegistrationRequest request,
                                                                 RegistrationContext context) throws RegistrationFrameworkException {

            Map<String, String> inputs = request.getInputs();
            RegistrationRequestedUser user = context.getRegisteringUser();

            if (inputs.get("password") == null) {
                throw new RegistrationFrameworkException("Password is not set as expected in the step.");
            }
            user.setPasswordless(false);
            user.setCredential(inputs.get("password"));
            return COMPLETE;
    }

    private RegistrationFlowConstants.StepStatus requestPassword(NextStepResponse response,
                                                                 RegistrationStepExecutorConfig config) {

        List<RequiredParam> params = new ArrayList<>();
        RequiredParam param1 = new RequiredParam();
        param1.setName("password");
        param1.setConfidential(true);
        param1.setMandatory(true);
        params.add(param1);

        Message message = new Message();
        message.setMessage("Onboard a password");
        message.setType(RegistrationFlowConstants.MessageType.INFO);

        updateResponse(response, config, params, message);

        return USER_INPUT_REQUIRED;
    }
}
