/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

-- WSO2 Message Broker Oracle Database schema --

-- Start of Message Store Tables --
/
CREATE TABLE MB_EXCHANGE (
    EXCHANGE_NAME VARCHAR2(256) NOT NULL,
    EXCHANGE_TYPE VARCHAR2(256) NOT NULL,
    CONSTRAINT PK_MB_EXCHANGE PRIMARY KEY (EXCHANGE_NAME)
)
/
CREATE TABLE MB_QUEUE_METADATA (
    QUEUE_NAME VARCHAR2(256) NOT NULL,
    QUEUE_ARGUMENTS RAW(2000) NOT NULL,
    CONSTRAINT PK_MB_QUEUE_METADATA PRIMARY KEY(QUEUE_NAME)
)
/
CREATE TABLE MB_BINDING (
    EXCHANGE_NAME VARCHAR2(256) NOT NULL,
    QUEUE_NAME VARCHAR2(256) NOT NULL,
    ROUTING_KEY VARCHAR2(256) NOT NULL,
    ARGUMENTS RAW(2000) NOT NULL,
    CONSTRAINT PK_MB_BINDING PRIMARY KEY (EXCHANGE_NAME, QUEUE_NAME, ROUTING_KEY, ARGUMENTS),
    CONSTRAINT FK_MB_BINDING_EXCHANGE FOREIGN KEY (EXCHANGE_NAME) REFERENCES MB_EXCHANGE (EXCHANGE_NAME) ON DELETE CASCADE,
    CONSTRAINT FK_MB_BINDING_QUEUE FOREIGN KEY (QUEUE_NAME) REFERENCES MB_QUEUE_METADATA (QUEUE_NAME) ON DELETE CASCADE
)
/
CREATE TABLE MB_METADATA (
    MESSAGE_ID NUMBER(19),
    EXPIRE_TIMESTAMP NUMBER(19) NOT NULL,
    EXCHANGE_NAME VARCHAR2(256) NOT NULL,
    ROUTING_KEY VARCHAR2(256) NOT NULL,
    CONTENT_LENGTH NUMBER(19) NOT NULL,
    MESSAGE_METADATA RAW(2000) NOT NULL,
    CONSTRAINT PK_MB_METADATA PRIMARY KEY (MESSAGE_ID)
)
/
CREATE TABLE MB_CONTENT (
    MESSAGE_ID NUMBER(19),
    CONTENT_OFFSET INT,
    MESSAGE_CONTENT RAW(2000) NOT NULL,
    CONSTRAINT PK_MB_CONTENT PRIMARY KEY (MESSAGE_ID, CONTENT_OFFSET),
    CONSTRAINT FK_MB_CONTENT FOREIGN KEY (MESSAGE_ID) REFERENCES MB_METADATA (MESSAGE_ID) ON DELETE CASCADE
)
/
CREATE TABLE MB_QUEUE_MAPPING (
    QUEUE_NAME VARCHAR2(256) NOT NULL,
    MESSAGE_ID INT,
    CONSTRAINT PK_MB_QUEUE_MAPPING PRIMARY KEY (MESSAGE_ID, QUEUE_NAME),
    CONSTRAINT FK_MB_QUEUE_MAPPING_MESSAGE FOREIGN KEY (MESSAGE_ID) REFERENCES MB_METADATA (MESSAGE_ID) ON DELETE CASCADE,
    CONSTRAINT FK_MB_QUEUE_MAPPING_QUEUE FOREIGN KEY (QUEUE_NAME) REFERENCES MB_QUEUE_METADATA (QUEUE_NAME) ON DELETE CASCADE
)
/
INSERT INTO MB_EXCHANGE (EXCHANGE_NAME, EXCHANGE_TYPE) VALUES ('<<default>>', 'direct')
/
INSERT INTO MB_EXCHANGE (EXCHANGE_NAME, EXCHANGE_TYPE) VALUES ('amq.dlx', 'direct')
/
INSERT INTO MB_EXCHANGE (EXCHANGE_NAME, EXCHANGE_TYPE) VALUES ('amq.direct', 'direct')
/
INSERT INTO MB_EXCHANGE (EXCHANGE_NAME, EXCHANGE_TYPE) VALUES ('amq.topic', 'topic')
-- End of Message Store Tables --

-- Start of RDBMS based Coordinator Election Tables  --
/
CREATE TABLE MB_COORDINATOR_HEARTBEAT (
    ANCHOR NUMBER(19),
    NODE_ID VARCHAR2(512) NOT NULL,
    LAST_HEARTBEAT NUMBER(19) NOT NULL,
    CONSTRAINT PK_MB_COORDINATOR_HEARTBEAT PRIMARY KEY (ANCHOR)
)
/
CREATE TABLE MB_NODE_HEARTBEAT (
    NODE_ID VARCHAR2(512) NOT NULL,
    LAST_HEARTBEAT NUMBER(19) NOT NULL,
    IS_NEW_NODE NUMBER(3) NOT NULL,
    CONSTRAINT PK_MB_NODE_HEARTBEAT PRIMARY KEY (NODE_ID)
)
/
-- End of RDBMS based Coordinator Election Tables  --

-- Start of Broker Authorization Tables --

CREATE TABLE MB_AUTH_SCOPE (
    SCOPE_ID NUMBER(19),
    SCOPE_NAME VARCHAR2(512) NOT NULL,
    CONSTRAINT PK_MB_AUTH_SCOPE PRIMARY KEY PRIMARY KEY (SCOPE_ID),
    CONSTRAINT SCOPE_NAME_CONSTRAINT UNIQUE (SCOPE_NAME)
)
/
CREATE TABLE MB_AUTH_SCOPE_MAPPING (
    SCOPE_ID NUMBER(19),
    USER_GROUP_ID VARCHAR2(256) NOT NULL,
    CONSTRAINT PK_MB_AUTH_SCOPE_MAPPING PRIMARY KEY (SCOPE_ID, USER_GROUP_ID),
    CONSTRAINT FK_MB_AUTH_SCOPE_MAPPING_SCOPE FOREIGN KEY (SCOPE_ID) REFERENCES MB_AUTH_SCOPE (SCOPE_ID) ON DELETE CASCADE
)
/
CREATE TABLE MB_AUTH_RESOURCE (
    RESOURCE_ID NUMBER(19),
    RESOURCE_TYPE VARCHAR2(256) NOT NULL,
    RESOURCE_NAME VARCHAR2(256) NOT NULL,
    OWNER_ID VARCHAR2(256) NOT NULL,
    CONSTRAINT PK_MB_AUTH_RESOURCE PRIMARY KEY (RESOURCE_ID),
    CONSTRAINT RESOURCE_CONSTRAINT UNIQUE (RESOURCE_TYPE, RESOURCE_NAME)
)
/
CREATE TABLE MB_AUTH_RESOURCE_MAPPING (
    RESOURCE_ID NUMBER(19),
    RESOURCE_ACTION VARCHAR2(256) NOT NULL,
    USER_GROUP_ID VARCHAR2(256) NOT NULL,
    CONSTRAINT PK_MB_AUTH_RESOURCE_MAPPING PRIMARY KEY (RESOURCE_ID, RESOURCE_ACTION, USER_GROUP_ID),
    CONSTRAINT FK_MB_AUTH_RESOURCE_MAPPING_RESOURCE FOREIGN KEY (RESOURCE_ID) REFERENCES MB_AUTH_RESOURCE (RESOURCE_ID) ON DELETE CASCADE
)
/
CREATE INDEX IDX_RESOURCE_KEY ON MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME)
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('exchanges:create')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('exchanges:delete')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('exchanges:get')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('exchanges:publish')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('queues:create')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('queues:delete')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('queues:get')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('queues:consume');
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('resources:grant')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('scopes:update')
/
INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('scopes:get')
/
INSERT INTO MB_AUTH_SCOPE_MAPPING (SCOPE_ID, USER_GROUP_ID) SELECT SCOPE_ID, 'admin' FROM MB_AUTH_SCOPE
/
INSERT INTO MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME ,OWNER_ID) VALUES('exchange', '<<default>>','admin')
/
INSERT INTO MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME ,OWNER_ID) VALUES('exchange', 'amq.direct','admin')
/
INSERT INTO MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME ,OWNER_ID) VALUES('exchange', 'amq.topic','admin')
/
INSERT INTO MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME ,OWNER_ID) VALUES('exchange', 'amq.dlx','admin');
/
INSERT INTO MB_AUTH_RESOURCE (RESOURCE_TYPE, RESOURCE_NAME ,OWNER_ID) VALUES('queue', 'amq.dlq','admin')
/
-- End of Broker Authorization Tables --
