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
package org.wso2.carbon.identity.user.self.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.self.registration.AttributeCollectionRegStepExecutor;
import org.wso2.carbon.identity.user.self.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.self.registration.UserRegistrationFlowService;
import org.wso2.carbon.identity.user.self.registration.UserRegistrationFlowServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
         name = "user.self.registration.component",
         immediate = true)
public class UserRegistrationDSComponent {

    private static final Log log = LogFactory.getLog(UserRegistrationDSComponent.class);

    private static BundleContext bundleContext = null;
    private static RegistryService registryService = null;
    private static RealmService realmService = null;
    public static RegistryService getRegistryService() {
        return registryService;
    }

    @Activate
    protected void activate(ComponentContext context) {

        bundleContext = context.getBundleContext();

        UserRegistrationFlowService registrationFlowService = UserRegistrationFlowServiceImpl.getInstance();
        bundleContext.registerService(UserRegistrationFlowService.class.getName(), registrationFlowService, null);

        bundleContext.registerService(RegistrationStepExecutor.class.getName(),
                AttributeCollectionRegStepExecutor.getInstance(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("UserRegistration bundle is deactivated ");
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        UserRegistrationDSComponent.registryService = null;
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        UserRegistrationDSComponent.registryService = registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        UserRegistrationDSComponent.realmService = realmService;
        UserRegistrationServiceDataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
        UserRegistrationDSComponent.realmService = null;
        UserRegistrationServiceDataHolder.setRealmService(null);
    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService")
    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        UserRegistrationServiceDataHolder.setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        UserRegistrationServiceDataHolder.setApplicationManagementService(null);
    }

    @Reference(
            name = "RegistrationStepExecutor",
            service = RegistrationStepExecutor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistrationStepExecutor")
    protected void setRegistrationStepExecutor(RegistrationStepExecutor registrationStepExecutor) {

        UserRegistrationServiceDataHolder.getRegistrationStepExecutors().add(registrationStepExecutor);
    }

    protected void unsetRegistrationStepExecutor(RegistrationStepExecutor registrationStepExecutor) {

        UserRegistrationServiceDataHolder.getRegistrationStepExecutors().remove(registrationStepExecutor);
    }
}

