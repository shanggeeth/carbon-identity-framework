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

import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.model.InputData;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

/**
 * Interface for a node in the registration flow graph.
 */
public interface Node {

    /**
     * Get the unique identifier of the node.
     *
     * @return The unique identifier of the node.
     */
    String getNodeId();

    /**
     * Get the next node in the sequence.
     *
     * @return The next node in the sequence.
     */
    Node getNextNode();

    /**
     * Set the next node in the sequence.
     *
     * @param nextNode The next node in the sequence.
     */
    void setNextNode(Node nextNode);

    /**
     * Get the previous node in the sequence.
     *
     * @return The previous node in the sequence.
     */
    Node getPreviousNode();

    /**
     * Set the previous node in the sequence.
     *
     * @param previousNode The previous node in the sequence.
     */
    void setPreviousNode(Node previousNode);

    /**
     * Execute the node.
     *
     * @param context The registration context.
     * @return The response of the node.
     * @throws RegistrationFrameworkException If an error occurs while executing the node.
     */
    NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException; // Placeholder for node-specific actions


    /**
     * Rollback the functionality of the node.
     *
     * @param context The registration context.
     * @return The response of the node.
     */
    NodeResponse rollback(RegistrationContext context) throws RegistrationFrameworkException;
}
