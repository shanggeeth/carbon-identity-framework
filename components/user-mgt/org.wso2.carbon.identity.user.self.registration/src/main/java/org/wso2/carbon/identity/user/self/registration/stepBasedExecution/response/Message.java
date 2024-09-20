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

package org.wso2.carbon.identity.user.self.registration.stepBasedExecution.response;

import org.wso2.carbon.identity.user.self.registration.stepBasedExecution.util.RegistrationConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to define a message object in the registration response.
 */
public class Message {

    private RegistrationConstants.MessageType type;
    private String messageId;
    private String message;
    private Map<String, String> context = new HashMap<>();
    private String i18nkey;

    public Message() {

    }

    public Message(RegistrationConstants.MessageType type, String message) {

        this.type = type;
        this.message = message;
    }

    public RegistrationConstants.MessageType getType() {

        return type;
    }

    public void setType(RegistrationConstants.MessageType type) {

        this.type = type;
    }

    public String getMessageId() {

        return messageId;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public Map<String, String> getContext() {

        return context;
    }

    public void setContext(Map<String, String> context) {

        this.context = context;
    }

    public String getI18nkey() {

        return i18nkey;
    }

    public void setI18nkey(String i18nkey) {

        this.i18nkey = i18nkey;
    }
}
