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

package org.wso2.carbon.identity.user.registration.model.response;

import org.wso2.carbon.identity.user.registration.util.RegistrationConstants;

/**
 * This class represents the result of a registration step.
 */
public class RegistrationStepResult {

    private RegistrationConstants.StepStatus status = RegistrationConstants.StepStatus.NOT_STARTED;
    private Message message;
    private ExecutorResponse response;

    public RegistrationConstants.StepStatus getStatus() {

        return status;
    }

    public void setStatus(RegistrationConstants.StepStatus status) {

        this.status = status;
    }

    public Message getMessage() {

        return message;
    }

    public void setMessage(Message message) {

        this.message = message;
    }

    public ExecutorResponse getResponse() {

        return response;
    }

    public void setResponse(ExecutorResponse response) {

        this.response = response;
    }
}
