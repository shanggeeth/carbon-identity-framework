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

import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.graphexecutor.Constants.STATUS_NODE_COMPLETE;

public class CombinedInputCollectionNode extends AbstractNode {

    private List<InputCollectionNode> referencedNodes = new ArrayList<>();; // For branching paths

    public CombinedInputCollectionNode(String name) {

         setName(name);
    }

    public void setReferencedNodes(List<InputCollectionNode> refNodeList) {

        this.referencedNodes = refNodeList;
    }

    public void addReferencedNode(InputCollectionNode referencedNode) {

        referencedNodes.add(referencedNode);
    }

    @Override
    public NodeResponse execute(InputData inputData, RegistrationContext context) {

        // Only declare the data required. So this node is complete.
        NodeResponse result = new NodeResponse(STATUS_NODE_COMPLETE);
        for (InputCollectionNode refNode : referencedNodes) {
            List<InputMetaData> dataRequired = refNode.getRequiredData();
            if (dataRequired != null) {
                result.addInputData(refNode.getName(), dataRequired);
            }
        }
        return result;
    }
}
