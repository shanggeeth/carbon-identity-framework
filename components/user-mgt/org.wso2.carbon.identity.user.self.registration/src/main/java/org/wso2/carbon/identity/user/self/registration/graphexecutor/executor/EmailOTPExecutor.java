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

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

public class EmailOTPExecutor implements AuthLinkedExecutor {

    private final List<InputMetaData> inputMetaData = getInitiallyRequiredData();

    private final String EMAIL_ADDRESS = "emailaddress";
    private final String EMAIL_OTP = "email-otp";

    public String getName() {

        return "EmailOTPVerifier";
    }

    public String getAuthMechanism() {

        return "EmailOTP";
    }

    @Override
    public ExecutorResponse execute(Map<String, String> input, RegistrationContext context) {

        // Implement the actual task logic here
        if (input != null && !input.isEmpty()) {
            if (input.containsKey(EMAIL_ADDRESS)) {
                // Send OTP to the email address
                // Generate OTP
                // Send OTP to the email address
                // Store the OTP in the context
                // Store the email address in the context
                // Update the required data
                inputMetaData.clear();
                inputMetaData.addAll(getIntermediateRequiredData());
            } else if (input.containsKey(EMAIL_OTP)) {
                // Validate the OTP
                // If the OTP is valid, update the context
                // Update the required data
                inputMetaData.clear();
            }
        }

        if (!inputMetaData.isEmpty()) {
            ExecutorResponse executorResponse = new ExecutorResponse(STATUS_USER_INPUT_REQUIRED);
            executorResponse.setRequiredData(inputMetaData);
            return executorResponse;
        }
        return new ExecutorResponse(STATUS_NODE_COMPLETE);
    }

    @Override
    public List<InputMetaData> declareRequiredData() {

        return getInitiallyRequiredData();
    }


    private List<InputMetaData> getInitiallyRequiredData() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData(EMAIL_ADDRESS, "string", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }

    private List<InputMetaData> getIntermediateRequiredData(){

        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData(EMAIL_OTP, "otp", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }
}
