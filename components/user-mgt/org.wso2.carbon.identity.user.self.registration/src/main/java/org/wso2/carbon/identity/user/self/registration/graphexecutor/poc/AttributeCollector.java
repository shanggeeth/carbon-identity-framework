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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

public class AttributeCollector implements Executor {

    private String name;
    private final List<InputMetaData> requiredData = new ArrayList<>();

    public AttributeCollector(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void addRequiredData(InputMetaData inputMetaData) {

        requiredData.add(inputMetaData);
    }

    @Override
    public ExecutorResponse process(Map<String, String> input, RegistrationContext context) {
        // Implement the actual task logic here
        if (input != null && !input.isEmpty()) {
            Iterator<InputMetaData> iterator = requiredData.iterator();
            while (iterator.hasNext()) {
                InputMetaData data = iterator.next();

                // Check if data.getName is there as a key in the input map.
                if (input.containsKey(data.getName())) {
                    // Check if the value of the key is not null or empty.
                    if (input.get(data.getName()) != null && !input.get(data.getName()).isEmpty()) {
                        // Remove the data from the requiredData list using the iterator.
                        iterator.remove();
                    }
                }
            }
        }

        if (!requiredData.isEmpty()) {
            ExecutorResponse inputs = new ExecutorResponse(STATUS_USER_INPUT_REQUIRED);
            inputs.setRequiredData(requiredData);
            return inputs;
        }
        return new ExecutorResponse(STATUS_COMPLETE);
    }

    @Override
    public List<InputMetaData> declareRequiredData() {

        return this.requiredData;
    }
}
