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

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_CRED_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.action.AttributeCollection;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.action.CredentialEnrollment;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

public class GoogleSignupTest implements Authentication, AttributeCollection {

    private static final String ID_TOKEN = "google_id_token";

    public String getName() {

        return "GoogleOIDCAuthenticator";
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
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(ID_TOKEN)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                response.addUpdatedUserClaims("claimURI", "valueFromIDToken");
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getIdTokenRequirement());
            return response;
        }
        response.setResult("ERROR");
        return response;
    }


    @Override
    public List<InitData> getInitData() {

        List<InitData> response = new ArrayList<>();
        response.add(getAttrCollectInitData());
        return response;
    }

    @Override
    public InitData getAuthInitData() {

        List<InputMetaData> inputMetaData = new ArrayList<>();
        inputMetaData.addAll(getIdTokenRequirement());
        inputMetaData.addAll(getPasswordData());
        return new InitData("AUTH_REQUIRED", inputMetaData);
    }

    @Override
    public InitData getAttrCollectInitData() {

        return new InitData(STATUS_ATTR_REQUIRED, getIdTokenRequirement());
    }

    private List<InputMetaData> getIdTokenRequirement() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData e1 = new InputMetaData("google_id_token", "attribute", 1);
        e1.setMandatory(true);
        e1.setValidationRegex("*");
        inputMetaData.add(e1);
        return inputMetaData;
    }

    private List<InputMetaData> getPasswordData() {

        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData e2 = new InputMetaData("password", "credential", 1);
        e2.setMandatory(true);
        e2.setValidationRegex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        inputMetaData.add(e2);
        return inputMetaData;
    }
}