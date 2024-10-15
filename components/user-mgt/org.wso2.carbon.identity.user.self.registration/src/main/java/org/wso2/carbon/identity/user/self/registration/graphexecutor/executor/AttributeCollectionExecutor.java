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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.executor;

import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Handles the attribute collection for the signup user.
 */
public class AttributeCollectionExecutor implements Executor {

    private String name;
    private final List<InputMetaData> requiredData = new ArrayList<>();

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return this.name;
    }

    public void addRequiredData(InputMetaData inputMetaData) {

        requiredData.add(inputMetaData);
    }

    @Override
    public ExecutorResponse execute(Map<String, String> input, RegistrationContext context) {

        if (input != null && !input.isEmpty()) {
            requiredData.removeIf(
                    data -> input.containsKey(data.getName()) && input.get(data.getName()) != null && !input.get(
                            data.getName()).isEmpty());
        }

        if (!requiredData.isEmpty()) {
            ExecutorResponse inputs = new org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse(STATUS_USER_INPUT_REQUIRED);
            inputs.setRequiredData(requiredData);
            return inputs;
        }
        return new ExecutorResponse(STATUS_COMPLETE);
    }

    @Override
    public List<InputMetaData> declareRequiredData() {

        return this.requiredData;
    }

//    public void execute(Map<String, String> inputs, RegistrationContext context, RegistrationStepExecutorConfig config)
//            throws RegistrationFrameworkException {
//
//
//        if (inputs == null ) {
//
//            List<RequiredParam> params = new ArrayList<>();
//            List<RequiredParam> undefinedParams = new ArrayList<>();
//
//            int displayOder = 0;
//            // TODO: Consider only the non-identity claims.
//            for (ClaimMapping mapping : config.getRequestedClaims()) {
//                String claimUri = mapping.getRemoteClaim().getClaimUri();
//                RequiredParam param = new RequiredParam();
//                param.setName(claimUri);
//                param.setConfidential(false);
//                param.setMandatory(mapping.isMandatory());
//                param.setDataType(RegistrationConstants.DataType.STRING); //TODO: Need to get the data type from the
//                param.setOrder(displayOder++);
//
//                if (context.getRegisteringUser() != null && context.getRegisteringUser().getClaims() != null &&
//                        context.getRegisteringUser().getClaims().get(claimUri) != null) {
//                    param.setAvailableValue(context.getRegisteringUser().getClaims().get(claimUri));
//                } else {
//                    undefinedParams.add(param);
//                }
//                params.add(param);
//            }
//
//            // All the required attributes are already defined. Not need to prompt again.
//            if (undefinedParams.size() == 0) {
//                return COMPLETE;
//            }
//
//            Message message = new Message(RegistrationConstants.MessageType.INFO, "User input required");
//            updateResponse(response, config, params, message);
//            context.updateRequestedParameterList(params);
//
//            return USER_INPUT_REQUIRED;
//        } else if (USER_INPUT_REQUIRED.equals(status) && inputs != null) {
//
//            List<RequiredParam> unsatisfiedParams = new ArrayList<>();
//            RegistrationRequestedUser user =  context.getRegisteringUser();
//            if (user == null) {
//                user = new RegistrationRequestedUser();
//                context.setRegisteringUser(user);
//            }
//
//            if (inputs.get(RegistrationConstants.USERNAME_CLAIM_URI) != null) {
//                user.setUsername(inputs.get(RegistrationConstants.USERNAME_CLAIM_URI));
//            }
//            for (RequiredParam param: context.getRequestedParameters()) {
//                if (param.getAvailableValue() != null) {
//                    // This is a predefined attribute so won't be updated.
//                    continue;
//                }
//                if (param.isMandatory() && inputs.get(param.getName()) == null) {
//                    unsatisfiedParams.add(param);
//                    // Mandatory attribute is not provided. So the step cannot be completed.
//                    continue;
//                }
//                if (inputs.get(param.getName()) != null) {
//                    user.addClaim(param.getName(), inputs.get(param.getName()));
//                }
//
//            }
//            if (unsatisfiedParams.size() > 0) {
//                throw new RegistrationFrameworkException("Mandatory attributes are not provided.");
//            }
//        }
//    }
//
//    private void updateResponse(NextStepResponse response, RegistrationStepExecutorConfig config,
//                                List<RequiredParam> params, Message message) {
//
//        ExecutorResponse executorResponse = new ExecutorResponse();
//        executorResponse.setName(config.getName());
//        executorResponse.setId(config.getId());
//
//        ExecutorMetadata metadata = new ExecutorMetadata();
//        metadata.setI18nKey("executor.attributeCollection");
//        metadata.setPromptType(RegistrationConstants.PromptType.USER_PROMPT);
//        metadata.setRequiredParams(params);
//
//        executorResponse.setMetadata(metadata);
//        executorResponse.setMessage(message);
//
//        response.addExecutor(executorResponse);
//    }
}
