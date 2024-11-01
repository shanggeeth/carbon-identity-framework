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

package org.wso2.carbon.identity.user.self.registration.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of an executor.
 */
public class ExecutorResponse {

    private String result;
    private List<InputMetaData> requiredData = new ArrayList<>();
    private final Map<String, Object> updatedUserClaims = new HashMap<>();
    private final Map<String, String> userCredentials = new HashMap<>();
    private final Map<String, Object> contextProperties = new HashMap<>();
    private final Map<String, String> additionalInfo = new HashMap<>();
    private Message message;

    public ExecutorResponse() {

    }

    public ExecutorResponse(String result) {

        this.result = result;
        this.requiredData = new ArrayList<>();
    }

    public String getResult() {

        return result;
    }

    public void setResult(String result) {

        this.result = result;
    }

    public List<InputMetaData> getRequiredData() {

        return requiredData;
    }

    public void setRequiredData(List<InputMetaData> requiredData) {

        this.requiredData = requiredData;
    }

    public void addRequiredData(List<InputMetaData> requiredData) {

        this.requiredData.addAll(requiredData);
    }

    public Message getMessage() {

        return message;
    }

    public void setMessage(Message message) {

        this.message = message;
    }

    public Map<String, Object> getUpdatedUserClaims() {

        return updatedUserClaims;
    }

    public void addUpdatedUserClaims(String key, String value) {

        updatedUserClaims.put(key, value);
    }

    public Map<String, String> getUserCredentials() {

        return userCredentials;
    }

    public void addUserCredentials(String key, String value) {

        userCredentials.put(key, value);
    }

    public Map<String, Object> getContextProperties() {

        return contextProperties;
    }

    public void addContextProperty(String key, String value) {

        contextProperties.put(key, value);
    }

    public Map<String, String> getAdditionalInfo() {

        return additionalInfo;
    }

    public void addAdditionalInfo(String key, String value) {

        contextProperties.put(key, value);
    }

}
