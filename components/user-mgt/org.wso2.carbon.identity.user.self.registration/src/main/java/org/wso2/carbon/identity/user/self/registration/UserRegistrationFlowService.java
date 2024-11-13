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

package org.wso2.carbon.identity.user.self.registration;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.ExecutionState;
import org.wso2.carbon.identity.user.self.registration.model.InputData;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.temp.AuthBasedSequenceLoader;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationFrameworkUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_SEQUENCE_NOT_DEFINED_FOR_APP;

/**
 * Service class to handle the user registration flow.
 */
public class UserRegistrationFlowService {

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowService.class);
    private static final UserRegistrationFlowService instance = new UserRegistrationFlowService();

    public static UserRegistrationFlowService getInstance() {

        return instance;
    }

    /**
     * Initiates the registration flow for the given application.
     *
     * @param appId Application ID.
     * @return ExecutionState.
     * @throws RegistrationFrameworkException if something goes wrong while initiating the registration flow.
     */
    public ExecutionState initiateFlow(String appId) throws RegistrationFrameworkException {

        String flowId = UUID.randomUUID().toString();
        RegistrationContext context = new RegistrationContext();
        RegSequence sequence = new AuthBasedSequenceLoader().loadSequence(appId);
        context.setRegSequence(sequence);
        context.setContextIdentifier(flowId);

        if (sequence.getFirstNode() == null) {
            throw new RegistrationServerException(ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getCode(),
                                                  ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getMessage(),
                                                  String.format(ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getDescription(),
                                                                appId));
        }

        NodeResponse response = sequence.execute(context);
        RegistrationFrameworkUtils.addRegContextToCache(context);

        // todo include the details for UI rendering.
        return new ExecutionState(flowId, response);
    }

    /**
     * Continues the registration flow for the given flow ID with the provided user inputs.
     *
     * @param flowId Flow ID.
     * @param inputs User inputs.
     * @return ExecutionState.
     * @throws RegistrationFrameworkException if something goes wrong while continuing the registration flow.
     */
    public ExecutionState continueFlow(String flowId, InputData inputs)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.retrieveRegContextFromCache(flowId);
        RegSequence sequence = context.getRegSequence();
        if (!validateInputs(inputs.getUserInput(), context)) {
            throw new RegistrationFrameworkException("Invalid inputs provided.");
        }
        context.updateRequiredDataWithInputs(inputs.getUserInput());
        NodeResponse response = sequence.execute(context);
        ExecutionState state = new ExecutionState(flowId, response);
        // Save the current sequence in the context.
        RegistrationFrameworkUtils.addRegContextToCache(context);

        // todo include the details for UI rendering.
        return state;
    }

    private boolean validateInputs(Map<String, String> inputs, RegistrationContext context) {

        if (context.getRequiredMetaData() == null) {
            return true;
        }
        if (context.getRequiredMetaData() != null && (inputs == null || inputs.isEmpty())) {
            return false;
        }

        for (InputMetaData metaData : context.getRequiredMetaData()) {

            if (metaData.isMandatory() && inputs.get(metaData.getName()) == null) {
                return false;
            }

            if (metaData.getValidationRegex() != null &&
                    !inputs.get(metaData.getName()).matches(metaData.getValidationRegex())) {
                return false;
            }

            // Return false if the given option is not in the list of provided options.
            List<Object> providedOptions = metaData.getOptions();
            if (providedOptions != null && !providedOptions.isEmpty() &&
                    !providedOptions.contains(inputs.get(metaData.getName()))) {
                return false;
            }
        }

        return true;
    }
}
