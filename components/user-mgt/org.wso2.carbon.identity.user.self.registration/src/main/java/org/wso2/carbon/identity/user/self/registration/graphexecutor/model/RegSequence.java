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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.model;/*
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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;

import java.util.ArrayList;
import java.util.List;

public class RegSequence {

    private List<Node> nodes; // Keep track of all nodes
    private ExecutionState currentState;

    public RegSequence() {
        this.nodes = new ArrayList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addNextNode(String nodeName, Node nextNode) {
        Node currentNode = findNode(nodeName);
        if (currentNode != null) {
            currentNode.setNextNode(nextNode);
        }
    }

    private Node findNode(String nodeName) {
        for (Node node : nodes) {
            if (node.getName().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }

    public ExecutionState execute(List<InputData> inputDataList) {

        Node currentNode;
        if (nodes.isEmpty()) {
            return null;
        }
        if (currentState == null) {
            currentState = new ExecutionState();
            currentNode = nodes.get(0);
        } else {
            // Resume from the current state
            currentNode = currentState.getCurrentNode();
        }

        while (currentNode != null) {

            InputData data = null;

            // Find matching InputData for the current node.
            if (inputDataList != null) {
                for (InputData inputData : inputDataList) {
                    if (inputData.getNodeName().equals(currentNode.getName())) {
                        data = inputData;
                        break;
                    }
                }
            }

            NodeResponse result = currentNode.execute(data);

            if (!Constants.STATUS_COMPLETE.equals(result.getStatus())) {
                // Input is required
                currentState.setCurrentNode(currentNode);
                currentState.setResponse(result);
                return currentState;
            }

            if (Constants.STATUS_COMPLETE.equals(result.getStatus()) &&
                    result.getInputDataList() != null && !result.getInputDataList().isEmpty()) {
                // Input is required but the node execution is complete.
                currentNode = currentNode.getNextNode();
                result.setStatus(Constants.STATUS_USER_INPUT_REQUIRED);
                currentState.setCurrentNode(currentNode);
                currentState.setResponse(result);
                return currentState;
            }
            currentNode = currentNode.getNextNode();
        }

        // Clear the state if we reach the end of the execution
        String flowId = currentState.getFlowId();
        currentState = null;
        NodeResponse response = new NodeResponse(Constants.STATUS_COMPLETE);
        ExecutionState finalState = new ExecutionState();
        finalState.setFlowId(flowId);
        finalState.setResponse(response);
        return finalState; // Execution completed
    }
}
