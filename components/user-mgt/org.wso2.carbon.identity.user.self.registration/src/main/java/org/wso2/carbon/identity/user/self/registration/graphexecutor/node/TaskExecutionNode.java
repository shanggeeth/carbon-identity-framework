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
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_VERIFICATION_REQUIRED;
import java.util.List;
import java.util.Optional;
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
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationRequestedUser;

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
     * @return The executor associated with the node.
     */
    public Executor getExecutor() {

        return executor;
    }

    @Override
    public NodeResponse execute(RegistrationContext context)
            throws RegistrationFrameworkException {

        if (executor == null) {
            throw new RegistrationServerException(ERROR_EXECUTOR_NOT_FOUND.getCode(),
                                                  ERROR_EXECUTOR_NOT_FOUND.getMessage(),
                                                  String.format(ERROR_EXECUTOR_NOT_FOUND.getDescription(),
                                                                getNodeId()));
        }
        NodeResponse nodeResponse = triggerExecutor(context);

        if (STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus()) && executor instanceof Authentication) {
            context.addAuthenticatedMethod(executor.getName());
        }
        return nodeResponse;
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

    private NodeResponse triggerExecutor(RegistrationContext context)
            throws RegistrationFrameworkException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        Optional<NodeResponse> attributeCollectionResponse = triggerAttributeCollection(context);
        if (attributeCollectionResponse.isPresent()) {
            return attributeCollectionResponse.get();
        }

        Optional<NodeResponse> credEnrollmentResponse = triggerCredentialEnrollment(context);
        if (credEnrollmentResponse.isPresent()) {
            return credEnrollmentResponse.get();
        }

        Optional<NodeResponse> verificationResponse = triggerVerification(context);
        if (verificationResponse.isPresent()) {
            return verificationResponse.get();
        }

        return new NodeResponse(STATUS_NODE_COMPLETE);
    }

    private Optional<NodeResponse> triggerAttributeCollection(RegistrationContext context) throws RegistrationFrameworkException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) || STATUS_ATTR_REQUIRED.equals(executorStatus)) &&
                executor instanceof AttributeCollection) {
            response = ((AttributeCollection) executor).collect(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context,response);
            }
        }
        return Optional.empty();
    }

    private Optional<NodeResponse> triggerCredentialEnrollment(RegistrationContext context) throws RegistrationServerException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) || STATUS_CRED_REQUIRED.equals(executorStatus)) &&
                executor instanceof CredentialEnrollment) {
            response = ((CredentialEnrollment) executor).enrollCredential(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context,response);
            }
        }
        return Optional.empty();
    }

    private Optional<NodeResponse> triggerVerification(RegistrationContext context) throws RegistrationServerException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) || STATUS_VERIFICATION_REQUIRED.equals(executorStatus)) &&
                executor instanceof Verification) {
            response = ((Verification) executor).verify(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context,response);
            }
        }
        return Optional.empty();
    }

    private NodeResponse handleIncompleteStatus(RegistrationContext context, ExecutorResponse response) {

        NodeResponse nodeResponse = new NodeResponse(STATUS_USER_INPUT_REQUIRED);
        context.setExecutorStatus(response.getResult());
        context.addProperties(response.getContextProperties());
        nodeResponse.addInputMetaData(getNodeId(), response.getRequiredData());
        return nodeResponse;
    }

    private void handleCompleteStatus(RegistrationContext context, ExecutorResponse response)
            throws RegistrationServerException {

        if (!response.getRequiredData().isEmpty()) {
            throw new RegistrationServerException("Required data should be empty to complete the node.");
        }
        RegistrationRequestedUser user = context.getRegisteringUser();
        if (response.getUpdatedUserClaims() != null) {
            user.addClaims(response.getUpdatedUserClaims());
        }
        if (response.getUserCredentials() != null) {
            user.addUserCredentials(response.getUserCredentials());
        }
    }
}
