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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NOT_STARTED;

public class RegistrationContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;

    // Constants introduced with graph execution.
    private Node currentNode;
    private RegSequence regSequence;
    private RegistrationRequestedUser registeringUser = new RegistrationRequestedUser();
    private String tenantDomain;
    private String contextIdentifier;
    private final Map<String, InputData> userInputs = new HashMap<>();
    private Map<String, List<InputMetaData>> requiredMetaData;
    private String executorStatus = STATUS_NOT_STARTED;

    private final List<String> authenticatedMethods = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();

    public Node getCurrentNode() {

        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {

        this.currentNode = currentNode;
    }

    public RegSequence getRegSequence() {

        return regSequence;
    }

    public void setRegSequence(RegSequence regSequence) {

        this.regSequence = regSequence;
    }

    public void removeUserInputFromContext(String key) {

        userInputs.remove(key);
    }

    public void addUserInputs(String key, InputData value) {

        userInputs.put(key, value);
    }

    public void addUserInputs(Map<String, InputData> inputDataMap) {

        if (inputDataMap != null) {
            userInputs.putAll(inputDataMap);
        }
    }

    public Map<String, List<InputMetaData>> getRequiredMetaData() {

        return requiredMetaData;
    }

    public void setRequiredMetaData(Map<String, List<InputMetaData>> requiredMetaData) {

        this.requiredMetaData = requiredMetaData;
    }

    public String getContextIdentifier() {

        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {

        this.contextIdentifier = contextIdentifier;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getExecutorStatus() {

        return executorStatus;
    }

    public void setExecutorStatus(String executorStatus) {

        this.executorStatus = executorStatus;
    }

    public RegistrationRequestedUser getRegisteringUser() {

        return registeringUser;
    }

    public void setRegisteringUser(RegistrationRequestedUser registeringUser) {

        this.registeringUser = registeringUser;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    public Object getProperty(String key) {

        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    public void addAuthenticatedMethod(String authenticator) {

        this.authenticatedMethods.add(authenticator);
    }

    public List<String> getAuthenticatedMethods() {

        return authenticatedMethods;
    }

    /**
     * Retrieve and remove a given user input from the context.
     *
     * @param key Key of the user input.
     * @return User input data.
     */
    public InputData retrieveUserInputFromContext(String key) {

        InputData requestedData = userInputs.get(key);
        userInputs.remove(key);

        return requestedData;
    }

    /**
     * Update the user input list in the context with the given input data map. This method will also update the
     * required data list in the context.
     *
     * @param inputDataMap User input data.
     */
    public void updateRequiredDataWithInputs(Map<String, InputData> inputDataMap) {

        for (Map.Entry<String, InputData> entry : inputDataMap.entrySet()) {
            if (entry.getValue() != null) {
                requiredMetaData.remove(entry.getKey());
                userInputs.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
