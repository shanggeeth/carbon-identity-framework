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

package org.wso2.carbon.identity.user.self.registration.executor;

import java.util.List;
import org.wso2.carbon.identity.user.self.registration.model.InitData;

/**
 * A wrapper interface for all the action types of registration.
 */
public interface Executor {

    String getName();

    List<InitData> getInitData();

    // TODO Have a execute function in this interface also to handle any actions that are not covered by the
    // action interfaces. Ex: user creation, org domain resolver
}
