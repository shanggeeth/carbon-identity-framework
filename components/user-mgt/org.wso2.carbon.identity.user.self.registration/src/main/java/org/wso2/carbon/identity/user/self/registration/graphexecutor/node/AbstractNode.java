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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.node;

import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

public abstract class AbstractNode implements Node {

    private String name;
    private Node nextNode;
    private Node previousNode;

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public Node getNextNode() {

        return this.nextNode;
    }

    @Override
    public void setNextNode(Node nextNode) {

        this.nextNode = nextNode;

    }

    @Override
    public Node getPreviousNode() {

        return this.previousNode;
    }

    @Override
    public void setPreviousNode(Node previousNode) {

        this.previousNode = previousNode;

    }
}
