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

package org.wso2.carbon.identity.user.self.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutionState;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.CombinedInputCollectorNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.TaskExecutorNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.AttributeCollector;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.EmailOTPExecutor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.PasswordOnboarder;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.model.RegistrationRequest;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.DefaultRegistrationSequenceHandler;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.RegistrationResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationFrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserRegistrationFlowServiceImpl implements UserRegistrationFlowService{

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowServiceImpl.class);
    private static final UserRegistrationFlowServiceImpl instance = new UserRegistrationFlowServiceImpl();

    public static UserRegistrationFlowServiceImpl getInstance() {

        return instance;
    }

    @Override
    public RegistrationResponse initiateUserRegistration(String appId,
                                                         String tenantDomain,
                                                         RegistrationConstants.SupportedProtocol type)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.initiateRegContext(appId, tenantDomain, type);

        RegistrationResponse response = RegistrationFrameworkUtils
                .getRegistrationSeqHandler(context.getRegistrationSequence()).handle(new RegistrationRequest(), context);
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return response;
    }

    @Override
    public RegistrationResponse initiateUserRegistration(HttpServletRequest request)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.initiateRegContext(request);
        RegistrationResponse response = RegistrationFrameworkUtils
                .getRegistrationSeqHandler(context.getRegistrationSequence()).handle(new RegistrationRequest(), context);
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return response;
    }

    @Override
    public RegistrationResponse processIntermediateUserRegistration(RegistrationRequest request)
            throws RegistrationFrameworkException {

        DefaultRegistrationSequenceHandler handler = DefaultRegistrationSequenceHandler.getInstance();
        RegistrationContext context = RegistrationFrameworkUtils.retrieveRegContextFromCache(request.getFlowId());
        if (context == null) {
            throw new RegistrationFrameworkException("Invalid flow id.");
        }
        RegistrationResponse response = handler.handle(request, context);
        if (context.isCompleted()) {
            LOG.debug("Registration flow completed for flow id: " + request.getFlowId() +
                    ". Hence clearing the cache.");
            RegistrationFrameworkUtils.removeRegContextFromCache(request.getFlowId());
        } else {
            LOG.debug("Registration flow is not completed for flow id: " + request.getFlowId() +
                    ". Hence updating the cache with the latest.");
            RegistrationFrameworkUtils.addRegContextToCache(context);
        }
        return response;
    }

    @Override
    public ExecutionState triggerRegFlow(String flowId, Map<String, InputData> inputs) throws RegistrationFrameworkException {

        RegistrationContext context;
        RegSequence sequence;
        if (flowId == null) {
            flowId = UUID.randomUUID().toString();
            context = new RegistrationContext();
            sequence = loadSequence();
            context.setRegSequence(sequence);
            context.setContextIdentifier(flowId);
        } else {
            context = RegistrationFrameworkUtils.retrieveRegContextFromCache(flowId);
            sequence = context.getRegSequence();
        }
        if (!validateInputs(inputs, context)) {
            throw new RegistrationFrameworkException("Invalid inputs provided.");
        }
        context.addUserInputs(inputs);
        NodeResponse response = sequence.execute(context);
        ExecutionState state = new ExecutionState();
        state.setFlowId(flowId);
        state.setResponse(response);
        // Save the current sequence in the context.

        RegistrationFrameworkUtils.addRegContextToCache(context);
        return state;
    }

    private RegSequence loadSequence() {

        AttributeCollector attrCollector1 = new AttributeCollector("AttributeCollector1");
        InputMetaData e1 = new InputMetaData("emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollector attrCollector2 = new AttributeCollector("AttributeCollector2");
        InputMetaData e2 = new InputMetaData("firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData("dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarder pwdOnboard = new PasswordOnboarder();
        EmailOTPExecutor emailOTPExecutor = new EmailOTPExecutor();

        TaskExecutorNode node1 = new TaskExecutorNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutorNode node3 = new TaskExecutorNode("node3", pwdOnboard);
        TaskExecutorNode node4 = new TaskExecutorNode("node4", emailOTPExecutor);
        TaskExecutorNode node5 = new TaskExecutorNode("node5", attrCollector2);

        CombinedInputCollectorNode node0 = new CombinedInputCollectorNode("node0");
        node0.addReferencedNode(node1);
        node0.addReferencedNode(node2);
        node0.addReferencedNode(node5);

        // Define the flow of the graph
        RegSequence regSequence = new RegSequence();

        regSequence.addNode(node0);
        regSequence.addNode(node1);
        regSequence.addNode(node2);
        regSequence.addNode(node3);
        regSequence.addNode(node4);
        regSequence.addNode(node5);

        regSequence.addNextNode("node0", node1);
        regSequence.addNextNode("node1", node2);
        regSequence.addNextNode("node2", node3);
        regSequence.addNextNode("node2", node4);
        regSequence.addNextNode("node3", node5);
        regSequence.addNextNode("node4", node5);

        return regSequence;
    }

    private boolean validateInputs(Map<String, InputData> inputs, RegistrationContext context) {

        if (context.getRequiredMetaData() == null) {
            return true;
        }
        if (context.getRequiredMetaData() != null && (inputs == null || inputs.isEmpty())) {
            return false;
        }

        // Is the following logic null safe?
        for (Map.Entry<String, List<InputMetaData>> entry : context.getRequiredMetaData().entrySet()) {

            if (inputs.get(entry.getKey()) == null) {
                return false;
            }

            Map<String, String> inputForNode = inputs.get(entry.getKey()).getUserInput();

            for (InputMetaData metaData : entry.getValue()) {

                // Return false if the input is mandatory and not provided.
                if (metaData.isMandatory() && (inputForNode == null || inputForNode.get(metaData.getName()) == null)) {
                    return false;
                }

                // Return false if the regex validation fails.
                if (metaData.getValidationRegex() != null && inputForNode != null &&
                        !inputForNode.get(metaData.getName()).matches(metaData.getValidationRegex())) {
                    return false;
                }

                // Return false if the given option is not in the list of provided options.
                List<Object> providedOptions = metaData.getOptions();
                if (providedOptions != null && !providedOptions.isEmpty() && inputForNode != null &&
                        !providedOptions.contains(inputForNode.get(metaData.getName()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void filterRequiredData() {

    }
}
