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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RegistrationRequest implements Serializable {

    private static final long serialVersionUID = -8742948356919529312L;

    private String flowId;
    private String flowType;
    private String inputType;
    private Map<String, String> inputs = new HashMap<String, String>();

    public String getFlowId() {

        return flowId;
    }

    public void setFlowId(String flowId) {

        this.flowId = flowId;
    }

    public Map<String, String> getInputs() {

        return inputs;
    }

    public void setInputs(Map<String, String> inputs) {

        this.inputs = inputs;
    }

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public String getInputType() {

        return inputType;
    }

    public void setInputType(String inputType) {

        this.inputType = inputType;
    }
}
