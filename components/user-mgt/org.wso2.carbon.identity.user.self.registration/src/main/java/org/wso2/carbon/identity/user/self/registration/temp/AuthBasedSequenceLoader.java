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

package org.wso2.carbon.identity.user.self.registration.temp;

import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.executor.impl.AttributeCollectorImpl;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.CombinedInputCollectionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.TaskExecutionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationFrameworkUtils;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This class is responsible for loading the authentication sequence based on the login sequence of the application.
 */
public class AuthBasedSequenceLoader {

    private static final Log LOG = LogFactory.getLog(AuthBasedSequenceLoader.class);

    private RegSequence deriveRegistrationSequence(String appId) throws RegistrationFrameworkException {

        ServiceProvider serviceProvider = RegistrationFrameworkUtils.retrieveSpFromAppId(appId, "carbon.super");
        AuthenticationStep[] authenticationSteps =
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();

        RegSequence sequenceConfig = new RegSequence();

        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return sequenceConfig;
        }

        Node currentNode = deriveAttributeCollectionStep(serviceProvider);
        sequenceConfig.setFirstNode(currentNode);
        // For each authentication step, consider the registration supported steps.
        for (AuthenticationStep authenticationStep : authenticationSteps) {

            Node nextNode = defineNode(authenticationStep, serviceProvider.getTenantDomain());
            if (nextNode == null) {
                LOG.info("No supported registration executors in the " + authenticationStep.getStepOrder() + " step " +
                                 "of the login flow.");
                continue;
            }
            if (currentNode == null) {
                sequenceConfig.setFirstNode(nextNode);
            } else if (currentNode instanceof UserChoiceDecisionNode) {
                for (Node option : ((UserChoiceDecisionNode) currentNode).getNextNodes()) {
                    option.setNextNode(nextNode);
                }
            } else {
                currentNode.setNextNode(nextNode);
            }
            currentNode = nextNode;
        }
        return sequenceConfig;
    }

    private Node defineNode(AuthenticationStep authenticationStep, String tenantDomain)
            throws RegistrationFrameworkException {

        List<TaskExecutionNode> options = new ArrayList<>();

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            IdentityProvider localIdp = new IdentityProvider();
            localIdp.setIdentityProviderName(FrameworkConstants.LOCAL_IDP_NAME);

            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                Executor regExecutor = getRegExecutor(localAuthenticator.getName());
                if (regExecutor != null) {
                    String idpName = localAuthenticator.getName();
                    String nodeId = Base64.getEncoder().encodeToString(idpName.getBytes(StandardCharsets.UTF_8));
                    TaskExecutionNode node = new TaskExecutionNode(nodeId, regExecutor);
                    options.add(node);
                }
            }
        }

        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();
        if (federatedIDPs != null) {
            // For each idp in the step.
            for (IdentityProvider federatedIDP : federatedIDPs) {
                FederatedAuthenticatorConfig federatedAuthenticator = federatedIDP.getDefaultAuthenticatorConfig();

                if (federatedAuthenticator == null) {
                    try {
                        federatedAuthenticator = IdentityProviderManager.getInstance()
                                .getIdPByName(federatedIDP.getIdentityProviderName(), tenantDomain)
                                .getDefaultAuthenticatorConfig();
                    } catch (IdentityProviderManagementException e) {
                        throw new RegistrationFrameworkException("Failed to load the default authenticator for IDP : "
                                + federatedIDP.getIdentityProviderName(), e);
                    }
                }
                Executor regExecutor = getRegExecutor(federatedAuthenticator.getName());
                if (regExecutor != null) {
                    String idpName = federatedIDP.getIdentityProviderName();
                    String nodeId = Base64.getEncoder().encodeToString(idpName.getBytes(StandardCharsets.UTF_8));
                    TaskExecutionNode node = new TaskExecutionNode(nodeId, regExecutor);
                    options.add(node);
                }
            }
        }

        if (options.isEmpty()) {
            return null;
        }
        if (options.size() > 1) {
            String nodeId =
                    Base64.getEncoder().encodeToString("userChoiceDecisionNode".getBytes(StandardCharsets.UTF_8));
            UserChoiceDecisionNode userChoiceDecisionNode = new UserChoiceDecisionNode(nodeId);
            userChoiceDecisionNode.setNextNodes(options);
            return userChoiceDecisionNode;
        }
        return options.get(0);
    }

    private Executor getRegExecutor(String authenticatorName) {

        Executor mappedRegExecutor = null;
        for (Executor executor : UserRegistrationServiceDataHolder.getExecutors()) {
            if (executor instanceof Authentication && authenticatorName.equals(executor.getName())) {
                mappedRegExecutor = executor;
                break;
            }
        }

        return  mappedRegExecutor;
    }

    private Node deriveAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }

        AttributeCollectorImpl collector = getAttributeCollectionExecutor(
                requestedClaims);
        String nodeId = Base64.getEncoder().encodeToString("mandatoryAttributeCollection".getBytes(StandardCharsets.UTF_8));
        return new TaskExecutionNode(nodeId, collector);
    }

    private AttributeCollectorImpl getAttributeCollectionExecutor(ClaimMapping[] requestedClaims) {

        AttributeCollectorImpl collector = new AttributeCollectorImpl();
        collector.setName("AttributeCollectorBasedOnAppClaims");

        int order = 0;
        for (ClaimMapping claimMapping : requestedClaims) {
            String claimUri = claimMapping.getLocalClaim().getClaimUri();
            InputMetaData inputMetaData = new InputMetaData(claimUri, "STRING", ++order);
            inputMetaData.setMandatory(claimMapping.isMandatory());
            collector.addRequiredData(inputMetaData);
        }
        return collector;
    }

    public RegSequence loadSequence(String appId) {

        if ("case1".equals(appId)) {
            return loadSequence1();
        } else if ("case2".equals(appId)) {
            return loadSequence2();
        } else if ("case3".equals(appId)) {
            return loadSequence3();
        }
        try {
            return new AuthBasedSequenceLoader().deriveRegistrationSequence(appId);
        } catch (RegistrationFrameworkException e) {
            LOG.error("Error while loading the sequence for the app: " + appId, e);
            return null;
        }
    }

    private RegSequence loadSequence1() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        InputMetaData e1 = new InputMetaData("emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        InputMetaData e2 = new InputMetaData("firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData("dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutionNode node3 = new TaskExecutionNode("node3", pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode("node4", emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode("node5", attrCollector2);

        node1.setNextNode(node2);
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNode(node5);
        node4.setNextNode(node5);

        // Define the flow of the graph
        return new RegSequence(node1);
    }

    private RegSequence loadSequence2() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        InputMetaData e1 = new InputMetaData("emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        InputMetaData e2 = new InputMetaData("firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData("dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutionNode node3 = new TaskExecutionNode("node3", pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode("node4", emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode("node5", attrCollector2);

        CombinedInputCollectionNode node0 = new CombinedInputCollectionNode("node0");
        node0.setReferencedNodes(new ArrayList<>(Arrays.asList(node1, node2, node5)));

        node0.setNextNode(node1);
        node1.setNextNode(node2);
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNode(node5);
        node4.setNextNode(node5);

        return new RegSequence(node0);
    }

    private RegSequence loadSequence3() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        InputMetaData e1 = new InputMetaData("emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        InputMetaData e2 = new InputMetaData("firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData("dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode("node1", attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode("node2");
        TaskExecutionNode node3 = new TaskExecutionNode("node3", pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode("node4", emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode("node5", attrCollector2);

        node1.setNextNode(node2);
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNode(node5);
        node4.setNextNode(node5);

        // Define the flow of the graph
        return new RegSequence(node1);
    }
}
