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

package org.wso2.carbon.identity.user.registration.model;

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.registration.config.RegistrationSequence;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;

    private int currentStep = 0;
    private RegistrationConstants.StepStatus currentStepStatus;
    private String contextIdentifier;
    private String tenantDomain;
    private String requestType;
    private String relyingParty;
    private RegistrationRequestedUser registeringUser = new RegistrationRequestedUser();
    private RegistrationSequence registrationSequence;
    private List<RequiredParam> requestedParameters;
    private List<String> engagedStepAuthenticators = new ArrayList<>();
    private boolean isCompleted;
    private Map<String, Object> properties = new HashMap<>();
    private ServiceProvider serviceProvider;

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
