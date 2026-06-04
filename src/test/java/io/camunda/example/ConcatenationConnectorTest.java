package io.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.runtime.core.outbound.operation.ConnectorOperations;
import io.camunda.connector.runtime.core.outbound.operation.OutboundConnectorOperationFunction;
import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.connector.validation.impl.DefaultValidationProvider;
import io.camunda.example.model.ConcatenationConnectorResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ConcatenationConnectorTest {

  @Test
  void shouldReturnExpectedResultWhenExecute() throws Exception {
    // given
    var connector = new ConcatenationConnector();
    var operations =
        ConnectorOperations.from(connector, new ObjectMapper(), new DefaultValidationProvider());
    var function = new OutboundConnectorOperationFunction(operations);

    var context = OutboundConnectorContextBuilder.create()
        .variables(Map.of(
            "input1", "my_input1_value",
            "input2", "my_input2_value"))
        .header("operation", "concatenate")
        .build();
    // when
    var result = function.execute(context);
    // then
    assertThat(result)
        .isInstanceOf(ConcatenationConnectorResult.class)
        .extracting("concatenationResult")
        .isEqualTo("my_input1_value my_input2_value");
  }

}