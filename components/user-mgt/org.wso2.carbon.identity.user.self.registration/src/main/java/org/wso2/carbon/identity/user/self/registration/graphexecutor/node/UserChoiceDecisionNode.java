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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.node;

import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.util.Constants;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.InputData;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Implementation of a node specific to prompting user to select a choice out of multiple registration executor options.
 */
public class UserChoiceDecisionNode extends AbstractNode implements InputCollectionNode {

    private List<Node> nextNodes = new ArrayList<>(); // For branching paths
    private static final String USER_CHOICE = "user-choice";

    public UserChoiceDecisionNode() {

        super();
    }

    /**
     * Set the nodes that are available for the user to choose from.
     *
     * @param nextNodes List of Task Executor Nodes.
     */
    public void setNextNodes(List<Node> nextNodes) {

        this.nextNodes = nextNodes;
    }

    /**
     * Get the nodes that are available for the user to choose from.
     *
     * @return List of Task Executor Nodes.
     */
    public List<Node> getNextNodes() {

        return this.nextNodes;
    }

    /**
     * Add a node to the list of nodes available for the user to choose from.
     *
     * @param node Task Executor Node.
     */
    public void addNextNode(TaskExecutionNode node) {

        this.nextNodes.add(node);
    }

    @Override
    public NodeResponse execute(RegistrationContext context) {

        Map<String, String> inputData = context.getUserInputData();

        if (inputData != null && inputData.containsKey(USER_CHOICE)) {
            for (Node nextNode : nextNodes) {
                if (nextNode instanceof TaskExecutionNode) {
                    Executor executor = ((TaskExecutionNode) nextNode).getExecutor();
                    if (executor != null) {
                        String executorName = executor.getName();
                        if (inputData.get(USER_CHOICE).equals(executorName)) {
                            setNextNode(nextNode);
                            inputData.remove(USER_CHOICE);
                            break;
                        }
                    }
                }
            }
        }
        if (getNextNode() != null) {
            return new NodeResponse(Constants.STATUS_NODE_COMPLETE);
        }
        NodeResponse response = new NodeResponse(STATUS_USER_INPUT_REQUIRED);
        response.addInputMetaData(getRequiredData());
        return response;
    }

    @Override
    public List<InputMetaData> getRequiredData() {

        if (getNextNode() != null) {
            return null;
        }

        InputMetaData meta = new InputMetaData(USER_CHOICE, USER_CHOICE, "multiple-options", 1);
        meta.setMandatory(true);
        meta.setI18nKey("user.choice");
        for (Node nextNode : nextNodes) {
            if (nextNode instanceof TaskExecutionNode) {
                meta.addOption(((TaskExecutionNode)nextNode).getExecutor().getName());
            } else {
                meta.addOption(nextNode.getNodeId());
            }
        }
        List<InputMetaData> inputMetaList = new ArrayList<>();
        inputMetaList.add(meta);
        return inputMetaList;
    }
}
