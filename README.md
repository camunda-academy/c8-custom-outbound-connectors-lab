# Camunda 8 Custom Outbound Connectors Lab

A hands-on lab for building a custom **outbound** connector with the Camunda Connector SDK.
The example implements a **`ConcatenationConnector`** that concatenates two input strings and
returns the combined result. It uses the **annotations-based Operations API** (`OutboundConnectorProvider`, `@Operation`).

> This lab is part of the [Camunda 8 Custom Outbound Connectors](https://academy.camunda.com/c8-custom-outbound-connectors) course on Camunda Academy.

> Looking for the inbound counterpart? See the
> [connector-template-inbound](https://github.com/camunda/connector-template-inbound) repo.

---

## Contents

- [Use this template](#use-this-template)
- [5-minute Quickstart](#5-minute-quickstart)
- [Operations API vs Classic Function API — which to pick](#operations-api-vs-classic-function-api--which-to-pick)
- [Build](#build)
- [Run locally](#run-locally)
  - [Local Connector Runtime (recommended for development)](#local-connector-runtime-recommended-for-development)
  - [Bundled Docker runtime (closer to production)](#bundled-docker-runtime-closer-to-production)
  - [SaaS](#saas)
- [Testing](#testing)
- [Element template — tips and do/don't](#element-template--tips-and-dodont)
- [Web Modeler vs Desktop Modeler](#web-modeler-vs-desktop-modeler)
- [Troubleshooting](#troubleshooting)
- [Versions and compatibility](#versions-and-compatibility)

---

## Use this template

Click **Use this template** on GitHub, then rename in the new repo:

| File | What to change |
|---|---|
| `pom.xml` | `<artifactId>`, `<name>`, `<description>`, `<groupId>` if needed |
| `src/main/java/io/camunda/example/ConcatenationConnector.java` | rename class; update `@OutboundConnector(name, type)` and `@ElementTemplate(id, name, version, description, icon, documentationRef)` |
| `src/main/resources/META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider` | match new fully-qualified class name (one line) |
| `pom.xml` element-template-generator config (`<connectorClass>`, `<templateId>`, `<templateFileName>`) | match new connector class and template id |
| `README.md`, `LICENSE` | your project metadata |

The element template (`element-templates/<your-template>.json`) is **regenerated** by `mvn package`,
do not edit it by hand.

See the official guide on [creating a custom connector](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/).

---

## 5-minute Quickstart

```bash
# 1. clone your fork
git clone https://github.com/<you>/<your-connector>.git
cd <your-connector>

# 2. build, run unit + integration tests, generate element template
mvn clean package

# 3. start the local runtime (uses an embedded Camunda for testing)
mvn -Dexec.mainClass=io.camunda.example.LocalConnectorRuntime test-compile exec:java
```

Then upload `element-templates/Concatenation connector.json` to your Modeler, drop a service task,
and pick **Concatenation Connector**. See [Run locally](#run-locally) for the full picture.

---

## Operations API vs Classic Function API — which to pick

The SDK supports two styles. **Default to the Operations API** unless you have a reason not to.

| | **Operations API** (`OutboundConnectorProvider` + `@Operation`) | **Classic Function API** (`OutboundConnectorFunction.execute`) |
|---|---|---|
| Multiple operations per connector | Native — one `@Operation` per method, dispatched via the `operation` header | Manual — you switch on a discriminator field yourself |
| Variable binding | Per-parameter (`@Variable`, `@Header`) — typed | You call `context.bindVariables(MyInput.class)` |
| Element template | Generated from `@TemplateProperty` on each operation's input record | Generated from a single input record |
| Boilerplate | Less | More |
| When you'd pick it | Most new connectors. Good fit when the connector wraps an API with several actions (HTTP-style: GET / POST / DELETE) | Single-purpose connectors, or when migrating an older connector and the rewrite isn't worth it |

This template uses the Operations API — see
[`ConcatenationConnector`](src/main/java/io/camunda/example/ConcatenationConnector.java). For a real-world example
covering more patterns, look at the
[CSV Connector](https://github.com/camunda/connectors/blob/main/connectors/csv/src/main/java/io/camunda/connector/csv/CsvConnector.java).

If you do want the Classic style, change the class to `implements OutboundConnectorFunction`,
update the SPI file to point at `io.camunda.connector.api.outbound.OutboundConnectorFunction`,
and remove `@Operation` annotations.

---

## Build

```bash
mvn clean package
```

Produces:
- a thin JAR
- a fat JAR (shaded). SDK artifacts are in `<scope>provided</scope>` and supplied by the runtime.
- the regenerated element template under `element-templates/`.

### Shading

`maven-shade-plugin` is preconfigured. Add `<relocations>` for libraries you ship that the runtime
also bundles (most commonly **Jackson**). Otherwise you'll see classpath errors like:

```
java.lang.NoSuchMethodError: com.fasterxml.jackson.databind.ObjectMapper.setSerializationInclusion(...)
```

See the [maven-shade docs on relocation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html).

---

## Run locally

You have three paths, in increasing fidelity to production:

### Local Connector Runtime (recommended for development)

`src/test/java/io/camunda/example/LocalConnectorRuntime.java` is a small Spring Boot app that
starts the connector runtime in-process and points it at a Zeebe gateway you supply.

**macOS / Linux:**
```bash
mvn test-compile exec:java -Dexec.mainClass=io.camunda.example.LocalConnectorRuntime \
  -Dexec.classpathScope=test
```

**Windows (PowerShell):**
```powershell
mvn test-compile exec:java "-Dexec.mainClass=io.camunda.example.LocalConnectorRuntime" `
  "-Dexec.classpathScope=test"
```

**Windows (cmd):**
```cmd
mvn test-compile exec:java -Dexec.mainClass=io.camunda.example.LocalConnectorRuntime -Dexec.classpathScope=test
```

Configure the Zeebe target in `src/test/resources/application.properties`. Without it, the runtime
falls back to the embedded test broker spun up by the integration tests.

### Bundled Docker runtime (closer to production)

For an environment closer to what runs in self-managed:

```bash
git clone git@github.com:camunda/camunda-distributions.git
cd camunda-distributions/docker-compose/versions/camunda-8.9
# either comment out the connectors service in docker-compose-core.yaml,
# or scale it to zero so the runtime you start locally is the one picking up jobs:
docker compose -f docker-compose-core.yaml up --scale connectors=0
```

Then start `LocalConnectorRuntime` as above. Operate is at http://localhost:8088/operate
(`demo` / `demo`).

> **Note**: this template targets Camunda **8.9.x**. Pin distributions to the matching minor —
> running against a different minor will surface as deserialization errors or incident messages
> about unknown headers.

### SaaS

1. https://console.camunda.io → create a cluster (latest 8.9 patch).
2. **API → Create new Client**, tick `Zeebe`, **Create**.
3. Copy the **Spring Boot** snippet into `src/test/resources/application.properties`.
4. Start `LocalConnectorRuntime` — the connector connects to your SaaS cluster.
5. In Web Modeler: create a project → **Create new** → **Upload files** → upload your
   `element-templates/<your-template>.json` → **Publish**. Then create a BPMN diagram in the same
   project and use the connector.

---

## Testing

Three layers, all run by `mvn clean verify`:

| Layer | File | Purpose |
|---|---|---|
| Unit (no runtime) | [`ConcatenationConnectorTest`](src/test/java/io/camunda/example/ConcatenationConnectorTest.java) | Happy-path test using `OutboundConnectorContextBuilder` from `connector-runtime-test`. Verifies that `input1` and `input2` are concatenated with a space. |
| Unit (no runtime) | [`ConcatenationRequestTest`](src/test/java/io/camunda/example/ConcatenationRequestTest.java) | Validation tests — ensures `@NotEmpty` constraints on `input1` and `input2` are enforced and raise `ConnectorInputException`. |
| Integration | [`MyConnectorIntegrationTest`](src/test/java/io/camunda/example/integration/MyConnectorIntegrationTest.java) | Spins up an embedded Camunda + connector runtime and runs a real BPMN process via `@CamundaSpringProcessTest`. |

The minimal pattern for unit-testing an annotations-based connector:

```java
var connector = new ConcatenationConnector();
var operations = ConnectorOperations.from(connector, new ObjectMapper(), new DefaultValidationProvider());
var function = new OutboundConnectorOperationFunction(operations);

var context = OutboundConnectorContextBuilder.create()
    .variables(Map.of("input1", "Hello", "input2", "World"))
    .header("operation", "concatenate")  // selects which @Operation to dispatch
    .build();

Object result = function.execute(context);
// result is a ConcatenationConnectorResult with concatenationResult = "Hello World"
```

The `operation` custom header is what the runtime uses to pick the `@Operation` method —
forgetting to set it produces `Operation ID is missing in the job context custom headers.`

---

## Element template — tips and do/don't

The template is generated from `@TemplateProperty` annotations on your input records. A few rules
that catch out most authors:

### Do

- **Typed defaults.** A boolean default is `defaultValue = "true"` with `type = PropertyType.Boolean`,
  not the string `"true"` on a Text property. Likewise numbers must use `PropertyType.Number`.
- **One `@Operation = one input record.** Discriminator (the `operation` header) is added by the
  generator. Don't model it yourself.
- **Group properties** with `@TemplateProperty(group = "...")`. Define the group label in
  `@ElementTemplate.PropertyGroups` so it shows in the Modeler panel.
- **Stable IDs.** `@ElementTemplate(id = "...", version = N)` — bump `version` when properties
  change in a breaking way; keep the `id` stable so existing diagrams find their template.
- **Icon.** SVG, square, monochrome-friendly, ~18×18 effective drawing area, no embedded fonts.
  Reference it by classpath path: `icon = "icon.svg"`.

### Don't

- **Don't** declare a property called `operation` yourself — it duplicates the discriminator the
  generator emits and the Modeler will show two "Operation" fields.
- **Don't** put validation only at runtime. Add Bean Validation annotations (`@NotEmpty`,
  `@NotNull`, `@Valid` on nested records) so the Modeler highlights missing values *before* deploy.
- **Don't** hand-edit the generated JSON. Any change is wiped on the next `mvn package`. Change
  the annotations instead.
- **Don't** reuse property IDs between operations. Two `@TemplateProperty(id = "url")` in
  different operations works *only* if you also set distinct `condition` blocks — easier to use
  distinct IDs.

Both should print nothing.

---

## Web Modeler vs Desktop Modeler

| | **Desktop Modeler** | **Web Modeler** |
|---|---|---|
| When to use | Iterating on the connector template itself — instant reload, no upload step | Sharing with non-developers, SaaS deploy targets, collaboration |
| Loading the template | **Settings → Element Templates → Add directory** pointing at `element-templates/` of this repo. Re-opens pick up regenerated JSON immediately | **Upload files** → select the JSON → **Publish** in the project. Re-publishing is required after every `mvn package` |
| Deploy target | Local self-managed (Docker runtime above) | SaaS or self-managed |
| Editing the template | The Desktop Modeler watches the directory — saving regenerates and reloads | Re-upload + re-publish per change |

A common workflow: develop and validate against Desktop Modeler with the local runtime, then
upload the same template to Web Modeler once it stabilises.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Operation ID is missing in the job context custom headers.` | The element template wasn't applied (or you wrote a hand-rolled task) so the `operation` header isn't set | In Modeler, apply the connector template to the service task. In tests, set `.header("operation", "<id>")`. |
| `No connector function found for type 'io.camunda:example:1'` | Runtime doesn't see your connector | Check that the SPI file `META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider` contains the FQ class name and is on the classpath of the runtime |
| `NullPointerException ... ValidationProvider.validate` in unit tests | Passed `null` to `ConnectorOperations.from(...)` | Pass `new DefaultValidationProvider()` (from `io.camunda.connector:connector-validation`) |
| `IncompatibleClassChangeError` against the runtime | Built against one minor, deployed against another | Match the SDK version (`<version.connectors>`) to the runtime minor |

---

## Versions and compatibility

This template currently pins:

| Component | Version | Property in `pom.xml` |
|---|---|---|
| Connector SDK & runtime libs | `8.9.1` | `version.connectors` |
| Camunda process-test | `8.9.0` | `version.camunda` |
| Java | 21 | `maven.compiler.release` |
| JUnit Jupiter | `6.0.2` | `version.junit-jupiter` |
| AssertJ | `3.27.4` | `version.assertj` |
| Mockito | `5.21.0` | `version.mockito` |

**Compatibility matrix**

| Connector SDK | Camunda runtime | Modeler (Desktop) | Modeler (Web) |
|---|---|---|---|
| `8.9.x` | `8.9.x` self-managed or SaaS | latest | latest |
| `8.8.x` | `8.8.x` | latest | latest |

Rule of thumb: the SDK's *minor* must match the connector runtime's minor. Patch versions are
freely swappable. When you upgrade, bump `version.connectors`, `version.camunda`, and the
`element-template-generator-maven-plugin` `version` together.

---

## API reference (this lab)

### `concatenate` — Concatenate strings

| Name | Description | Example |
|---|---|---|
| `input1` | First string to concatenate | `Hello` |
| `input2` | Second string to concatenate | `World` |

Output: `{ "concatenationResult": "Hello World" }` — the two inputs joined with a single space.

---

More: [Connectors SDK source](https://github.com/camunda/connectors) ·
[Custom Connector docs](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/)
