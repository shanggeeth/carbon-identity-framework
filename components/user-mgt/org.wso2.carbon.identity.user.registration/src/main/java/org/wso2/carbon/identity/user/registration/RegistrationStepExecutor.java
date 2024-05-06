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

package org.wso2.carbon.identity.user.registration;

import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.response.NextStepResponse;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationConstants;
import org.wso2.carbon.identity.user.registration.util.RegistrationConstants.StepStatus;

import java.util.List;
import java.util.Map;

public interface RegistrationStepExecutor {

    String getName();

    RegistrationConstants.RegExecutorBindingType getBindingType();

    String getBoundIdentifier();

    String getExecutorType();

    StepStatus execute(Map<String, String> inputs, RegistrationContext context, NextStepResponse response,
                       RegistrationStepExecutorConfig config) throws RegistrationFrameworkException;

    List<RequiredParam> getRequiredParams();
}
