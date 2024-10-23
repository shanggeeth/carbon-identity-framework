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

import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_CRED_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NOT_STARTED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_VERIFICATION_REQUIRED;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.action.AttributeCollection;
import org.wso2.carbon.identity.user.self.registration.action.CredentialEnrollment;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.action.Verification;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.InputData;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

/**
 * Implementation of a node specific to executing a registration executor.
 */
public class TaskExecutionNode extends AbstractNode implements InputCollectionNode {

    private final Executor executor;

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

        if (executor == null) {
            throw new RegistrationServerException(ERROR_EXECUTOR_NOT_FOUND.getCode(),
                                                  ERROR_EXECUTOR_NOT_FOUND.getMessage(),
                                                  String.format(ERROR_EXECUTOR_NOT_FOUND.getDescription(),
                                                                   getNodeId()));
        }
        ExecutorResponse executorResponse = triggerExecutor(inputData, context);

        if (STATUS_USER_INPUT_REQUIRED.equals(executorResponse.getStatus())) {
            NodeResponse response = new NodeResponse(STATUS_USER_INPUT_REQUIRED);
            response.addInputMetaData(getNodeId(), executorResponse.getRequiredData());
            return response;
        }
        if (executor instanceof Authentication) {
            context.addAuthenticatedMethod(executor.getName());
        }
        return new NodeResponse(STATUS_NODE_COMPLETE);
    }

    @Override
    public List<InputMetaData> getRequiredData() {

        if (executor != null && executor instanceof AttributeCollection) {
            AttributeCollection attributeCollection = (AttributeCollection) executor;
            InitData response = attributeCollection.getAttrCollectInitData();
            return response.getRequiredData();
        }
        return null;
    }

    private ExecutorResponse triggerExecutor(InputData inputData, RegistrationContext context)
            throws RegistrationFrameworkException {

        String executorStatus = context.getExecutorStatus();
        Map<String, String> inputs = inputData != null ? inputData.getUserInput() : null;
        ExecutorResponse response ;
         if ((STATUS_NOT_STARTED.equals(executorStatus) || STATUS_ATTR_REQUIRED.equals(executorStatus)) &&
                 executor instanceof AttributeCollection) {
             response = ((AttributeCollection) executor).collect(inputs, context);
             if (!STATUS_ACTION_COMPLETE.equals(response.getStatus())) {
                 context.setExecutorStatus(response.getStatus());
                 response.setStatus(STATUS_USER_INPUT_REQUIRED);
                 return response;
             }
         }
        if ((STATUS_NOT_STARTED.equals(executorStatus) || STATUS_CRED_REQUIRED.equals(executorStatus)) &&
                executor instanceof CredentialEnrollment) {
            response = ((CredentialEnrollment) executor).enrollCredential(inputs, context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getStatus())) {
                context.setExecutorStatus(response.getStatus());
                response.setStatus(STATUS_USER_INPUT_REQUIRED);
                return response;
            }
        }
        if ((STATUS_NOT_STARTED.equals(executorStatus) || STATUS_VERIFICATION_REQUIRED.equals(executorStatus)) &&
                executor instanceof Verification) {

             response = ((Verification) executor).verify(inputs, context);
                if (!STATUS_ACTION_COMPLETE.equals(response.getStatus())) {
                    context.setExecutorStatus(response.getStatus());
                    response.setStatus(STATUS_USER_INPUT_REQUIRED);
                    return response;
                }
        }
        // If the executor is not an instance of any of the above classes, there's nothing to execute.
        return new ExecutorResponse(STATUS_NODE_COMPLETE);
    }
}
