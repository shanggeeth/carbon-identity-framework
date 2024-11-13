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

package org.wso2.carbon.identity.user.self.registration.executor.impl;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NEXT_ACTION_PENDING;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.action.AttributeCollection;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

/**
 * Implementation of an executor for attribute collection.
 */
public class AttributeCollectorImpl implements AttributeCollection {

    private String name;
    private final List<InputMetaData> requiredData = new ArrayList<>();

    public AttributeCollectorImpl() {

    }

    public AttributeCollectorImpl(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void addRequiredData(InputMetaData inputMetaData) {

        requiredData.add(inputMetaData);
    }

    @Override
    public ExecutorResponse collect(RegistrationContext context) {

        ExecutorResponse response = new ExecutorResponse();
        if (STATUS_ATTR_REQUIRED.equals(context.getExecutorStatus())) {
            Map<String, String> input = context.getUserInputData();
            requiredData.removeIf(data -> {
                if (input.containsKey(data.getName()) && input.get(data.getName()) != null) {
                    Map<String, Object> updatedClaims = new HashMap<>();
                    updatedClaims.put(data.getName(), input.get(data.getName()));
                    response.setUpdatedUserClaims(updatedClaims);
                    return true;
                }
                return false;
            });
            response.setResult(STATUS_ACTION_COMPLETE);
            return response;
        }
        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_ATTR_REQUIRED);
            response.setRequiredData(requiredData);
            return response;
        }
        return new ExecutorResponse("ERROR");
    }

    @Override
    public InitData getAttrCollectInitData() {

        return new InitData(STATUS_ATTR_REQUIRED, requiredData);
    }

    @Override
    public List<InitData> getInitData() {

        List<InitData> initData = new ArrayList<>();
        initData.add(getAttrCollectInitData());
        return initData;
    }
}
