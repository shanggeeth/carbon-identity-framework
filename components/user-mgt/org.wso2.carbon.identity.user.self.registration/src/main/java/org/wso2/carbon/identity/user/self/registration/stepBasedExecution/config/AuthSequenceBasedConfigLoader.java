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

package org.wso2.carbon.identity.user.self.registration.stepBasedExecution.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This class is responsible for loading the authentication sequence based on the login sequence of the application.
 */
public class AuthSequenceBasedConfigLoader implements RegistrationSequenceLoader {

    private static final Log LOG = LogFactory.getLog(AuthSequenceBasedConfigLoader.class);
    private static final AuthSequenceBasedConfigLoader instance = new AuthSequenceBasedConfigLoader();

    private AuthSequenceBasedConfigLoader() {

    }

    public static AuthSequenceBasedConfigLoader getInstance() {

        return instance;
    }

    @Override
    public RegistrationSequence loadRegistrationSequence(ServiceProvider serviceProvider) throws RegistrationFrameworkException {

        if (serviceProvider == null) {
            throw new RegistrationFrameworkException("ServiceProvider cannot be null.");
        }
        AuthenticationStep[] authenticationSteps =
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();

        RegistrationSequence sequenceConfig = new RegistrationSequence();
        sequenceConfig.setApplicationId(serviceProvider.getApplicationName());
        sequenceConfig.setFlowDefinition(RegistrationConstants.DEFAULT_FLOW_DEFINITION);

        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return sequenceConfig;
        }

        // For each authentication step, consider the registration supported steps.
        for (AuthenticationStep authenticationStep : authenticationSteps) {

            RegistrationStep stepConfig = loadExecutors(authenticationStep, serviceProvider.getTenantDomain());
            if (stepConfig != null) {
                sequenceConfig.addStepDefinition(stepConfig);
            }
        }

        RegistrationStepExecutorConfig attributeCollectorConfig = deriveAttributeCollectionStep(serviceProvider);

//        if (attributeCollectorConfig != null) {
//            // Include the attribute collection as a mandatory task for the first step.
//            RegistrationStep firstStep = sequenceConfig.getStepDefinitions().get(0);
//            firstStep.addConfiguredExecutor(attributeCollectorConfig);
//            firstStep.setType(RegistrationConstants.StepType.AGGREGATED_TASKS);
//        }
        return sequenceConfig;
    }

    private RegistrationStepExecutor getRegStepExecutor(String name) {

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (name.equals(executor.getName())) {
                return executor;
            }
        }
        return null;
    }

    private RegistrationStep loadExecutors(AuthenticationStep authenticationStep, String tenantDomain)
            throws RegistrationFrameworkException {

        List<RegistrationStepExecutorConfig> executorConfigs = new ArrayList<>();

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            // assign it to the step
            IdentityProvider localIdp = new IdentityProvider();
            localIdp.setIdentityProviderName(FrameworkConstants.LOCAL_IDP_NAME);

            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(localAuthenticator.getName(),
                        localAuthenticator.getName(), localIdp);
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();
        if (federatedIDPs != null) {
            // for each idp in the step
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
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        federatedIDP.getIdentityProviderName(), federatedAuthenticator.getName(), federatedIDP
                );
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        if (executorConfigs.size() == 0) {
            LOG.info("No supported executors in the step.");
            return null;
        }
        RegistrationStep stepConfig = new RegistrationStep();
        stepConfig.setConfiguredExecutors(executorConfigs);
        // If there is only one executor, that is a mandatory step.
        if (executorConfigs.size() == 1) {
            executorConfigs.get(0).setOptional(false);
            stepConfig.setSelectedExecutor(executorConfigs.get(0));
        }
        if (executorConfigs.size() > 1) {
            stepConfig.setType(RegistrationConstants.StepType.MULTI_OPTION);
        }
        return stepConfig;
    }

    private RegistrationStepExecutorConfig getMappedRegExecutorConfig(String idpName,
                                                                      String authenticatorName, IdentityProvider idp)
            throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig regStepConfig = new RegistrationStepExecutorConfig();
        RegistrationStepExecutor mappedRegExecutor = null;

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (RegistrationConstants.RegExecutorBindingType.AUTHENTICATOR.equals(executor.getBindingType())
                    && executor.getBoundIdentifier().equals(authenticatorName)) {
                mappedRegExecutor = executor;
                break;
            }
        }

        if (mappedRegExecutor == null) {
            return null;
        }
        regStepConfig.setName(mappedRegExecutor.getName());
        regStepConfig.setId(Base64.getEncoder().encodeToString(idpName.getBytes(StandardCharsets.UTF_8)));
        regStepConfig.setExecutor(mappedRegExecutor);
        regStepConfig.setIdentityProvider(idp);

        return regStepConfig;
    }

    private RegistrationStepExecutorConfig deriveAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }
        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        RegistrationStepExecutor executor = getRegStepExecutor(RegistrationConstants.ATTRIBUTE_COLLECTOR);
        config.setName(executor.getName());
        config.setId(Base64.getEncoder()
                             .encodeToString("AttributeCollectorBasedOnAppClaims".getBytes(StandardCharsets.UTF_8)));
        config.setRequestedClaims(requestedClaims);
        config.setExecutor(executor);
        config.setOptional(false);
        return config;
    }
}
