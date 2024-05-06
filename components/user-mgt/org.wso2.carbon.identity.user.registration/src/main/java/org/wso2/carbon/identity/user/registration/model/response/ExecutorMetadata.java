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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to hold the metadata of the executor.
 */
public class ExecutorMetadata {

    private String i18nKey;
    private RegistrationConstants.PromptType promptType;
    private List<RequiredParam> requiredParams;
    private Map<String, String> additionalData = new HashMap<>();

    public String getI18nKey() {

        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
    }

    public RegistrationConstants.PromptType getPromptType() {

        return promptType;
    }

    public void setPromptType(RegistrationConstants.PromptType promptType) {

        this.promptType = promptType;
    }

    public List<RequiredParam> getRequiredParams() {

        return requiredParams;
    }

    public void setRequiredParams(List<RequiredParam> requiredParams) {

        this.requiredParams = requiredParams;
    }

    public Map<String, String> getAdditionalData() {

        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {

        this.additionalData = additionalData;
    }
}
