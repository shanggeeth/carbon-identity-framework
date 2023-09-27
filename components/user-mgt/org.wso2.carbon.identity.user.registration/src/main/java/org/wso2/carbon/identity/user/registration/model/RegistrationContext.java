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

import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.io.Serializable;
import java.util.UUID;

public class RegistrationContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;

    private int currentStep;
    private String contextIdentifier;
    private String tenantDomain;
    private String requestType;
    private String relyingParty;
    private RegistrationRequestedUser registeringUser;
    private RegistrationSequence registrationSequence;
    private RegistrationFlowConstants.Status flowStatus;

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

    public RegistrationFlowConstants.Status getFlowStatus() {

        return flowStatus;
    }

    public void setFlowStatus(RegistrationFlowConstants.Status flowStatus) {

        this.flowStatus = flowStatus;
    }
}
