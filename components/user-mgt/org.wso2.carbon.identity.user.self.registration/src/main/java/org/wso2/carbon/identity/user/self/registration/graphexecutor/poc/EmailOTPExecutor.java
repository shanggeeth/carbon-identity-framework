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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.poc;

import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailOTPExecutor implements Executor {

    private final List<InputMetaData> inputMetaData = new ArrayList<>();

    public String getName() {

        return "executor.EmailOTPExecutor";
    }

    @Override
    // Update all the places where process method is called to pass RegistrationContext as a parameter.

    public ExecutorResponse process(Map<String, String> input, RegistrationContext context) {

        getInitiallyRequiredData();
        // Implement the actual task logic here
        if (input != null && !input.isEmpty()) {
            for (InputMetaData data : inputMetaData) {

                // Check if data.getName is there as a key in the input map.
                if (input.containsKey(data.getName())) {
                    // Check if the value of the key is not null or empty.
                    if (input.get(data.getName()) != null && !input.get(data.getName()).isEmpty()) {
                        // Remove the data from the requiredData list.
                        inputMetaData.remove(data);
                    }
                }
            }
        }

        if (!inputMetaData.isEmpty()) {
            ExecutorResponse executorResponse = new ExecutorResponse("USER_INPUT");
            executorResponse.setRequiredData(inputMetaData);
            return executorResponse;
        }
        return new ExecutorResponse("NONE");
    }

    @Override
    public List<InputMetaData> declareRequiredData() {

        return getInitiallyRequiredData();
    }


    private List<InputMetaData> getInitiallyRequiredData() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData("emailaddress", "string", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }

    private List<InputMetaData> getIntermediateRequiredData(){

        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData("email-otp", "otp", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }
}
