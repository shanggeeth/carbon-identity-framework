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
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;
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
                Map<String, Object> updatedClaims = new HashMap<>();
                updatedClaims.put("claimURI", userInputs.get(ID_TOKEN));
                response.setUpdatedUserClaims(updatedClaims);
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getIdTokenRequirement());
            response.setAdditionalInfo(getConfigurations());
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

        List<InputMetaData> inputMetaData = new ArrayList<>(getIdTokenRequirement());
        return new InitData("AUTH_REQUIRED", inputMetaData);
    }

    @Override
    public InitData getAttrCollectInitData() {

        return new InitData(STATUS_EXTERNAL_REDIRECTION, getIdTokenRequirement());
    }

    private List<InputMetaData> getIdTokenRequirement() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        String attributeId = "google_id_token";
        InputMetaData e1 = new InputMetaData(attributeId, "google_id_token", "attribute", 1);
        e1.setMandatory(true);
        e1.setValidationRegex("*");
        inputMetaData.add(e1);
        return inputMetaData;
    }

    private Map<String, String> getConfigurations() {

        Map<String, String> googleProperties = new HashMap<>();
        googleProperties.put("redirectUrl", "https://accounts.google.com/o/oauth2/auth?response_type=code" +
                "&redirect_uri=https%3A%2F%2Fexample-app.com%2Fredirect&state=e12f-ed27-49e5-ad0a-8bbb5671d81e%2COIDC" +
                "&client_id=231644702133-ds23592jt.apps.googleusercontent.com&scope=openid");
        return googleProperties;
    }
}