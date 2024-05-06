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

/**
 * This class represents the required parameter in the registration flow.
 */
public class RequiredParam {

    private String name;
    private String availableValue;
    private RegistrationConstants.DataType dataType;
    private boolean isConfidential;
    private boolean isMandatory;
    private int order;
    private String validationRegex;
    private String i18nKey;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getAvailableValue() {

        return availableValue;
    }

    public void setAvailableValue(String availableValue) {

        this.availableValue = availableValue;
    }

    public RegistrationConstants.DataType getDataType() {

        return dataType;
    }

    public void setDataType(RegistrationConstants.DataType dataType) {

        this.dataType = dataType;
    }

    public boolean isConfidential() {

        return isConfidential;
    }

    public void setConfidential(boolean confidential) {

        isConfidential = confidential;
    }

    public boolean isMandatory() {

        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {

        isMandatory = mandatory;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public String getValidationRegex() {

        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {

        this.validationRegex = validationRegex;
    }

    public String getI18nKey() {

        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
    }
}
