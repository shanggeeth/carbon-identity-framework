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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsSerializer;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraalSelectAcrFromFunction;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_LOG;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of GraalJS Polyglot Context and any related expensive objects.
 * <p>
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing GraalJS engine.
 */
public class JsGraalGraphBuilderFactory implements JsBaseGraphBuilderFactory<Context> {

    private static final Log LOG = LogFactory.getLog(JsGraalGraphBuilderFactory.class);
    private static final String JS_BINDING_CURRENT_CONTEXT = "JS_BINDING_CURRENT_CONTEXT";

    public void init() {

    }

    @SuppressWarnings("unchecked")
    public static void restoreCurrentContext(AuthenticationContext authContext, Context context)
            throws FrameworkException {

        Map<String, Object> map = (Map<String, Object>) authContext.getProperty(JS_BINDING_CURRENT_CONTEXT);
        Value bindings = context.getBindings(POLYGLOT_LANGUAGE);
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object deserializedValue = GraalSerializer.getInstance().fromJsSerializable(entry.getValue(), context);
                if (deserializedValue instanceof AbstractJSObjectWrapper) {
                    ((AbstractJSObjectWrapper) deserializedValue).initializeContext(authContext);
                }
                bindings.putMember(entry.getKey(), deserializedValue);
            }
        }
    }

    public static void persistCurrentContext(AuthenticationContext authContext, Context context) {

        Value engineBindings = context.getBindings(POLYGLOT_LANGUAGE);
        Map<String, Object> persistableMap = new HashMap<>();
        engineBindings.getMemberKeys().forEach((key) -> {
            Value binding = engineBindings.getMember(key);
            /*
             * Since, we don't have a difference between global and engine scopes, we need to identify what are the
             * custom functions and the logger object we added bindings to, and not persist them since we will anyways
             * bind them again.
             * The functions will be host objects and can be executed. The logger object will be host object and will
             * not have any array elements.
             */
            if (!(binding.isHostObject() && (binding.canExecute() || !binding.hasArrayElements()))) {
                persistableMap.put(key, GraalSerializer.getInstance().toJsSerializable(binding));
            }
        });
        authContext.setProperty(JS_BINDING_CURRENT_CONTEXT, persistableMap);
    }

    public Context createEngine(AuthenticationContext authenticationContext) {

        Context context = Context.newBuilder(POLYGLOT_LANGUAGE)
                .allowHostAccess(HostAccess.EXPLICIT)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        Value bindings = context.getBindings(POLYGLOT_LANGUAGE);
        bindings.putMember(JS_FUNC_SELECT_ACR_FROM, new GraalSelectAcrFromFunction());
        bindings.putMember(JS_LOG, new JsLogger());
        return context;
    }

    @Override
    public JsSerializer getJsUtil() {

        return GraalSerializer.getInstance();
    }

    @Override
    public JsBaseGraphBuilder getCurrentBuilder() {

        return JsGraalGraphBuilder.getCurrentBuilder();
    }

    public JsGraalGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                             Map<Integer, StepConfig> stepConfigMap) {

        return new JsGraalGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext));
    }

    public JsGraalGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                             Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode) {

        return new JsGraalGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext),
                currentNode);
    }

}
