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

package org.wso2.carbon.identity.user.registration.config;

import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegistrationStep implements Serializable {

    private static final long serialVersionUID = -6352819516789400021L;
    private String id;
    private RegistrationFlowConstants.StepType type;
    private RegistrationStepExecutorConfig selectedExecutor;
    private List<RegistrationStepExecutorConfig> configuredExecutors = new ArrayList<>();

    // Write the default constructor for this class.
    public RegistrationStep() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public RegistrationStepExecutorConfig getSelectedExecutor() {

        return selectedExecutor;
    }

    public void setSelectedExecutor(RegistrationStepExecutorConfig selectedExecutor) {

        this.selectedExecutor = selectedExecutor;
    }

    public List<RegistrationStepExecutorConfig> getConfiguredExecutors() {

        return configuredExecutors;
    }

    public void setConfiguredExecutors(List<RegistrationStepExecutorConfig> configuredExecutors) {

        this.configuredExecutors = configuredExecutors;
    }

    public void addConfiguredExecutor(RegistrationStepExecutorConfig configuredExecutor) {

        this.configuredExecutors.add(configuredExecutor);
    }

    public RegistrationFlowConstants.StepType getType() {

        return type;
    }

    public void setType(RegistrationFlowConstants.StepType type) {

        this.type = type;
    }
}
