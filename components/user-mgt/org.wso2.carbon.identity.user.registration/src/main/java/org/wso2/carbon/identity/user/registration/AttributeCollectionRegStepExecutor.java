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

import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.model.response.Message;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeCollectionRegStepExecutor implements RegistrationStepExecutor {

    private static AttributeCollectionRegStepExecutor instance = new AttributeCollectionRegStepExecutor();

    public static AttributeCollectionRegStepExecutor getInstance() {

        return instance;
    }

    @Override
    public String getName() {

        return "AttributeCollector";
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

        return "AttributeCollectorType";
    }

    @Override
    public ExecutorResponse execute(RegistrationRequest registrationRequest, RegistrationContext context, RegistrationStepExecutorConfig config)
            throws RegistrationFrameworkException {

        ExecutorResponse response = new ExecutorResponse();
        response.setGivenName(config.getGivenName());
        response.setName(this.getName());
        response.setId(config.getId());

        if ( registrationRequest == null || registrationRequest.getInputs() == null ) {

            context.getRegistrationSequence().getStepMap().get(context.getCurrentStep()).setStatus(
                    RegistrationFlowConstants.StepStatus.INCOMPLETE);

            List<RequiredParam> params = new ArrayList<>();

            int displayOder = 0;
            for (ClaimMapping mapping : config.getRequestedClaims()) {
                RequiredParam param = new RequiredParam();
                param.setName(mapping.getRemoteClaim().getClaimUri());
                param.setConfidential(false);
                param.setMandatory(mapping.isMandatory());
                param.setDataType(RegistrationFlowConstants.DataType.STRING); //TODO: Need to get the data type from the
                param.setOrder(displayOder++);
                param.setI18nKey("i18nKey_claim_should_support");
                param.setValidationRegex("validationRegex_claim_should_support");
                params.add(param);
            }

            response.setStatus(RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED);

            Message message = new Message();
            message.setMessage("User input required");
            message.setType(RegistrationFlowConstants.MessageType.INFO);

            List<Message> messages = new ArrayList<>();
            messages.add(message);

            response.setRequiredParams(params);
            response.setMessages(messages);
        } else if (registrationRequest.getInputs() != null) {

            Map<String, String> inputs = registrationRequest.getInputs();
            RegistrationRequestedUser user =  context.getRegisteringUser();
            if (user == null) {
                user = new RegistrationRequestedUser();
                context.setRegisteringUser(user);
            }

            if (inputs.get("http://wso2.org/claims/username") != null) {
                user.setUsername(inputs.get("http://wso2.org/claims/username"));
            }
            if (user.getClaims() != null) {
                user.getClaims().putAll(inputs);
            } else {
                user.setClaims(inputs);
            }
            response.setStatus(RegistrationFlowConstants.StepStatus.COMPLETE);
        }
        return response;
    }
}
