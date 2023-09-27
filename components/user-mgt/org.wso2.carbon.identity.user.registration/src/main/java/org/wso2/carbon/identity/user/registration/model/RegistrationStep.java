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

import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegistrationStep implements Serializable {

    private static final long serialVersionUID = -6352819516789400021L;
    private int order;
    private boolean isMultiOption;
    private RegistrationFlowConstants.StepStatus status;
    private RegistrationStepExecutor selectedExecutor;
    private List<RegistrationStepExecutor> configuredExecutors = new ArrayList<>();

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public boolean isMultiOption() {

        return isMultiOption;
    }

    public void setMultiOption(boolean multiOption) {

        isMultiOption = multiOption;
    }

    public RegistrationFlowConstants.StepStatus getStatus() {

        return status;
    }

    public void setStatus(RegistrationFlowConstants.StepStatus status) {

        this.status = status;
    }

    public RegistrationStepExecutor getSelectedExecutor() {

        return selectedExecutor;
    }

    public void setSelectedExecutor(RegistrationStepExecutor selectedExecutor) {

        this.selectedExecutor = selectedExecutor;
    }

    public List<RegistrationStepExecutor> getConfiguredExecutors() {

        return configuredExecutors;
    }

    public void setConfiguredExecutors(List<RegistrationStepExecutor> configuredExecutors) {

        this.configuredExecutors = configuredExecutors;
    }
}
