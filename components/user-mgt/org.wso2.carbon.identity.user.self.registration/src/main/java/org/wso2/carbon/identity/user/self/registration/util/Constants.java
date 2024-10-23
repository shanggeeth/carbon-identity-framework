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

package org.wso2.carbon.identity.user.self.registration.util;

/**
 * Constants for the graph executor.
 */
public class Constants {

    // Define a set of constants to track the status returned from the Executor level. The status can be COMPLETE,
    // INCOMPLETE, USER_INPUT_REQUIRED, ERROR, USER_CHOICE_REQUIRED
    public static final String STATUS_FLOW_COMPLETE = "COMPLETE";
    public static final String STATUS_USER_INPUT_REQUIRED = "USER_INPUT_REQUIRED";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_NODE_COMPLETE = "NODE_COMPLETE";
    public static final String STATUS_COMPLETE = "COMPLETE";

    public static final String STATUS_ACTION_COMPLETE = "ACTION_COMPLETE";
    public static final String STATUS_ATTR_REQUIRED = "ATTR_REQUIRED";
    public static final String STATUS_CRED_REQUIRED = "CRED_REQUIRED";
    public static final String STATUS_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String STATUS_NOT_STARTED = "NOT_STARTED";


    // Define enums to handle the error codes and messages.
    public enum ErrorMessages {

        // Server errors.
        ERROR_SEQUENCE_NOT_DEFINED_FOR_TENANT("SRF-65001",
                                   "Registration sequence not found.",
                                   "Registration sequence is not defined for the given tenant: %s"),
        ERROR_SEQUENCE_NOT_DEFINED_FOR_APP("SRF-65002",
                                              "Registration sequence not found.",
                                              "Registration sequence is not defined for the given application: %s"),
        ERROR_EXECUTOR_NOT_FOUND("SRF-65003",
                                 "Executor not found.",
                                 "Executor not found for the given node: %s"),

        // Client errors.
        ERROR_INVALID_FLOW_ID("SRF-60001",
                              "Invalid flow ID.",
                              "The given flow ID: %s is invalid."),
        ERROR_FLOW_ID_NOT_FOUND("SRF-60002",
                              "Flow ID not found.",
                              "Registration flow id is not found in the request."),;

        private final String code;
        private final String message;
        private final String description;

        /**
         * Create an Error Message.
         *
         * @param code    Relevant error code.
         * @param message Relevant error message.
         */
        ErrorMessages(String code, String message, String description) {
            this.code = code;
            this.message = message;
            this.description = description;
        }

        /**
         * To get the code of specific error.
         *
         * @return Error code.
         */
        public String getCode() {
            return code;
        }

        /**
         * To get the message of specific error.
         *
         * @return Error message.
         */
        public String getMessage() {

            return message;
        }

        /**
         * To get the description of specific error.
         *
         * @return Error description.
         */
        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
