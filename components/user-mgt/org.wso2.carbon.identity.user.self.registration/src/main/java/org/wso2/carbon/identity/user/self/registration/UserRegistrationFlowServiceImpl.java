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
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutionState;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.CombinedInputCollectionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.TaskExecutionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.AttributeCollector;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.EmailOTPExecutor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.PasswordOnboarder;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.AuthBasedSequenceLoader;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.model.RegistrationRequest;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.DefaultRegistrationSequenceHandler;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response.RegistrationResponse;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.RegistrationFrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.ErrorMessages.ERROR_SEQUENCE_NOT_DEFINED_FOR_APP;

public class UserRegistrationFlowServiceImpl implements UserRegistrationFlowService {

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowServiceImpl.class);
    private static final UserRegistrationFlowServiceImpl instance = new UserRegistrationFlowServiceImpl();
    private static final Node END_NODE = null;

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
                .getRegistrationSeqHandler(context.getRegistrationSequence())
                .handle(new RegistrationRequest(), context);
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return response;
    }

    @Override
    public RegistrationResponse initiateUserRegistration(HttpServletRequest request)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.initiateRegContext(request);
        RegistrationResponse response = RegistrationFrameworkUtils
                .getRegistrationSeqHandler(context.getRegistrationSequence())
                .handle(new RegistrationRequest(), context);
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
    public ExecutionState initiateRegFlow(String appId) throws RegistrationFrameworkException {

        String flowId = UUID.randomUUID().toString();
        RegistrationContext context = new RegistrationContext();
        RegSequence sequence = loadSequence(appId);
        context.setRegSequence(sequence);
        context.setContextIdentifier(flowId);

        if (sequence.getFirstNode() == null) {
            throw new RegistrationServerException(ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getCode(),
                                                  ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getMessage(),
                                                  String.format(ERROR_SEQUENCE_NOT_DEFINED_FOR_APP.getDescription(),
                                                                appId));
        }

        NodeResponse response = sequence.execute(context);
        ExecutionState state = new ExecutionState();
        state.setFlowId(flowId);
        state.setResponse(response);
        // Save the current sequence in the context.
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return state;
    }

    @Override
    public ExecutionState triggerRegFlow(String flowId, Map<String, InputData> inputs)
            throws RegistrationFrameworkException {

        RegistrationContext context;
        RegSequence sequence;
        context = RegistrationFrameworkUtils.retrieveRegContextFromCache(flowId);
        sequence = context.getRegSequence();
        if (!validateInputs(inputs, context)) {
            throw new RegistrationFrameworkException("Invalid inputs provided.");
        }
        context.updateRequiredDataWithInputs(inputs);
        NodeResponse response = sequence.execute(context);
        ExecutionState state = new ExecutionState();
        state.setFlowId(flowId);
        state.setResponse(response);
        // Save the current sequence in the context.
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return state;
    }

    private RegSequence loadSequence(String appId) {

        if ("case1".equals(appId)) {
            return loadSequence1();
        } else if ("case2".equals(appId)) {
            return loadSequence2();
        }
        try {
            return new AuthBasedSequenceLoader().deriveRegistrationSequence(appId);
        } catch (RegistrationFrameworkException e) {
            LOG.error("Error while loading the sequence for the app: " + appId, e);
            return null;
        }
    }

    private RegSequence loadSequence1() {

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

        TaskExecutionNode node1 = new TaskExecutionNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutionNode node3 = new TaskExecutionNode("node3", pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode("node4", emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode("node5", attrCollector2);

        node1.setNextNode(node2);
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNode(node5);
        node4.setNextNode(node5);

        // Define the flow of the graph
        return new RegSequence(node1);
    }

    private RegSequence loadSequence2() {

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

        TaskExecutionNode node1 = new TaskExecutionNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutionNode node3 = new TaskExecutionNode("node3", pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode("node4", emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode("node5", attrCollector2);

        CombinedInputCollectionNode node0 = new CombinedInputCollectionNode("node0");
        node0.setReferencedNodes(new ArrayList<>(Arrays.asList(node1, node2, node5)));

        node0.setNextNode(node1);
        node1.setNextNode(node2);
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNode(node5);
        node4.setNextNode(node5);

        return new RegSequence(node0);
    }

    private boolean validateInputs(Map<String, InputData> inputs, RegistrationContext context) {

        if (context.getRequiredMetaData() == null) {
            return true;
        }
        if (context.getRequiredMetaData() != null && (inputs == null || inputs.isEmpty())) {
            return false;
        }

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
