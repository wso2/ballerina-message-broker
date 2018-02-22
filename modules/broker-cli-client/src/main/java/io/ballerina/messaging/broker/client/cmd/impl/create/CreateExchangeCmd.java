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
package io.ballerina.messaging.broker.client.cmd.impl.create;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Exchange;
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;

/**
 * Command representing MB exchange creation.
 */
@Parameters(commandDescription = "Create an exchange in the Broker with parameters")
public class CreateExchangeCmd extends CreateCmd {

    @Parameter(description = "name of the exchange")
    private String exchangeName;

    @Parameter(names = { "--type", "-t" },
               description = "type of the exchange")
    private String type = "direct";

    @Parameter(names = { "--durable", "-d" },
               description = "durability of the exchange")
    private boolean durable = false;

    public CreateExchangeCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Configuration configuration = Utils.readConfigurationFile();
        HttpClient httpClient = new HttpClient(configuration);
        String urlSuffix = "exchanges/";

        Exchange exchange = new Exchange(exchangeName, type, durable);

        // do POST
        HttpResponse response = httpClient
                .sendHttpRequest(new HttpRequest(urlSuffix, exchange.getAsJsonString()), "POST");

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
            Message message = buildResponseMessage(response, "Exchange created successfully");
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " create exchange [exchange-name] [flag]*\n");
    }
}