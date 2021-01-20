// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Provides the functionality to handle the messages received by the consumer services.
public class Messages {
    private int messageCount = -1;
    private Message[] messages = [];

    # Retrieves the Array of Asb message objects.
    # 
    # + return - Array of Message objects
    public isolated function getMessages() returns Message[] {
        return self.messages;
    }

    # Retrieves the count of Message objects.
    # 
    # + return - Count of Message objects
    public isolated function getMessageCount() returns int {
        return self.messageCount;
    }
}
