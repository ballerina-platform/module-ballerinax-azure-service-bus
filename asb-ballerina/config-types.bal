// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

# Client configuration details.
#
# + connectionString - Connection String of Azure service bus
# + entityPath - Name or path of the entity (e.g : Queue name, Topic name, Subscription path)
# + receiveMode - Receive mode as PEEKLOCK or RECEIVEANDDELETE (default : PEEKLOCK)
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    string connectionString;
    string entityPath;
    string receiveMode = PEEKLOCK;
|};