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

package org.wso2.carbon.identity.user.registration.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;

/**
 * This class is used to load the legacy registration sequences based on the configurations.
 */
public class LegacyRegSequenceLoader implements RegistrationSequenceLoader {

    private static final Log LOG = LogFactory.getLog(LegacyRegSequenceLoader.class);
    private static LegacyRegSequenceLoader instance = new LegacyRegSequenceLoader();

    private LegacyRegSequenceLoader() {

    }

    public static LegacyRegSequenceLoader getInstance() {

        return instance;
    }

    @Override
    public RegistrationSequence loadRegistrationSequence(ServiceProvider serviceProvider) throws
                                                                                          RegistrationFrameworkException {

        RegistrationSequence registrationSequence = new RegistrationSequence();

        return registrationSequence;
    }


    }
