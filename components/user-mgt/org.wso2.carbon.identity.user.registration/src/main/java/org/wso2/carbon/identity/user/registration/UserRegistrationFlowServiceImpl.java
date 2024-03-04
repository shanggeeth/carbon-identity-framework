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

package org.wso2.carbon.identity.user.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.response.RegistrationResponse;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;
import org.wso2.carbon.identity.user.registration.util.RegistrationFrameworkUtils;

import javax.servlet.http.HttpServletRequest;

public class UserRegistrationFlowServiceImpl implements UserRegistrationFlowService{

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowServiceImpl.class);
    private static final UserRegistrationFlowServiceImpl instance = new UserRegistrationFlowServiceImpl();

    public static UserRegistrationFlowServiceImpl getInstance() {

        return instance;
    }

    @Override
    public RegistrationResponse initiateUserRegistration(String appId,
                                                         String tenantDomain,
                                                         RegistrationFlowConstants.SupportedProtocol type)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.initiateRegContext(appId, tenantDomain, type);

        RegistrationResponse response = RegistrationFrameworkUtils
                .getRegistrationSeqHandler(context.getRegistrationSequence()).handle(new RegistrationRequest(), context);
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return response;
    }

    @Override
    public RegistrationResponse initiateUserRegistration(HttpServletRequest request)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFrameworkUtils.initiateRegContext(request);
        RegistrationResponse response = RegistrationFrameworkUtils
                .getRegistrationSeqHandler(context.getRegistrationSequence()).handle(new RegistrationRequest(), context);
        RegistrationFrameworkUtils.addRegContextToCache(context);
        return response;
    }

    @Override
    public RegistrationResponse processIntermediateUserRegistration(RegistrationRequest request)
            throws RegistrationFrameworkException {

        DefaultRegistrationSequenceHandler handler = DefaultRegistrationSequenceHandler.getInstance();
        RegistrationContext context = RegistrationFrameworkUtils.retrieveRegContextFromCache(request.getFlowId());
        if (context == null) {
            throw new RegistrationFrameworkException("Invalid flow id.");
        }
        RegistrationResponse response = handler.handle(request, context);
        if (context.isCompleted()) {
            LOG.debug("Registration flow completed for flow id: " + request.getFlowId() +
                    ". Hence clearing the cache.");
            RegistrationFrameworkUtils.removeRegContextFromCache(request.getFlowId());
        } else {
            LOG.debug("Registration flow is not completed for flow id: " + request.getFlowId() +
                    ". Hence updating the cache with the latest.");
            RegistrationFrameworkUtils.addRegContextToCache(context);
        }
        return response;
    }
}
