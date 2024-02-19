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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HttpServletResponse.
 * This wrapper uses GraalJS polyglot context.
 * This provides controlled access to HttpServletResponse object via provided javascript native syntax.
 * e.g
 * response.headers.["Set-Cookie"] = ['crsftoken=xxxxxssometokenxxxxx']
 * <p>
 * instead of
 * context.getResponse().addCookie(cookie);
 * <p>
 * Also, it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * HttpServletResponse.
 */
public class JsGraalServletResponse extends JsServletResponse implements ProxyObject {

    Log log = LogFactory.getLog(JsGraalServletResponse.class);

    public JsGraalServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        if (FrameworkConstants.JSAttributes.JS_HEADERS.equals(name)) {
            Map<String, String> headers = new HashMap<>();
            Collection<String> headerNames = getResponse().getHeaderNames();
            if (headerNames != null) {
                for (String element : headerNames) {
                    headers.put(element, getResponse().getHeader(element));
                }
            }
            return new JsGraalHeaders(headers, getResponse());
        }
        return super.getMember(name);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(FrameworkConstants.JSAttributes.JS_HEADERS);
    }

    @Override
    public void putMember(String key, Value value) {

        log.warn("Unsupported operation. Servlet Response is read only. Can't set parameter " + key + " to value: " +
                value);
    }

}
