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
package io.ballerina.messaging.broker.core.eventpublisher;

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.ContentChunk;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Default implementation of {@link CorePublisher}.
 */
public class DefaultCorePublisher implements CorePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCorePublisher.class);
    private Broker broker;

    /**
     * Name of the Exchange where notifications are published.
     */
    private String exchangeName = "x-event";
    private int count;

    DefaultCorePublisher(Broker broker) {
        this.broker = broker;
    }

    @Override
    public void publishNotification(String id, Map<String, String> properties) {
        String data = "Event Message";

        Metadata metadata = new Metadata(id, exchangeName, data.length());
        metadata.setHeaders(properties);
        FieldTable messageProperties = new FieldTable();
        messageProperties.add(ShortString.parseString("propertyFlags"), FieldValue.parseLongInt(8192));
        metadata.setProperties(messageProperties);
        Message notificationMessage = new Message(count++, metadata);


        //Creating the body of the message
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        int chunkSize = dataBytes.length;
        int offset = 0;
        ByteBuf buffer = Unpooled.wrappedBuffer(dataBytes,
                                                offset,
                                                Math.min(chunkSize, dataBytes.length - offset));
        notificationMessage.addChunk(new ContentChunk(0, buffer));

        try {
            broker.publish(notificationMessage);
        } catch (BrokerException e) {
            LOGGER.warn("Exception while publishing event notification message {}", notificationMessage, e);
        }
    }

    void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

}
