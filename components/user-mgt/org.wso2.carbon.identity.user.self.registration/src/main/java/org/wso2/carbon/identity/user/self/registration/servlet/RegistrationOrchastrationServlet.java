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

package org.wso2.carbon.identity.user.self.registration.servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class RegistrationOrchastrationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("app");

        // Logic to get the app name from path
        // the url should look like this: http://localhost:9443/registration-orchestration/app/2347987234

        String[] pathParts = request.getRequestURI().split("/");
        String appName = pathParts[pathParts.length - 1];

        // get the json body which contains the registration flow data
        String jsonBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        // logic to register the registration flow data to the app

        // Return a dummy json response with the app name and the registration flow data
        response.setContentType("application/json");
        response.getWriter().write("{\"app\":\"" + appName + "\", \"registrationFlowData\":" + jsonBody + "}");
        response.setStatus(HttpServletResponse.SC_OK);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // get the app name from the path and return the app name as a json response

        String[] pathParts = request.getRequestURI().split("/");
        String appName = pathParts[pathParts.length - 1];

    }
}