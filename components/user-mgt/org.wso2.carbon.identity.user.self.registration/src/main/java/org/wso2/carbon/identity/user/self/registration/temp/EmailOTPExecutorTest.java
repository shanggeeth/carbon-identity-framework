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
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_VERIFICATION_REQUIRED;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.action.AttributeCollection;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.action.Verification;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

public class EmailOTPExecutorTest implements Authentication, AttributeCollection, Verification {

    private final String EMAIL_ADDRESS = "http://wso2.org/claims/emailaddress";
    private final String EMAIL_VERIFIED_CLAIM_URI = "http://wso2.org/claims/emailverified";
    private final String EMAIL_OTP = "email-otp";

    public String getName() {

        return "EmailOTPVerifier";
    }

    public String getAuthMechanism() {

        return "EmailOTP";
    }

    @Override
    public ExecutorResponse authenticate(RegistrationContext context) {

        return  null;
    }

    @Override
    public ExecutorResponse collect(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
        if (STATUS_ATTR_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(EMAIL_ADDRESS)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                response.addUpdatedUserClaims(EMAIL_ADDRESS, userInputs.get(EMAIL_ADDRESS));
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(getEmailMetaData());
            return response;
        }
        response.setResult("ERROR");
        return response;
    }


    private List<InputMetaData> getEmailMetaData() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData(EMAIL_ADDRESS, "string", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }

    private List<InputMetaData> getOTPMetaData(){

        List<InputMetaData> inputMetaData = new ArrayList<>();
        InputMetaData data = new InputMetaData(EMAIL_OTP, "otp", 1);
        inputMetaData.add(data);
        return inputMetaData;
    }

    @Override
    public ExecutorResponse verify(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
        if (STATUS_VERIFICATION_REQUIRED.equals(context.getExecutorStatus())) {
            if ( userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(EMAIL_OTP)) {
                response.setResult(STATUS_ACTION_COMPLETE);
                response.addUpdatedUserClaims(EMAIL_VERIFIED_CLAIM_URI, "true");
                return response;
            }
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_VERIFICATION_REQUIRED);
            response.setRequiredData(getOTPMetaData());
            response.addContextProperty(EMAIL_OTP, "12345");
            return response;
        }
        response.setResult("ERROR");
        return response;
    }

    @Override
    public List<InitData> getInitData() {

        List<InitData> initData = new ArrayList<>();
        initData.add(getAttrCollectInitData());
        initData.add(getVerificationInitData());
        return initData;
    }

    @Override
    public InitData getAuthInitData() {

        return new InitData(STATUS_ATTR_REQUIRED, getEmailMetaData());
    }

    @Override
    public InitData getAttrCollectInitData() {

        return new InitData(STATUS_ATTR_REQUIRED, getEmailMetaData());
    }

    @Override
    public InitData getVerificationInitData() {

        return new InitData(STATUS_VERIFICATION_REQUIRED, getOTPMetaData());
    }
}
