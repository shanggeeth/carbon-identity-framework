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

package org.wso2.carbon.identity.user.self.registration.temp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.action.AttributeCollection;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.action.CredentialEnrollment;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.util.Constants;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_CRED_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;

public class PasswordOnboarderTest implements Authentication, AttributeCollection, CredentialEnrollment {

    private static final String USERNAME = "http://wso2.org/claims/username";

    public String getName() {

        return "PasswordOnboard";
    }

    @Override
    public ExecutorResponse authenticate(RegistrationContext context) {

        return null;
    }

    @Override
    public ExecutorResponse collect(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
        if (STATUS_ATTR_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(USERNAME)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                Map<String, Object> userClaims = new HashMap<>();
                userClaims.put(USERNAME, userInputs.get(USERNAME));
                response.setUpdatedUserClaims(userClaims);
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getUsernameData());
            return response;
        }
        response.setResult("ERROR");
        return response;
    }

    @Override
    public ExecutorResponse enrollCredential(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
        if (STATUS_CRED_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(PASSWORD)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                Map<String, String> credentials = new HashMap<>();
                credentials.put(PASSWORD, userInputs.get(PASSWORD));
                response.setUserCredentials(credentials);
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getPasswordData());
            return response;
        }
        response.setResult("ERROR");
        return response;
    }

    @Override
    public List<InitData> getInitData() {

        List<InitData> response = new ArrayList<>();
        response.add(getAttrCollectInitData());
        response.add(getCredentialEnrollmentInitData());
        return response;
    }

    @Override
    public InitData getCredentialEnrollmentInitData() {

        return new InitData(STATUS_CRED_REQUIRED, getPasswordData());
    }

    @Override
    public InitData getAuthInitData() {

        List<InputMetaData> inputMetaData = new ArrayList<>();
        inputMetaData.addAll(getUsernameData());
        inputMetaData.addAll(getPasswordData());
        return new InitData("AUTH_REQUIRED", inputMetaData);
    }

    @Override
    public InitData getAttrCollectInitData() {

        return new InitData(STATUS_ATTR_REQUIRED, getUsernameData());
    }

    private List<InputMetaData> getUsernameData() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData e1 = new InputMetaData(USERNAME, USERNAME, "attribute", 1);
        e1.setMandatory(true);
        e1.setValidationRegex("*");
        inputMetaData.add(e1);
        return inputMetaData;
    }

    private List<InputMetaData> getPasswordData() {

        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData e2 = new InputMetaData("password", PASSWORD, "credential", 1);
        e2.setMandatory(true);
        e2.setValidationRegex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        inputMetaData.add(e2);
        return inputMetaData;
    }
}