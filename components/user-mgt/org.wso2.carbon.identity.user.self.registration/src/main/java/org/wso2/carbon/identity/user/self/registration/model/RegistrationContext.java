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

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.self.registration.config.RegistrationSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;
import org.wso2.carbon.identity.user.self.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;

    // Constants introduced with graph execution.
    private String currentStatus = Constants.STATUS_FLOW_NOT_STARTED;
    private Node currentNode;
    private RegSequence regSequence;
    private RegistrationRequestedUser registeringUser = new RegistrationRequestedUser();
    private String tenantDomain;
    private String contextIdentifier;
    private final Map<String, InputData> userInputs = new HashMap<>();
    private Map<String, List<InputMetaData>> requiredMetaData;

    private int currentStep = 0;
    private RegistrationConstants.StepStatus currentStepStatus;
    private String requestType;
    private String relyingParty;
    private RegistrationSequence registrationSequence;
    private List<RequiredParam> requestedParameters;
    private List<String> engagedStepAuthenticators = new ArrayList<>();
    private boolean isCompleted;
    private Map<String, Object> properties = new HashMap<>();
    private ServiceProvider serviceProvider;
    private String flowStatus;

    public String getCurrentStatus() {

        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {

        this.currentStatus = currentStatus;
    }

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

    public InputData retrieveUserInputFromContext(String key) {

        InputData requestedData = userInputs.get(key);
        userInputs.remove(key);

        return requestedData;
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

    public String getFlowStatus() {

        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {

        this.flowStatus = flowStatus;
    }


    public int getCurrentStep() {

        return currentStep;
    }

    public void setCurrentStep(int currentStep) {

        this.currentStep = currentStep;
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

    public String getRequestType() {

        return requestType;
    }

    public void setRequestType(String requestType) {

        this.requestType = requestType;
    }

    public String getRelyingParty() {

        return relyingParty;
    }


    public void setRelyingParty(String relyingParty) {

        this.relyingParty = relyingParty;
    }

    public RegistrationRequestedUser getRegisteringUser() {

        return registeringUser;
    }

    public void setRegisteringUser(RegistrationRequestedUser registeringUser) {

        this.registeringUser = registeringUser;
    }

    public RegistrationSequence getRegistrationSequence() {

        return registrationSequence;
    }

    public void setRegistrationSequence(RegistrationSequence registrationSequence) {

        this.registrationSequence = registrationSequence;
    }

    public boolean isCompleted() {

        return isCompleted;
    }

    public void setCompleted(boolean completed) {

        isCompleted = completed;
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

    public List<RequiredParam> getRequestedParameters() {

        return this.requestedParameters;
    }

    public void updateRequestedParameterList(List<RequiredParam> newParamList) {

        if (this.requestedParameters == null) {
            this.requestedParameters = new ArrayList<>();
        } else {
            this.requestedParameters.clear();
        }
        this.requestedParameters.addAll(newParamList);
    }

    public RegistrationConstants.StepStatus getCurrentStepStatus() {

        return currentStepStatus;
    }

    public void setCurrentStepStatus(RegistrationConstants.StepStatus currentStepStatus) {

        this.currentStepStatus = currentStepStatus;
    }

    public void addEngagedStepAuthenticator(String authenticator) {

        this.engagedStepAuthenticators.add(authenticator);
    }

    public List<String> getEngagedStepAuthenticators() {

        return engagedStepAuthenticators;
    }

    public ServiceProvider getServiceProvider() {

        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {

        this.serviceProvider = serviceProvider;
    }
}
