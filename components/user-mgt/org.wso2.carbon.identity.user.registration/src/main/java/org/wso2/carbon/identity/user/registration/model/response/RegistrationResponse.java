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

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to response while executing the registration flow
 */
public class RegistrationResponse {

    private String flowId;
    private RegistrationConstants.Status status;
    private String userAssertion;
    private NextStepResponse nextStep;
    private List<LinkObject> links = new ArrayList<>();

    public String getFlowId() {

        return flowId;
    }

    public void setFlowId(String flowId) {

        this.flowId = flowId;
    }

    public RegistrationConstants.Status getStatus() {

        return status;
    }

    public void setStatus(RegistrationConstants.Status status) {

        this.status = status;
    }

    public NextStepResponse getNextStep() {

        return nextStep;
    }

    public void setNextStep(NextStepResponse nextStep) {

        this.nextStep = nextStep;
    }

    public List<LinkObject> getLinks() {

        return links;
    }

    public void setLinks(List<LinkObject> links) {

        this.links = links;
    }

    public String getUserAssertion() {

        return userAssertion;
    }

    public void setUserAssertion(String userAssertion) {

        this.userAssertion = userAssertion;
    }
}
