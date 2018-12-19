/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.eventing;

import io.ballerina.messaging.broker.common.EventSync;
import java.util.Map;

public class TestPublisher implements EventSync {

    private boolean activeState;

    public boolean isActiveState() {
        return activeState;
    }

    public String getID() {
        return "TestPublisher";
    }

    @Override
    public void publish(String id, Map<String, String> properties) {
        //no implementation
    }

    @Override
    public void activate() {
        this.activeState = true;
    }

    @Override
    public void deactivate() {
        this.activeState = false;
    }
}