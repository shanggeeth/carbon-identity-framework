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

package org.wso2.carbon.identity.user.self.registration.exception;

/**
 * Represents the client exception that occurs in the registration framework.
 */
public class RegistrationClientException extends RegistrationFrameworkException {

    public RegistrationClientException(String message) {

        super(message);
    }

    public RegistrationClientException(String errorCode, String message, String description, Throwable cause) {

        super(errorCode, message, description, cause);
    }

    public RegistrationClientException(String errorCode, String message, String description) {

        super(errorCode, message, description);
    }

    public RegistrationClientException(String message, Throwable cause) {

        super(message, cause);
    }

    public RegistrationClientException(Throwable cause) {

        super(cause);
    }
}
