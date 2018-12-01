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
package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression.This class is doing a less than or equal comparison between left and right
 * values provided and evaluate to a boolean value.
 */
public class LessThanOrEqualExpression implements BooleanExpression {

    private final Expression<Metadata> left;

    private final Expression<Metadata> right;

    public LessThanOrEqualExpression (Expression<Metadata> left , Expression<Metadata> right) {
        this.left = left;
        this.right = right;
    }
    @Override
    public boolean evaluate (Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        Object rightValue = right.evaluate(metadata);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        if (leftValue instanceof Number) {
            long l = ((Number) leftValue).longValue();
            long l1 = ((Number) rightValue).longValue();
            return l < l1 || l == l1;
        }
        return false;
    }
}