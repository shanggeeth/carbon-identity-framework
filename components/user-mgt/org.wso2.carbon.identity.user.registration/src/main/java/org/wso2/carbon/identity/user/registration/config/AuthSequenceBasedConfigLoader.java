/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.NOT_STARTED;

public class AuthSequenceBasedConfigLoader {

    private static final Log LOG = LogFactory.getLog(AuthSequenceBasedConfigLoader.class);
    private static AuthSequenceBasedConfigLoader instance = new AuthSequenceBasedConfigLoader();

    private AuthSequenceBasedConfigLoader() {

    }

    public static AuthSequenceBasedConfigLoader getInstance() {

        return instance;
    }

    public RegistrationSequence deriveRegistrationSequence(String appId) throws RegistrationFrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ServiceProvider sp;
        try {
            sp = appInfo.getApplicationByResourceId(appId, "carbon.super" );
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error occurred while retrieving service provider", e);
        }
        if (sp == null) {
            throw new RegistrationFrameworkException("Service provider not found for app id: " + appId);
        }
        AuthenticationStep[] authenticationSteps =
                sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
        return getSequenceFromServiceProvider(sp, authenticationSteps);
    }


    private RegistrationSequence getSequenceFromServiceProvider(ServiceProvider serviceProvider,
                                                                AuthenticationStep[] authenticationSteps) throws RegistrationFrameworkException {

        if (serviceProvider == null) {
            throw new RegistrationFrameworkException("ServiceProvider cannot be null.");
        }
        RegistrationSequence sequenceConfig = new RegistrationSequence();
        sequenceConfig.setApplicationId(serviceProvider.getApplicationName());
        sequenceConfig.setFlowDefinition(RegistrationFlowConstants.DEFAULT_FLOW_DEFINITION);

        if (authenticationSteps == null) {
            return sequenceConfig;
        }

        // for each configured step. We are considering only the first step at the moment.
//        for (AuthenticationStep authenticationStep : authenticationSteps) {
//
//            // loading local authenticators
//            RegistrationStep stepConfig = loadExecutors(authenticationStep);
//
//            if (stepConfig != null) {
//                stepConfig.setStatus(NOT_STARTED);
//                stepConfig.setOrder(++stepOrder);
//                sequenceConfig.getStepMap().put(stepConfig.getOrder(), stepConfig);
//            }
//        }
        AuthenticationStep firstStep = authenticationSteps[0];

        // Load registration executors based on the authenticators.
        RegistrationStep stepConfig = loadExecutors(firstStep);

        if (stepConfig != null) {
            sequenceConfig.addStepDefinition(stepConfig);
        }
        RegistrationStep attributeCollectStep = deriveAttributeCollectionStep(serviceProvider);

        if (attributeCollectStep != null) {
            sequenceConfig.addStepDefinition(attributeCollectStep);
        }
        return sequenceConfig;
    }

    private IdentityProvider getIdentityProvider(String idpName) throws RegistrationFrameworkException {

        IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
        try {
            return idpdao.getIdentityProvider(idpName);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Cannot get the IdP: " + idpName, e);
        }
    }

    private Map<String, String> getAuthenticatorPropertyMapFromIdP(IdentityProvider idp, String name) {

        Map<String, String> propertyMap = new HashMap<String, String>();

        if (idp != null) {
            FederatedAuthenticatorConfig[] authenticatorConfigs = idp.getFederatedAuthenticatorConfigs();

            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (authenticatorConfig.getName().equals(name)) {

                    for (Property property : authenticatorConfig.getProperties()) {
                        propertyMap.put(property.getName(), property.getValue());
                    }
                    break;
                }
            }
        }
        return propertyMap;
    }

    private RegistrationStepExecutor getRegStepExecutor(String name) {

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (name.equals(executor.getName())) {
                return executor;
            }
        }
        return null;
    }

    private RegistrationStep loadExecutors(AuthenticationStep authenticationStep)
            throws RegistrationFrameworkException {

        List<RegistrationStepExecutorConfig> executorConfigs = new ArrayList<>();

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            // assign it to the step
            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        localAuthenticator.getName(), localAuthenticator.getDisplayName());
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
                                .getIdPByName(federatedIDP.getIdentityProviderName(), "carbon.super")
                                .getDefaultAuthenticatorConfig();
                    } catch (IdentityProviderManagementException e) {
                        throw new RegistrationFrameworkException("Failed to load the default authenticator for IDP : "
                                + federatedIDP.getIdentityProviderName(), e);
                    }
                }
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        federatedAuthenticator.getName(), federatedAuthenticator.getDisplayName()
                );
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        if (executorConfigs.size() == 0) {
            LOG.info("No supported executors in the step");
            return null;
        }
        RegistrationStep stepConfig = new RegistrationStep();
        stepConfig.setConfiguredExecutors(executorConfigs);
        if (executorConfigs.size() > 1) {
            stepConfig.setMultiOption(true);
        }
        return stepConfig;
    }

    private RegistrationStepExecutorConfig getMappedRegExecutorConfig(String idpName, String givenName)
            throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig regStepConfig = new RegistrationStepExecutorConfig();
        RegistrationStepExecutor mappedRegExecutor = null;

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (RegistrationFlowConstants.RegistrationExecutorBindingType.AUTHENTICATOR.equals(executor.getBindingType())
                    && executor.getBoundIdentifier().equals(idpName)) {
                mappedRegExecutor = executor;
                break;
            }
        }

        if (mappedRegExecutor == null) {
            return null;
        }
        regStepConfig.setName(givenName);
        regStepConfig.setId(idpName);
        regStepConfig.setExecutor(mappedRegExecutor);
        regStepConfig.setIdentityProvider(getIdentityProvider(idpName));

        return regStepConfig;
    }

    private RegistrationStep deriveAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }
        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("AttributeCollection");
        config.setId("AttributeCollectorBasedOnAppClaims");
        config.setRequestedClaims(requestedClaims);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(config);

        RegistrationStep step = new RegistrationStep();
        step.setMultiOption(false);
        step.setSelectedExecutor(null);
        step.setConfiguredExecutors(executors);

        return step;
    }
}
