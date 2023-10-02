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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.config.RegistrationStep;
import org.wso2.carbon.identity.user.registration.model.response.CurrentStepResponse;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;

import java.util.ArrayList;
import java.util.List;

public class DefaultRegistrationStepHandler implements RegistrationStepHandler {

    private static final Log LOG = LogFactory.getLog(DefaultRegistrationStepHandler.class);
    private static DefaultRegistrationStepHandler instance = new DefaultRegistrationStepHandler();

    public static DefaultRegistrationStepHandler getInstance() {

        return instance;
    }

    public static void setInstance(DefaultRegistrationStepHandler instance) {

        DefaultRegistrationStepHandler.instance = instance;
    }

    @Override
    public CurrentStepResponse handle(RegistrationRequest request, RegistrationContext context) throws RegistrationFrameworkException {

        RegistrationStep step = context.getRegistrationSequence().getStepMap().get(context.getCurrentStep());

        CurrentStepResponse stepResponse = new CurrentStepResponse();
        List<ExecutorResponse> executorResponses = new ArrayList<>();

        if (step.getSelectedExecutor() == null) {
            List<RegistrationStepExecutorConfig> regExecutors = step.getConfiguredExecutors();
            if (regExecutors.size() == 1) {
                stepResponse.setType(RegistrationFlowConstants.StepType.SINGLE_OPTION);
                step.setSelectedExecutor(regExecutors.get(0));
            } else {
                // Handling multi option steps.
                if (RegistrationFlowConstants.StepStatus.SELECTION_PENDING.equals(step.getStatus())) {
                    // It is expected to an executor to be selected in order to continue the registration.
                    RegistrationStepExecutorConfig selectedExecutor = resolveSelectedExecutor(regExecutors, request);
                    step.setSelectedExecutor(selectedExecutor);
                    // Once an executor is selected, it can be considered as a single option and start the flow.
                    stepResponse.setType(RegistrationFlowConstants.StepType.SINGLE_OPTION);
                    step.setStatus(RegistrationFlowConstants.StepStatus.INCOMPLETE);
                } else {
                    step.setStatus(RegistrationFlowConstants.StepStatus.SELECTION_PENDING);
                    stepResponse.setType(RegistrationFlowConstants.StepType.MULTI_OPTION);
                    stepResponse.setExecutors(loadMultiOptionsForStep(regExecutors));
                    // Further processing of the step is not possible without selecting an executor.
                    return stepResponse;
                }
            }
        }

        RegistrationStepExecutorConfig regExecutor = step.getSelectedExecutor();
        ExecutorResponse executorResponse;

        // If the step is not expecting any user inputs, there's no need to pass the registration request.
        if (!RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED.equals(step.getStatus())) {
            executorResponse = regExecutor.getExecutor().execute(null, context, regExecutor);
        } else {
            executorResponse = regExecutor.getExecutor().execute(request, context, regExecutor);
        }

        if (executorResponse != null && executorResponse.getStatus() != null ) {
            step.setStatus(executorResponse.getStatus());
            if (!RegistrationFlowConstants.StepStatus.COMPLETE.equals(executorResponse.getStatus())){
                executorResponses.add(executorResponse);
            }
        } else {
            throw new RegistrationFrameworkException("Registration step executor did not handle the request properly");
        }

        stepResponse.setExecutors(executorResponses);
        return stepResponse;
    }

    private RegistrationStepExecutorConfig resolveSelectedExecutor(List<RegistrationStepExecutorConfig> regExecutors,
                                                             RegistrationRequest request)
            throws RegistrationFrameworkException {

        if (!"SELECTION".equals(request.getInputType())) {
            throw new RegistrationFrameworkException("Registration option selection is expected");
        }
        String selectedExecutorName = request.getInputs().get("executorName");
        RegistrationStepExecutorConfig selectedExecutor = null;
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            if (regExecutor.getGivenName().equals(selectedExecutorName)) {
                selectedExecutor = regExecutor;
            }
        }
        if (selectedExecutor == null) {
            throw new RegistrationFrameworkException("No valid executor found");
        }
        return selectedExecutor;
    }

    private List<ExecutorResponse> loadMultiOptionsForStep(List<RegistrationStepExecutorConfig> regExecutors) {

        List<ExecutorResponse> executorResponses = new ArrayList<>();
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            ExecutorResponse executorResponse = new ExecutorResponse();
            executorResponse.setGivenName(regExecutor.getGivenName());
            executorResponse.setName(regExecutor.getName());
            executorResponse.setId(regExecutor.getGivenName());
            executorResponses.add(executorResponse);
        }
        return executorResponses;
    }
}
