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

package org.wso2.carbon.identity.user.self.registration.model.response;

import org.wso2.carbon.identity.user.self.registration.util.RegistrationConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the next registration step along with its type and the configured executors.
 */
public class NextStepResponse {

    private RegistrationConstants.StepType type;
    private List<ExecutorResponse> executors = new ArrayList<>();
    private Message message;

    public RegistrationConstants.StepType getType() {

        return type;
    }

    public void setType(RegistrationConstants.StepType type) {

        this.type = type;
    }

    public List<ExecutorResponse> getExecutors() {

        return executors;
    }

    public void setExecutors(List<ExecutorResponse> executors) {

        this.executors = executors;
    }

    public void addExecutor(ExecutorResponse executor) {

        this.executors.add(executor);
    }

    public Message getMessage() {

        return message;
    }

    public void setMessage(Message message) {

        this.message = message;
    }
}
