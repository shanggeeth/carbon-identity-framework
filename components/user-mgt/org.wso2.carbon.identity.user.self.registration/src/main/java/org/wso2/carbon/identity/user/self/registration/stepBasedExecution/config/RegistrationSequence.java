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

package org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegistrationSequence implements Serializable {

    private static final long serialVersionUID = 3847582949819257705L;
    private String name;
    private String applicationId;
    private boolean isAutoLoginEnabled;
    private List<RegistrationStep> stepDefinitions = new ArrayList<>();
    private String flowDefinition;
    private boolean completed;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(String applicationId) {

        this.applicationId = applicationId;
    }

    public boolean isAutoLoginEnabled() {

        return isAutoLoginEnabled;
    }

    public void setAutoLoginEnabled(boolean autoLoginEnabled) {

        isAutoLoginEnabled = autoLoginEnabled;
    }

    public boolean isCompleted() {

        return completed;
    }

    public void setCompleted(boolean completed) {

        this.completed = completed;
    }

    public String getFlowDefinition() {

        return flowDefinition;
    }

    public void setFlowDefinition(String flowDefinition) {

        this.flowDefinition = flowDefinition;
    }

    public void addStepDefinition(RegistrationStep step) {

        stepDefinitions.add(step);
    }

    public List<RegistrationStep> getStepDefinitions() {

        return stepDefinitions;
    }
}
