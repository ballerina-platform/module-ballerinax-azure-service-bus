/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.asb;

import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.jvm.util.BLangConstants.VERSION_SEPARATOR;

/**
 * Asb Connector Constants.
 */
public class ASBConstants {
    // Asb package name constant fields
    public static final String ORG_NAME = "ballerinax";
    public static final String ASB = "asb";
    public static final String ASB_VERSION = "0.1.0";
    public static final BPackage PACKAGE_ID_ASB = new BPackage(ORG_NAME, "asb", ASB_VERSION);
    public static final String PACKAGE_RABBITMQ_FQN =
            ORG_NAME + ORG_NAME_SEPARATOR + ASB + VERSION_SEPARATOR + ASB_VERSION;

    // Error constant fields
    static final String ASB_ERROR = "AsbError";

    // Message constant fields
    public static final String MESSAGE_OBJECT = "Message";
    public static final String MESSAGES_OBJECT = "Messages";
    public static final BString MESSAGE_CONTENT = BStringUtils.fromString("messageContent");
    public static final BString MESSAGES_CONTENT = BStringUtils.fromString("messages");
    public static final BString DELIVERY_TAG = BStringUtils.fromString("deliveryTag");
    public static final String XML_CONTENT_ERROR = "Error while retrieving the xml content of the message. ";
    public static final String JSON_CONTENT_ERROR = "Error while retrieving the json content of the message. ";
    public static final String TEXT_CONTENT_ERROR = "Error while retrieving the string content of the message. ";
    public static final String INT_CONTENT_ERROR = "Error while retrieving the int content of the message. ";
    public static final String FLOAT_CONTENT_ERROR = "Error while retrieving the float content of the message. ";

    // listener constant fields
    public static final String CONSUMER_SERVICES = "consumer_services";
    public static final String STARTED_SERVICES = "started_services";
    public static final String FUNC_ON_MESSAGE = "onMessage";

    public static final BString QUEUE_NAME = BStringUtils.fromString("queueName");
    public static final BString CONNECTION_STRING = BStringUtils.fromString("connectionString");
    public static final String CONNECTION_NATIVE_OBJECT = "asb_connection_object";

    public static final String SERVICE_CONFIG = "ServiceConfig";
    public static final BString ALIAS_QUEUE_CONFIG = BStringUtils.fromString("queueConfig");

    public static final String UNCHECKED = "unchecked";
}
