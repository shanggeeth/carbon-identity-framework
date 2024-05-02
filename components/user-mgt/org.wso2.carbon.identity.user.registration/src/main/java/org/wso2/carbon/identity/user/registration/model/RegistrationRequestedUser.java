/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationRequestedUser implements Serializable {

    private static final long serialVersionUID = -1873658743998134877L;

    private String username;
    private boolean isPasswordless = true;
    private String credential;
    private Map<String, String> claims = new HashMap<>();
    private List<String> statusList = new ArrayList<>();

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
        // Update the username claim as well.
        this.claims.put("http://wso2.org/claims/username", username);
    }

    public String getCredential() {

        return credential;
    }

    public void setCredential(String credential) {

        this.credential = credential;
    }

    public Map<String, String> getClaims() {

        return claims;
    }

    public void setClaims(Map<String, String> claims) {

        this.claims = claims;
    }

    public String getClaim(String claimUri) {

        return this.claims.get(claimUri);
    }

    public void addClaim(String claimUri, String claimValue) {

        this.claims.put(claimUri, claimValue);
    }

    public boolean isPasswordless() {

        return isPasswordless;
    }

    public void setPasswordless(boolean isPasswordless) {

        this.isPasswordless = isPasswordless;
    }

    public List<String> getStatusList() {

        return statusList;
    }

    public void addUserStatus(String status) {

        this.statusList.add(status);
    }

    public boolean isVerifiedUserStatus(String status) {

        return this.statusList.contains(status);
    }
}
