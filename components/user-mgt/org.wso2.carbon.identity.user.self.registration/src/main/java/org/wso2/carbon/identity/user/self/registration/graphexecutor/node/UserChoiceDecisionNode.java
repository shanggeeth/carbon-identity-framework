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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegOption;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_CHOICE_REQUIRED;

// node.UserChoiceDecisionNode class implementation
public class UserChoiceDecisionNode implements Node {

    private String name;
    private final List<Node> nextNodes; // For branching paths
    private Node nextNode; // For selected path

    // Define a constant for decision options.
    private static final String USER_CHOICE = "user-choice";

    public UserChoiceDecisionNode(String name) {
        this.name = name;
        this.nextNodes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setNextNode(Node nextNode) {
        nextNodes.add(nextNode);
    }

    public List<Node> getNextNodes() {
        return nextNodes;
    }

    public Node getNextNode() {
        return nextNode;
    }

    @Override
    public NodeResponse execute(InputData inputData, RegistrationContext context) {
        // Decision logic
        boolean nodeSelected = false;

        if (inputData != null && inputData.getUserInput() != null && !inputData.getUserInput().isEmpty()) {
            for (Node nextNode : nextNodes) {
                if (nextNode instanceof TaskExecutorNode) {
                    String executorName = ((TaskExecutorNode) nextNode).getExecutor().getName();
                    if (inputData.getUserInput().get(USER_CHOICE).equals(executorName)) {
                        this.nextNode = nextNode;
                        nodeSelected = true;
                        break;
                    }
                }
            }
        }
        if (nodeSelected) {
            return new NodeResponse(Constants.STATUS_NODE_COMPLETE);
        } else {
            NodeResponse response = new NodeResponse(STATUS_USER_CHOICE_REQUIRED);
            response.addInputData(name, getUserChoices());
            return response;
        }
    }

    public List<InputMetaData> getUserChoices() {

        if (nextNode != null) {
            return null;
        }

        InputMetaData meta = new InputMetaData(USER_CHOICE, "multiple-options", 1);
        meta.setMandatory(true);
        meta.setI18nKey("user.choice");
        for (Node nextNode : nextNodes) {
            if (nextNode instanceof TaskExecutorNode) {
                meta.addOption(((TaskExecutorNode) nextNode).getExecutor().getName());
            }
        }
        List<InputMetaData> input = new ArrayList<>();
        input.add(meta);
        return input;
    }
}
