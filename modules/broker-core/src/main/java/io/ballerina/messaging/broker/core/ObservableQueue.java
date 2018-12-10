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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.EventSync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Represents an Queue which trigger events for the broker.
 */
final class ObservableQueue extends Queue {

    private final Queue queue;
    /**
     * We are using a HashSet instead of a concurrent for message limits set because this object is only read or
     * accessed and not written or modified.
     */
    private final HashSet<Integer> messageLimits;
    private final EventSync eventSync;

    ObservableQueue(Queue queue, EventSync eventSync, List<Integer> messageLimits) {
        super(queue.getName(), queue.isDurable(), queue.isAutoDelete());
        this.queue = queue;
        this.eventSync = eventSync;
        this.messageLimits = new HashSet<>(messageLimits);
    }

    @Override
    public int capacity() {
        return queue.capacity();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean enqueue(Message message) throws BrokerException {
        boolean enqueued = queue.enqueue(message);
        if (enqueued) {
            publishQueueLimitReachedEvent("queue.publishLimitReached.", getQueueHandler());
        }
        return enqueued;
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) throws BrokerException {
        queue.prepareEnqueue(xid, message);
    }

    @Override
    public void commit(Xid xid) {
        queue.commit(xid);
    }

    @Override
    public void rollback(Xid xid) {
        queue.rollback(xid);
    }

    @Override
    public Message dequeue() {
        Message message =  queue.dequeue();
        publishQueueLimitReachedEvent("queue.deliverLimitReached.", this.getQueueHandler());
        return message;
    }

    @Override
    public void detach(DetachableMessage detachableMessage) throws BrokerException {
        queue.detach(detachableMessage);
    }

    @Override
    public void prepareDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException {
        queue.prepareDetach(xid, detachableMessage);
    }

    @Override
    public int clear() {
        return queue.clear();
    }

    private void publishQueueLimitReachedEvent(String type, QueueHandler queueHandler) {
        int queueSize = queueHandler.size();
        if (messageLimits.contains(queueSize)) {
            Map<String, String> properties = new HashMap<>();
            String queueName = queueHandler.getUnmodifiableQueue().getName();
            String isAutoDelete = String.valueOf(queueHandler.getUnmodifiableQueue().isAutoDelete());
            String isDurable = String.valueOf(queueHandler.getUnmodifiableQueue().isDurable());
            properties.put("queueName", queueName);
            properties.put("autoDelete", isAutoDelete);
            properties.put("durable", isDurable);
            properties.put("messageCount", String.valueOf(queueHandler.size()));
            String id = type + queueName + "." + queueSize;
            eventSync.publish(id, properties);
        }
    }
}