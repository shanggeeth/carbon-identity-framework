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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

public class PasswordOnboarder implements AuthLinkedExecutor {

    private List<InputMetaData> inputMetaData = new ArrayList<>();

    public String getName() {

        return "PasswordOnboarder";
    }

    public String getAuthMechanism() {

        return "BasicAuthenticator";
    }

    @Override
    public ExecutorResponse execute(Map<String, String> input, RegistrationContext context) {

        if (input != null && !input.isEmpty()) {
            inputMetaData.removeIf(
                    data -> input.containsKey(data.getName()) && input.get(data.getName()) != null && !input.get(
                            data.getName()).isEmpty());
        } else {
            declareRequiredData();
        }

        if (!inputMetaData.isEmpty()) {
            ExecutorResponse executorResponse = new ExecutorResponse(STATUS_USER_INPUT_REQUIRED);
            executorResponse.setRequiredData(inputMetaData);
            return executorResponse;
        }
        return new ExecutorResponse(Constants.STATUS_COMPLETE);
    }

    @Override
    public List<InputMetaData> declareRequiredData() {

        InputMetaData e1 = new InputMetaData("password", "credential", 1);
        e1.setMandatory(true);
        e1.setValidationRegex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        inputMetaData.add(e1);
        return inputMetaData;
    }
}
