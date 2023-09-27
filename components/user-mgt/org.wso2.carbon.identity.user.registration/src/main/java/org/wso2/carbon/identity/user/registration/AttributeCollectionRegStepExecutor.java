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

import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeCollectionRegStepExecutor implements RegistrationStepExecutor {

    private static AttributeCollectionRegStepExecutor instance = new AttributeCollectionRegStepExecutor();

    public static AttributeCollectionRegStepExecutor getInstance() {

        return instance;
    }

    @Override
    public String getName() {

        return "AttributeCollector";
    }

    @Override
    public RegistrationFlowConstants.RegistrationExecutorBindingType getBindingType() throws RegistrationFrameworkException {

        return RegistrationFlowConstants.RegistrationExecutorBindingType.NONE;
    }

    @Override
    public String getBoundIdentifier() throws RegistrationFrameworkException {

        return null;
    }

    @Override
    public String getExecutorType() throws RegistrationFrameworkException {

        return "AttributeCollectorType";
    }

    @Override
    public ExecutorResponse execute(RegistrationRequest registrationRequest, RegistrationContext context)
            throws RegistrationFrameworkException {

        ExecutorResponse response = new ExecutorResponse();
        response.setName(this.getName());
        response.setId("someIdForAttributeCollectorInstance");
        response.setType(this.getExecutorType());

        if ( registrationRequest == null || registrationRequest.getInputs() == null ) {

            context.getRegistrationSequence().getStepMap().get(context.getCurrentStep()).setStatus(
                    RegistrationFlowConstants.StepStatus.INCOMPLETE);

            List<RequiredParam> params = new ArrayList<>();
            RequiredParam param1 = new RequiredParam();
            param1.setName("username");
            param1.setConfidential(false);
            param1.setMandatory(true);
            params.add(param1);

            RequiredParam param2 = new RequiredParam();
            param2.setName("firstname");
            param2.setConfidential(false);
            param2.setMandatory(true);
            params.add(param2);

            response.setStatus(RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED);
            response.setRequiredParams(params);
        } else if (registrationRequest.getInputs() != null) {

            Map<String, String> inputs = registrationRequest.getInputs();
            RegistrationRequestedUser user =  context.getRegisteringUser();
            if (user == null) {
                user = new RegistrationRequestedUser();
                context.setRegisteringUser(user);
            }

            user.setUsername(inputs.get("username"));
            user.setClaims(inputs);
            response.setStatus(RegistrationFlowConstants.StepStatus.COMPLETE);
        }
        return response;
    }
}
