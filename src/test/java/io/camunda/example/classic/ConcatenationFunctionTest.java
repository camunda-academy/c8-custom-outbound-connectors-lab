package io.camunda.example.classic;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.dto.ConcatenationConnectorRequest;
import io.camunda.dto.ConcatenationConnectorResult;
import org.junit.jupiter.api.Test;

public class ConcatenationFunctionTest {

  @Test
  void shouldReturnExpectedResultWhenExecute() throws Exception {
  // given
  var input = new ConcatenationConnectorRequest("my_input1_value","my_input2_value");

  var function = new ConcatenationConnectorFunction();
  var context = OutboundConnectorContextBuilder.create()
    .variables(input)
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