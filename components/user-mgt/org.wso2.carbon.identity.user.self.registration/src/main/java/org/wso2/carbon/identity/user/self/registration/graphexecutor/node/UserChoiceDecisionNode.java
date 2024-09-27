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
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

// node.UserChoiceDecisionNode class implementation
public class UserChoiceDecisionNode extends AbstractNode implements InputCollectionNode {

    private List<TaskExecutionNode> nextNodes = new ArrayList<>(); // For branching paths
    private static final String USER_CHOICE = "user-choice";

    public UserChoiceDecisionNode(String name) {

        setName(name);
    }

    public void setNextNodes(List<TaskExecutionNode> nextNodes) {

        this.nextNodes = nextNodes;
    }

    public void addNextNode(TaskExecutionNode node) {

        this.nextNodes.add(node);
    }

    @Override
    public NodeResponse execute(InputData inputData, RegistrationContext context) {

        if (inputData != null && inputData.getUserInput() != null && !inputData.getUserInput().isEmpty()) {
            for (TaskExecutionNode nextNode : nextNodes) {
                Executor executor = nextNode.getExecutor();
                if (executor != null) {
                    String executorName = executor.getName();
                    if (inputData.getUserInput().get(USER_CHOICE).equals(executorName)) {
                        setNextNode(nextNode);
                        break;
                    }
                }
            }
        }
        if (getNextNode() != null) {
            return new NodeResponse(Constants.STATUS_NODE_COMPLETE);
        } else {
            NodeResponse response = new NodeResponse(STATUS_USER_INPUT_REQUIRED);
            response.addInputData(this.getName(), getRequiredData());
            return response;
        }
    }

    @Override
    public List<InputMetaData> getRequiredData() {

        if (getNextNode() != null) {
            return null;
        }

        InputMetaData meta = new InputMetaData(USER_CHOICE, "multiple-options", 1);
        meta.setMandatory(true);
        meta.setI18nKey("user.choice");
        for (TaskExecutionNode nextNode : nextNodes) {
            meta.addOption(nextNode.getExecutor().getName());
        }
        List<InputMetaData> input = new ArrayList<>();
        input.add(meta);
        return input;
    }
}
