package io.camunda.example.integration;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

@SpringBootTest(
    classes = {TestConnectorRuntimeApplication.class},
    properties = "camunda.client.mode=self-managed",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamundaSpringProcessTest
public class MyConnectorIntegrationTest {

  @Autowired
  private CamundaClient client;

  @Test
  void testMyConnectorFunctionality() {
    // given - start a process instance with input variables
    final var input1 = "Hello";
    final var input2 = "from Test!";
    final var processInstance =
        client
            .newCreateInstanceCommand()
            // processes in resources/bpmn are automatically deployed
            .bpmnProcessId("operation-connector-test-process")
            .latestVersion()
            .variables(Map.of("input1", input1, "input2", input2))
            .send()
            .join();

    // when - wait for the process to complete
    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byName("MyConnector"))
        // then - verify the result variable
        .hasVariable("result", Map.of("concatenationResult", input1 + " " + input2))
        .isCompleted();
  }

  @Test
  void testConnectorWithEmptyFirstInput() {
    // given - start a process instance with input variables
    final var processInstance =
            client
                    .newCreateInstanceCommand()
                    // processes in resources/bpmn are automatically deployed
                    .bpmnProcessId("operation-connector-test-process")
                    .latestVersion()
                    .variables(Map.of("input1", "", "input2", "World"))
                    .send()
                    .join();

    // then - verify that an incident was created
    assertThatProcessInstance(processInstance).hasActiveIncidents();
  }

  @Test
  void testConnectorWithEmptySecondInput() {
    // given - start a process instance with input variables
    final var processInstance =
            client
                    .newCreateInstanceCommand()
                    // processes in resources/bpmn are automatically deployed
                    .bpmnProcessId("operation-connector-test-process")
                    .latestVersion()
                    .variables(Map.of("input1", "Hello", "input2", ""))
                    .send()
                    .join();

    // then - verify that an incident was created
    assertThatProcessInstance(processInstance).hasActiveIncidents();
  }
}
