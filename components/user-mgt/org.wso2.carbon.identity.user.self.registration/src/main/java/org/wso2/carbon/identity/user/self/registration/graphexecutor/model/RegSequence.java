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

import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_FLOW_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

public class RegSequence {

    private final Node firstNode; // Keep track of all nodes

    public RegSequence(Node node) {

        firstNode = node;
    }


    public NodeResponse execute(RegistrationContext context)
            throws RegistrationFrameworkException {

        Node currentNode = context.getCurrentNode();
        if (firstNode == null) {
            throw new RegistrationFrameworkException("No nodes found in the sequence.");
        }
        // If the current node is not provided, start from the beginning.
        if (currentNode == null) {
            currentNode = firstNode;
        }

        while (currentNode != null) {

            // Retrieve the inputs for the current node and remove it from the context.
            InputData dataForCurrentNode = context.retrieveUserInputFromContext(currentNode.getName());
            NodeResponse nodeResponse = currentNode.execute(dataForCurrentNode, context);

            if (STATUS_USER_INPUT_REQUIRED.equals(nodeResponse.getStatus())) {
                context.setCurrentNode(currentNode);
                context.setRequiredMetaData(nodeResponse.getInputDataList());
                return nodeResponse;
            }

            // Sometimes the node execution can be completed but request more data. Ex: CombinedInputCollectorNode.
            if (STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus()) && !nodeResponse.getInputDataList().isEmpty()) {
                currentNode = moveToNextNode(currentNode);
                nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
                context.setCurrentNode(currentNode);
                context.setRequiredMetaData(nodeResponse.getInputDataList());
                return nodeResponse;
            }
            currentNode = moveToNextNode(currentNode);
        }

        return new NodeResponse(STATUS_FLOW_COMPLETE);
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private Node moveToNextNode(Node currentNode) {

        Node nextNode = currentNode.getNextNode();

        if (nextNode != null) {
            nextNode.setPreviousNode(currentNode);
        }
        return nextNode;
    }


}
