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
import org.wso2.carbon.identity.user.registration.config.RegistrationStep;
import org.wso2.carbon.identity.user.registration.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorMetadata;
import org.wso2.carbon.identity.user.registration.model.response.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.model.response.NextStepResponse;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFrameworkUtils;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.NOT_STARTED;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.SELECTION_PENDING;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepType.MULTI_OPTION;
import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepType.SINGLE_OPTION;

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
    public NextStepResponse handle(RegistrationRequest request, RegistrationContext context) throws RegistrationFrameworkException {

        RegistrationStep step = context.getRegistrationSequence().getStepMap().get(context.getCurrentStep());

        NextStepResponse stepResponse = new NextStepResponse();
        stepResponse.setType(SINGLE_OPTION);

        if (step.getSelectedExecutor() == null) {
            List<RegistrationStepExecutorConfig> regExecutors = step.getConfiguredExecutors();
            if (regExecutors.size() == 1) {
                step.setSelectedExecutor(regExecutors.get(0));
            } else {
                // Handling multi option steps.
                if (SELECTION_PENDING.equals(step.getStatus())) {
                    // It is expected to an executor to be selected in order to continue the registration.
                    RegistrationStepExecutorConfig selectedExecutor = resolveSelectedExecutor(regExecutors, request);
                    step.setSelectedExecutor(selectedExecutor);
                    // Once an executor is selected, it can be considered as a single option and start the flow.
                    stepResponse.setType(SINGLE_OPTION);
                    step.setStatus(NOT_STARTED);
                } else {
                    step.setStatus(SELECTION_PENDING);
                    stepResponse.setType(MULTI_OPTION);
                    stepResponse.setExecutors(loadMultiOptionsForStep(context, regExecutors));
                    // Further processing of the step is not possible without selecting an executor.
                    return stepResponse;
                }
            }
        }

        RegistrationStepExecutorConfig regExecutor = step.getSelectedExecutor();

        step.setStatus(regExecutor.getExecutor().execute(request, context, stepResponse, regExecutor));
        return stepResponse;
    }

    private RegistrationStepExecutorConfig resolveSelectedExecutor(List<RegistrationStepExecutorConfig> regExecutors,
                                                                   RegistrationRequest request) throws RegistrationFrameworkException {

        String selectedExecutorId = request.getExecutorId();
        RegistrationStepExecutorConfig selectedExecutor = null;
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            if (regExecutor.getId().equals(selectedExecutorId)) {
                selectedExecutor = regExecutor;
            }
        }
        if (selectedExecutor == null) {
            throw new RegistrationFrameworkException("No valid executor found");
        }
        return selectedExecutor;
    }

    private List<ExecutorResponse> loadMultiOptionsForStep(RegistrationContext context,
                                                           List<RegistrationStepExecutorConfig> regExecutors) {

        List<ExecutorResponse> executorResponses = new ArrayList<>();
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            ExecutorResponse executorResponse = new ExecutorResponse();
            executorResponse.setName(regExecutor.getName());
            executorResponse.setExecutorName(regExecutor.getName());
            executorResponse.setId(regExecutor.getId());
            executorResponses.add(executorResponse);

            ExecutorMetadata meta = new ExecutorMetadata();
            meta.setI18nKey("executor." + regExecutor.getName());

            List<RequiredParam> params = regExecutor.getExecutor().getRequiredParams();
            RegistrationFrameworkUtils.updateAvailableValuesForRequiredParams(context, params);

            meta.setRequiredParams(params);
            executorResponse.setMetadata(meta);
        }
        return executorResponses;
    }
}
