/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperBaseFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalClaims;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalHeaders;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalRuntimeClaims;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalSteps;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for GraalJS execution.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing GraalJS engine.
 */
public class JsGraalWrapperFactory implements JsWrapperBaseFactory {

    @Override
    public JsGraalAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new JsGraalAuthenticatedUser(authenticatedUser);
    }

    @Override
    public JsGraalAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext authenticationContext,
                                                              AuthenticatedUser authenticatedUser) {

        return new JsGraalAuthenticatedUser(authenticationContext, authenticatedUser);
    }

    @Override
    public JsGraalAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext context,
                                                             AuthenticatedUser wrappedUser, int step, String idp) {

        return new JsGraalAuthenticatedUser(context, wrappedUser, step, idp);
    }

    @Override
    public JsGraalAuthenticationContext createJsAuthenticationContext
            (AuthenticationContext authenticationContext) {

        return new JsGraalAuthenticationContext(authenticationContext);
    }

    @Override
    public JsGraalCookie createJsCookie(Cookie cookie) {

        return new JsGraalCookie(cookie);
    }

    @Override
    public JsGraalParameters createJsParameters(Map parameters) {

        return new JsGraalParameters(parameters);
    }

    @Override
    public JsGraalWritableParameters createJsWritableParameters(Map data) {

        return new JsGraalWritableParameters(data);
    }

    @Override
    public JsGraalServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new JsGraalServletRequest(wrapped);
    }

    @Override
    public JsGraalServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new JsGraalServletResponse(wrapped);
    }

    @Override
    public JsGraalClaims createJsClaims(AuthenticationContext context, int step, String idp,
                                       boolean isRemoteClaimRequest) {

        return new JsGraalClaims(context, step, idp, isRemoteClaimRequest);
    }

    @Override
    public JsGraalClaims createJsClaims(AuthenticationContext context, AuthenticatedUser user,
                                       boolean isRemoteClaimRequest) {

        return new JsGraalClaims(context, user, isRemoteClaimRequest);
    }

    @Override
    public JsGraalRuntimeClaims createJsRuntimeClaims(AuthenticationContext context, int step, String idp) {

        return new JsGraalRuntimeClaims(context, step, idp);
    }

    @Override
    public JsGraalRuntimeClaims createJsRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        return new JsGraalRuntimeClaims(context, user);
    }

    @Override
    public JsGraalStep createJsStep(AuthenticationContext context, int step, String authenticatedIdp,
                                   String authenticatedAuthenticator) {

        return new JsGraalStep(context, step, authenticatedIdp, authenticatedAuthenticator);
    }

    @Override
    public JsGraalHeaders createJsHeaders(Map wrapped, HttpServletResponse response) {

        return new JsGraalHeaders(wrapped, response);
    }

    @Override
    public JsGraalSteps createJsSteps(AuthenticationContext context) {

        return new JsGraalSteps(context);
    }

}
