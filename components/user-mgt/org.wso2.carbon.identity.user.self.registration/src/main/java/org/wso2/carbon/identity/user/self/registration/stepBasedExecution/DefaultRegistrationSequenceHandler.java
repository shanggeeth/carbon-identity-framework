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

package org.wso2.carbon.identity.user.self.registration.stepBasedExecution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.model.RegistrationRequest;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.NextStepResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.RegistrationResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config.RegistrationStep;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationFrameworkUtils;

import java.util.Optional;

public class DefaultRegistrationSequenceHandler implements RegistrationSequenceHandler{

    private static DefaultRegistrationSequenceHandler instance = new DefaultRegistrationSequenceHandler();
    private static final Log LOG = LogFactory.getLog(DefaultRegistrationSequenceHandler.class);

    private DefaultRegistrationSequenceHandler() {

    }

    public static DefaultRegistrationSequenceHandler getInstance() {

        return instance;
    }

    @Override
    public RegistrationResponse handle(RegistrationRequest request, RegistrationContext context) throws RegistrationFrameworkException {

        RegistrationResponse response =  new RegistrationResponse();
        response.setFlowId(context.getContextIdentifier());
        while (!context.getRegistrationSequence().isCompleted()) {

            int currentStep = context.getCurrentStep();

            RegistrationStep stepConfig= context.getRegistrationSequence().getStepDefinitions().get(currentStep);

            // If the current step is completed.
            if (stepConfig != null && RegistrationConstants.StepStatus.COMPLETE.equals(context.getCurrentStepStatus())) {

                // Check whether there are more steps to execute.
                if (context.getRegistrationSequence().getStepDefinitions().size() == currentStep + 1) {
                    context.getRegistrationSequence().setCompleted(true);
                    continue;
                }
                currentStep = context.getCurrentStep() + 1;
                context.setCurrentStep(currentStep);
            }

            RegistrationStepHandler registrationStepHandler = DefaultRegistrationStepHandler.getInstance();
            NextStepResponse response1 = registrationStepHandler.handle(request, context);

            // If step is not completed, that means step wants to redirect to outside.
            if (!RegistrationConstants.StepStatus.COMPLETE.equals(context.getCurrentStepStatus())) {
                response.setStatus(RegistrationConstants.Status.INCOMPLETE);
                response.setNextStep(response1);
                return response;
            }
        }

        if (context.getRegistrationSequence().isCompleted()) {
            LOG.info("The registration is completed. Can trigger post registration steps.");
            String userId = RegistrationFrameworkUtils.createUser(context.getRegisteringUser(), context.getTenantDomain());
            context.setCompleted(true);
            response.setStatus(RegistrationConstants.Status.COMPLETE);
            Optional<String> encodedUserAssertion = RegistrationFrameworkUtils.getSignedUserAssertion(userId, context);
            encodedUserAssertion.ifPresent(response::setUserAssertion);
        }
        return response;
    }
}
