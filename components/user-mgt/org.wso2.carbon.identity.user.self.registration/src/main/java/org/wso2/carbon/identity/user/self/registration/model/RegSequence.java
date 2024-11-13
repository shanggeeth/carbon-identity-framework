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

package org.wso2.carbon.identity.user.self.registration.model;/*
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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.DecisionNode;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationFrameworkUtils;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;

import java.util.Optional;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_FLOW_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Represents a sequence of nodes in the registration flow.
 */
public class RegSequence {

    private static final Log LOG = LogFactory.getLog(RegSequence.class);
    private Node firstNode;

    public RegSequence() {

    }

    public RegSequence(Node node) {

        firstNode = node;
    }

    public Node getFirstNode() {

        return firstNode;
    }

    public void setFirstNode(Node firstNode) {

        this.firstNode = firstNode;
    }


    /**
     * Check if the sequence contains a node of the specified type.
     *
     * @param nodeType The class type of the node to check.
     * @return true if the sequence contains a node of the specified type, false otherwise.
     */
    public boolean containsNodeOfType(Class<? extends Node> nodeType) {

        Node currentNode = firstNode;
        while (currentNode != null) {
            if (nodeType.isInstance(currentNode)) {
                return true;
            }
            currentNode = currentNode.getNextNode();
        }
        return false;
    }



    /**
     * Execute the registration sequence.
     *
     * @param context Registration context.
     * @return Node response.
     * @throws RegistrationFrameworkException If an error occurs while executing the registration sequence.
     */
    public NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException {

        Node currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                              "registration sequence.");
            currentNode = firstNode;
        }

        while (currentNode != null) {

            NodeResponse nodeResponse = currentNode.execute(context);

            // Sometimes the node execution can be completed but request more data. Ex: CombinedInputCollectorNode.
            if (STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus()) && !nodeResponse.getInputMetaDataList().isEmpty()) {
                if (LOG.isDebugEnabled()){
                    LOG.debug("Current node: "+ currentNode.getNodeId() + " is completed but requires more data.");
                }
                currentNode = moveToNextNode(currentNode);
                nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
                context.setCurrentNode(currentNode);
                context.setRequiredMetaData(nodeResponse.getInputMetaDataList());
                return nodeResponse;
            } else if (STATUS_USER_CREATED.equals(nodeResponse.getStatus())) {
                if (LOG.isDebugEnabled()){
                    LOG.debug("User is successfully registered. Move to next node if there are any.");
                }
                currentNode = moveToNextNode(currentNode);
            } else if (!STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus())) {
                context.setCurrentNode(currentNode);
                context.setRequiredMetaData(nodeResponse.getInputMetaDataList());
                if (LOG.isDebugEnabled()){
                    LOG.debug("User input is required for the current node: " + currentNode.getNodeId());
                }
                return nodeResponse;
            } else {
                currentNode = moveToNextNode(currentNode);
            }
        }
        return handleExitLogic(context);
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
            if(LOG.isDebugEnabled()) {
                LOG.debug("Current node " + currentNode.getNodeId() + " is completed. "
                                  + "Moving to the next node: " + nextNode.getNodeId()
                                  + " and setting " + currentNode.getNodeId() + " as the previous node.");
            }
            nextNode.setPreviousNode(currentNode);
        }
        return nextNode;
    }

    // TODO: Implement the exit logic of the registration flow.
    private NodeResponse handleExitLogic(RegistrationContext context) {

        NodeResponse response = new NodeResponse(STATUS_FLOW_COMPLETE);
        if (context.getUserAssertion() != null ) {
            response.setUserAssertion(context.getUserAssertion());
        }
        return response;
    }
}
