/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ORG_NAME_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VERSION_SEPARATOR;

/**
 * Asb Connector Constants.
 */
public class ASBConstants {
    // Asb package name constant fields
    public static final String ORG_NAME = "ballerinax";
    public static final String ASB = "asb";
    public static final String ASB_VERSION = "0.1.2";
    public static final Module PACKAGE_ID_ASB = new Module(ORG_NAME, "asb", ASB_VERSION);
    public static final String PACKAGE_ASB_FQN =
            ORG_NAME + ORG_NAME_SEPARATOR + ASB + VERSION_SEPARATOR + ASB_VERSION;

    // Error constant fields
    static final String ASB_ERROR = "AsbError";

    // Message constant fields
    public static final String MESSAGE_OBJECT = "Message";
    public static final BString MESSAGE_CONTENT = StringUtils.fromString("messageContent");
    public static final BString MESSAGE_CONTENT_TYPE = StringUtils.fromString("messageContentType");
    public static final BString BMESSAGE_ID = StringUtils.fromString("messageId");
    public static final BString BTO = StringUtils.fromString("to");
    public static final BString BREPLY_TO = StringUtils.fromString("replyTo");
    public static final BString BREPLY_TO_SESSION_ID = StringUtils.fromString("replyToSessionId");
    public static final BString BLABEL = StringUtils.fromString("label");
    public static final BString BSESSION_ID = StringUtils.fromString("sessionId");
    public static final BString BCORRELATION_ID = StringUtils.fromString("correlationId");
    public static final BString BTIME_TO_LIVE = StringUtils.fromString("timeToLive");
    public static final BString BPROPERTIES = StringUtils.fromString("properties");
    public static final String OPTIONAL_PROPERTIES = "OptionalProperties";

    // Message content data binding errors
    public static final String XML_CONTENT_ERROR = "Error while retrieving the xml content of the message. ";
    public static final String JSON_CONTENT_ERROR = "Error while retrieving the json content of the message. ";
    public static final String TEXT_CONTENT_ERROR = "Error while retrieving the string content of the message. ";
    public static final String INT_CONTENT_ERROR = "Error while retrieving the int content of the message. ";
    public static final String FLOAT_CONTENT_ERROR = "Error while retrieving the float content of the message. ";

    // Messages constant fields
    public static final String MESSAGES_OBJECT = "Messages";
    public static final BString MESSAGES_CONTENT = StringUtils.fromString("messages");
    public static final BString MESSAGE_COUNT = StringUtils.fromString("messageCount");

    // Keys of the input message optional parameters specified as a Map
    public static final String CONTENT_TYPE = "contentType";
    public static final String MESSAGE_ID = "messageId";
    public static final String TO = "to";
    public static final String REPLY_TO = "replyTo";
    public static final String REPLY_TO_SESSION_ID = "replyToSessionId";
    public static final String LABEL = "label";
    public static final String SESSION_ID = "sessionId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String TIME_TO_LIVE = "timeToLive";
    public static final int DEFAULT_TIME_TO_LIVE = 1;

    // listener constant fields
    public static final String CONSUMER_SERVICES = "consumer_services";
    public static final String STARTED_SERVICES = "started_services";
    public static final String FUNC_ON_MESSAGE = "onMessage";
    public static final String FUNC_ON_ERROR = "onError";
    public static final String DISPATCH_ERROR = "Error occurred while dispatching the message. ";

    public static final BString QUEUE_NAME = StringUtils.fromString("queueName");
    public static final BString CONNECTION_STRING = StringUtils.fromString("connectionString");
    public static final String CONNECTION_NATIVE_OBJECT = "asb_connection_object";

    public static final String SERVICE_CONFIG = "ServiceConfig";
    public static final BString ALIAS_QUEUE_CONFIG = StringUtils.fromString("queueConfig");

    public static final String UNCHECKED = "unchecked";
}
