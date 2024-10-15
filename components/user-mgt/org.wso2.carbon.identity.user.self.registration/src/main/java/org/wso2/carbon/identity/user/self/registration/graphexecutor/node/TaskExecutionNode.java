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
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.AuthLinkedExecutor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Implementation of a node specific to executing a registration executor.
 */
public class TaskExecutionNode extends AbstractNode implements InputCollectionNode {

    private Executor executor;

    public TaskExecutionNode(String id, Executor executor) {

        setId(id);
        this.executor = executor;
    }

    /**
     * Get the executor associated with the node.
     *
     * @return  The executor associated with the node.
     */
    public Executor getExecutor() {

        return executor;
    }

    @Override
    public NodeResponse execute(InputData inputData, RegistrationContext context)
            throws RegistrationFrameworkException {

        ExecutorResponse executorResponse;
        if (executor == null) {
            throw new RegistrationFrameworkException("Executor not found for node");
        }
        executorResponse = executor.execute(inputData != null ? inputData.getUserInput() : null, context);

        if (executorResponse != null && STATUS_USER_INPUT_REQUIRED.equals(executorResponse.getStatus())) {
            NodeResponse response = new NodeResponse(STATUS_USER_INPUT_REQUIRED);
            response.addInputData(getNodeId(), executorResponse.getRequiredData());
            return response;
        }
        if (executor instanceof AuthLinkedExecutor) {
            context.addAuthenticatedMethod(((AuthLinkedExecutor) executor).getAuthMechanism());
        }
        return new NodeResponse(STATUS_NODE_COMPLETE);
    }

    @Override
    public List<InputMetaData> getRequiredData() {

        return (executor != null) ? executor.declareRequiredData() : null;
    }
}
