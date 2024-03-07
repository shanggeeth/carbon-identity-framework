/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseRuntimeClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Represent the user's runtime claims.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public class JsOpenJdkNashornRuntimeClaims extends JsOpenJdkNashornClaims
        implements JsBaseRuntimeClaims, AbstractOpenJdkNashornJsObject {

    public JsOpenJdkNashornRuntimeClaims(AuthenticationContext context, int step, String idp) {

        super(context, step, idp, false);
    }

    public JsOpenJdkNashornRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        super(context, user, false);
    }

    public Object getMember(String claimUri) {

        if (authenticatedUser != null) {
            return getRuntimeClaim(claimUri);
        }
        return null;
    }

    public boolean hasMember(String claimUri) {

        if (authenticatedUser != null) {
            return hasRuntimeClaim(claimUri);
        }
        return false;
    }

    public void setMember(String claimUri, Object claimValue) {

        if (authenticatedUser != null) {
            setRuntimeClaim(claimUri, claimValue);
        }
    }
}
