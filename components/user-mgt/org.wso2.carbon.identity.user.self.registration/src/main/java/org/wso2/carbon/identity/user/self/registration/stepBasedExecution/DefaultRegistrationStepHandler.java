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
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config.RegistrationStep;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config.RegistrationStepExecutorConfig;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.model.EngagedExecutor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.model.RegistrationRequest;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.Message;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.NextStepResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.RegExecutorBindingType.AUTHENTICATOR;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepStatus.AGGREGATED_TASKS_PENDING;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepStatus.COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepStatus.NOT_STARTED;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepStatus.SELECTION_PENDING;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepStatus.USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepType.AGGREGATED_TASKS;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepType.MULTI_OPTION;
import static org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants.StepType.SINGLE_OPTION;

public class DefaultRegistrationStepHandler implements RegistrationStepHandler {

    private static final Log LOG = LogFactory.getLog(DefaultRegistrationStepHandler.class);
    private static DefaultRegistrationStepHandler instance = new DefaultRegistrationStepHandler();

    public static DefaultRegistrationStepHandler getInstance() {

        return instance;
    }

    @Override
    public NextStepResponse handle(RegistrationRequest request, RegistrationContext context) throws RegistrationFrameworkException {

        RegistrationStep step = context.getRegistrationSequence().getStepDefinitions().get(context.getCurrentStep());
        List<RegistrationStepExecutorConfig> regExecutorConfigs = step.getConfiguredExecutors();

        NextStepResponse stepResponse = new NextStepResponse();
        stepResponse.setType(step.getType());

        RegistrationConstants.StepStatus status = context.getCurrentStepStatus();

        if (status == null) {
            context.setCurrentStepStatus(NOT_STARTED);
        }

        if (RegistrationConstants.StepType.MULTI_OPTION.equals(step.getType())) {
            // Handling multi option steps.
            if (SELECTION_PENDING.equals(context.getCurrentStepStatus())) {
                // It is expected to an executor to be selected in order to continue the registration.
                if (!(request.getEngagedExecutors().size() == 1)) {
                    throw new RegistrationFrameworkException("Only one registration executor can be selected.");
                }
                EngagedExecutor executor = request.getEngagedExecutors().get(0);
                RegistrationStepExecutorConfig selectedExecutor = resolveSelectedExecutor(executor.getId(),
                                                                                          regExecutorConfigs);
                step.setSelectedExecutor(selectedExecutor);
                // Once an executor is selected, it can be considered as a single option and start the flow.
                step.setType(SINGLE_OPTION);
                context.setCurrentStepStatus(NOT_STARTED);
//                callRegistrationStepExecutor(context, executor.getInputs(), step.getSelectedExecutor(), stepResponse);
            } else {
                loadMultiOptionsForStep(context, stepResponse, regExecutorConfigs);
                Message message = new Message(RegistrationConstants.MessageType.INFO,
                                              "Select an option to proceed the registration.");
                context.setCurrentStepStatus(SELECTION_PENDING);
                stepResponse.setType(MULTI_OPTION);
                stepResponse.setMessage(message);
                // Further processing of the step is not possible without selecting an executor.
                return stepResponse;
            }
        }

        if (SINGLE_OPTION.equals(step.getType())) {
            if (step.getSelectedExecutor() == null && step.getConfiguredExecutors().size() == 1) {
                step.setSelectedExecutor(step.getConfiguredExecutors().get(0));
            }

            if (!(request.getEngagedExecutors().size() == 1)) {
                throw new RegistrationFrameworkException("Only one registration executor is expected.");
            }
            EngagedExecutor executor = request.getEngagedExecutors().get(0);
            callRegistrationStepExecutor(context, executor.getInputs(), step.getSelectedExecutor(), stepResponse);
        }

        if (RegistrationConstants.StepType.AGGREGATED_TASKS.equals(step.getType())) {

            // Handling aggregated.
            if (AGGREGATED_TASKS_PENDING.equals(context.getCurrentStepStatus())) {
                validateUserInputs(context, request, regExecutorConfigs,stepResponse);
            } else {
                loadMultiOptionsForStep(context, stepResponse, regExecutorConfigs);
                stepResponse.setType(AGGREGATED_TASKS);
                context.setCurrentStepStatus(AGGREGATED_TASKS_PENDING);

                Message message = new Message(RegistrationConstants.MessageType.INFO,
                                              "This is an aggregated step. One or more executors must be engaged.");
                stepResponse.setMessage(message);
                // Further processing of the step is not possible without selecting an executor.
                return stepResponse;
            }
        }
        return stepResponse;
    }

    private void validateUserInputs(RegistrationContext context, RegistrationRequest request,
                                    List<RegistrationStepExecutorConfig> regExecutors, NextStepResponse response)
            throws RegistrationFrameworkException {

        int selectedAuthenticatorCount = 0;
        boolean multipleAuthenticatorsListed = Boolean.parseBoolean(
                String.valueOf(context.getProperty("multipleAuthenticators")));
        
        for (EngagedExecutor engagedExecutor : request.getEngagedExecutors()) {
            context.setCurrentStepStatus(USER_INPUT_REQUIRED);
            RegistrationStepExecutorConfig executorConfig = resolveSelectedExecutor(engagedExecutor.getId(), regExecutors);
            if (executorConfig.getExecutor().getBindingType() == AUTHENTICATOR) {
                selectedAuthenticatorCount++;
                if (selectedAuthenticatorCount > 1) {
                    throw new RegistrationFrameworkException("Only one authenticator should be selected.");
                }
                if (multipleAuthenticatorsListed) {
                    context.setCurrentStepStatus(NOT_STARTED);
                }
            }
            callRegistrationStepExecutor(context, engagedExecutor.getInputs(), executorConfig, response);
        }

        if (multipleAuthenticatorsListed && selectedAuthenticatorCount == 0) {
            throw new RegistrationFrameworkException("At least one authenticator should be selected.");
        }
    }

    private void callRegistrationStepExecutor(RegistrationContext context, Map<String, String> inputs,
                                              RegistrationStepExecutorConfig regExecutor, NextStepResponse stepResponse)
            throws RegistrationFrameworkException {

        if (regExecutor != null && regExecutor.getExecutor().getBindingType() == AUTHENTICATOR) {

            ExternalIdPConfig externalIdPConfig;
            try {
                externalIdPConfig = ConfigurationFacade.getInstance()
                        .getIdPConfigByName(regExecutor.getName(), context.getTenantDomain());
            } catch (IdentityProviderManagementException e) {
                throw new RegistrationFrameworkException("Error while retrieving IdP configurations", e);
            }
            regExecutor.setAuthenticatorProperties(FrameworkUtils.getAuthenticatorPropertyMapFromIdP(
                    externalIdPConfig, regExecutor.getExecutor().getBoundIdentifier()));
        }

        RegistrationConstants.StepStatus stepStatus = regExecutor.getExecutor()
                .execute(inputs, context, stepResponse, regExecutor);
        // If the step is completed and an authenticator is involved, add it to the engaged authenticators list.
        if (stepStatus == COMPLETE) {
            if (AUTHENTICATOR == regExecutor.getExecutor().getBindingType()) {
                context.addEngagedStepAuthenticator(regExecutor.getExecutor().getBoundIdentifier());
            }
        }
        context.setCurrentStepStatus(stepStatus);
    }

    private RegistrationStepExecutorConfig resolveSelectedExecutor(String id, List<RegistrationStepExecutorConfig> regExecutors)
            throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig selectedExecutor = null;
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            if (regExecutor.getId().equals(id)) {
                selectedExecutor = regExecutor;
            }
        }
        if (selectedExecutor == null) {
            throw new RegistrationFrameworkException("No valid executor found.");
        }
        return selectedExecutor;
    }


    private void loadMultiOptionsForStep(RegistrationContext context, NextStepResponse stepResponse,
                                                           List<RegistrationStepExecutorConfig> regExecutors)
            throws RegistrationFrameworkException {

        int authenticatorCount = 0;
        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
           if (AUTHENTICATOR.equals(regExecutor.getExecutor().getBindingType())) {
                authenticatorCount++;
            }
            if (authenticatorCount > 1) {
                context.setProperty("multipleAuthenticators", true);
                break; // Exit the loop if authenticatorCount is greater than 1
            }
        }

        for (RegistrationStepExecutorConfig regExecutor : regExecutors) {
            context.setCurrentStepStatus(NOT_STARTED);
            if (!AUTHENTICATOR.equals(regExecutor.getExecutor().getBindingType())) {
                callRegistrationStepExecutor(context, null, regExecutor, stepResponse);
            } else if (authenticatorCount == 1) {
                callRegistrationStepExecutor(context, null, regExecutor, stepResponse);
            } else {
                ExecutorResponse executorResponse = new ExecutorResponse();
                executorResponse.setName(regExecutor.getName());
                executorResponse.setType(regExecutor.getExecutor().getExecutorType());
                executorResponse.setId(regExecutor.getId());
                stepResponse.addExecutor(executorResponse);            }
        }
    }
}
