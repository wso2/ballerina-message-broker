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
import io.ballerina.messaging.broker.common.ValidationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an Exchange Registry which trigger events for the broker.
 */
public class ObservableExchangeRegistryImpl extends ExchangeRegistry {
    private final ExchangeRegistryImpl exchangeRegistry;
    private final EventSync eventSync;

    ObservableExchangeRegistryImpl(ExchangeRegistryImpl exchangeRegistry, EventSync eventSync) {
        this.exchangeRegistry = exchangeRegistry;
        this.eventSync = eventSync;
    }

    @Override
    public Exchange getExchange(String exchangeName) {
        return exchangeRegistry.getExchange(exchangeName);
    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        boolean exchangeDeleted = exchangeRegistry.deleteExchange(exchangeName, ifUnused);
        if (exchangeDeleted) {
            publishExchangeEvent("exchange.deleted", exchange);
        }
        return exchangeDeleted;
    }

    @Override
    public void declareExchange(String exchangeName, String type,
                                   boolean passive, boolean durable) throws ValidationException, BrokerException {
        if (exchangeName.isEmpty()) {
            throw new ValidationException("Exchange name cannot be empty.");
        }

        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        if (passive) {
            if (Objects.isNull(exchange)) {
                throw new ValidationException("Exchange [ " + exchangeName + " ] doesn't exists. Passive parameter "
                        + "is set, hence not creating the exchange.");
            }
        } else {
            createExchange(exchangeName, Exchange.Type.from(type), durable);
        }
    }

    @Override
    public void createExchange(String exchangeName, Exchange.Type type, boolean durable)
            throws BrokerException, ValidationException {
        exchangeRegistry.createExchange(exchangeName, type, durable);
        publishExchangeEvent("exchange.created", exchangeRegistry.getExchange(exchangeName));
    }

    @Override
    public Exchange getDefaultExchange() {
        return exchangeRegistry.getDefaultExchange();
    }

    @Override
    public void retrieveFromStore(QueueRegistry queueRegistry) throws BrokerException {
        exchangeRegistry.retrieveFromStore(queueRegistry);
    }

    @Override
    public Collection<Exchange> getAllExchanges() {
        return exchangeRegistry.getAllExchanges();
    }

    @Override
    public void reloadExchangesOnBecomingActive(QueueRegistry queueRegistry) throws BrokerException {
        exchangeRegistry.retrieveFromStore(queueRegistry);
    }

    private void publishExchangeEvent(String id, Exchange exchange) {
        Map<String, String> properties = new HashMap<>();
        properties.put("exchangeName", exchange.getName());
        properties.put("type", exchange.getType().toString());
        properties.put("durable", String.valueOf(exchange.isDurable()));
        eventSync.publish(id, properties);
    }
}