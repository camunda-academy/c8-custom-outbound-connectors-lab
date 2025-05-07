package io.camunda.example.dto;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.PropertyType;
import jakarta.validation.constraints.NotEmpty;

public record ConcatenationConnectorRequest(
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String input1,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String input2) {}