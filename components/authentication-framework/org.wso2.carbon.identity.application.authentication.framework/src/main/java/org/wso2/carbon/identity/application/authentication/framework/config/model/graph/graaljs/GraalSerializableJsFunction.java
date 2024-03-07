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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.GenericSerializableJsFunction;

import java.util.function.Function;

/**
 * Javascript function wrapper. This allows serialization of a javascript defined function.
 * Since Nashorn is deprecated in JDK 11 and onwards. We replaced it with GraalJS classes.
 */
public class GraalSerializableJsFunction implements GenericSerializableJsFunction<Context> {

    private static final Log log = LogFactory.getLog(GraalSerializableJsFunction.class);
    private static final long serialVersionUID = -7001351065432647040L;
    private String source;
    private boolean isPolyglotFunction = false;
    private String name;

    public GraalSerializableJsFunction(String source) {

        this.source = source;
        this.isPolyglotFunction = true;
    }

    public GraalSerializableJsFunction(String source, boolean isFunction) {

        this.source = source;
        this.isPolyglotFunction = true;
    }

    public String getSource() {

        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    @Override
    public boolean isFunction() {

        return isPolyglotFunction;
    }

    @Override
    public void setFunction(boolean function) {

    }

    public Object apply(Context polyglotContext, Object... params) {

        if (isPolyglotFunction) {
            try {
                Value jsFunction = polyglotContext.eval("js", "(" + getSource() + ")");
                return jsFunction.execute(params);
            } catch (PolyglotException e) {
                log.error("Error when executing function", e);
            }
        }

        return null;
    }

    /**
     * This will return the converted GraalSerializableJsFunction if the given  value is a function.
     *
     * @param functionObject Value type Function to Serialize
     * @return null if the ScriptObjectMirror is not a function.
     */
    public static GraalSerializableJsFunction toSerializableForm(Object functionObject) {

        if (functionObject == null) {
            return null;
        }
        try {
            Value functionAsValue;
            if (functionObject instanceof Function) {
                Context context = Context.getCurrent();
                functionAsValue = context.asValue(functionObject);
            } else {
                functionAsValue = (Value) functionObject;
            }
            if (functionAsValue.canExecute()) {
                if (functionAsValue.isHostObject()) {
                    return null;
                }
                String source = (String) functionAsValue.getSourceLocation().getCharacters();
                return serializePolyglot(source);
            }
        } catch (PolyglotException e) {
            log.error("Error when serializing JavaScript Function: ", e);
        }
        return null;

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    private static GraalSerializableJsFunction serializePolyglot(String source) {

        return new GraalSerializableJsFunction(source);
    }
}
