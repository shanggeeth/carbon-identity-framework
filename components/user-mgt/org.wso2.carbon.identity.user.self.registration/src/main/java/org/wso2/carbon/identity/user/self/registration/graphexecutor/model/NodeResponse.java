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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of a node in the registration sequence.
 */
public class NodeResponse {

    private String status;
    private final Map<String, List<InputMetaData>> inputDataMap =  new HashMap<>();

    public NodeResponse(String status) {

        this.status = status;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, List<InputMetaData>> getInputDataList() {

        return inputDataMap;
    }

    public void addInputData(String name, List<InputMetaData> inputMetaDataList) {

        inputDataMap.put(name, inputMetaDataList);
    }
}
