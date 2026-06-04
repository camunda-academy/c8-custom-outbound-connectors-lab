package io.camunda.example;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.example.model.ConcatenationConnectorRequest;
import io.camunda.example.model.ConcatenationConnectorResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@OutboundConnector(
    name = "concatenation-connector",
    inputVariables = {"input1", "input2"},
    type = "io.camunda:concatenation-api:1")
public class ConcatenationConnector implements OutboundConnectorProvider {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConcatenationConnector.class);

  @Operation(id = "concatenate", name = "Concatenate strings")
  public ConcatenationConnectorResult concatenate(
      @Variable ConcatenationConnectorRequest connectorRequest) {

    LOGGER.info("Executing my connector with request {}", connectorRequest);

    String concatenationResult =
        connectorRequest.input1() + " " + connectorRequest.input2();

    var result = new ConcatenationConnectorResult(concatenationResult);

    LOGGER.info("Connector executed with result {}", result);
    return result;
  }
}

