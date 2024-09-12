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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to represent the response of an executor.
 */
public class ExecutorResponse {

    private String status;
    private String executorName;
    private List<InputMetaData> requiredData;

    public ExecutorResponse(String status) {
        this.status = status;
        this.requiredData = new ArrayList<>();
    }

    public String getExecutorName() {

        return executorName;
    }

    public void setExecutorName(String executorName) {

        this.executorName = executorName;
    }

    // Getters and Setters
    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public List<InputMetaData> getRequiredData() {

        return requiredData;
    }

    public void setRequiredData(List<InputMetaData> requiredData) {

        this.requiredData = requiredData;
    }

    // Add model.Element to requiredData list.
    public void addRequiredData(InputMetaData inputMetaData) {

        requiredData.add(inputMetaData);
    }
}

