package io.camunda.example;

import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.example.model.ConcatenationConnectorRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcatenationRequestTest  {

  String input1, input2;

  @Test
  void shouldFailWhenValidate_NoInput1() {
      // given
      var input = new ConcatenationConnectorRequest(input1,input2);

      var context = OutboundConnectorContextBuilder
        .create()
        .variables(input).build();
      // when
      assertThatThrownBy(() -> context.bindVariables(ConcatenationConnectorRequest.class))
        // then
        .isInstanceOf(ConnectorInputException.class)
        .hasMessageContaining("input1");
  }

  @Test
  void shouldFailWhenValidate_NoInput2() {
      // given
      var input = new ConcatenationConnectorRequest(input1,input2);

      var context = OutboundConnectorContextBuilder
        .create()
        .variables(input).build();
      // when
      assertThatThrownBy(() -> context.bindVariables(ConcatenationConnectorRequest.class))
        // then
        .isInstanceOf(ConnectorInputException.class)
        .hasMessageContaining("input2");
  }
  
}
