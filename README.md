<!-- markdownlint-disable MD033 -->
<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/open-feature/community/0e23508c163a6a1ac8c0ced3e4bd78faafe627c7/assets/logo/horizontal/white/openfeature-horizontal-white.svg">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/open-feature/community/0e23508c163a6a1ac8c0ced3e4bd78faafe627c7/assets/logo/horizontal/black/openfeature-horizontal-black.svg">
    <img align="center" alt="OpenFeature Logo">
  </picture>
</p>

<h2 align="center">OpenFeature Java SDK</h2>

[![Specification](https://img.shields.io/static/v1?label=Specification&message=v0.6.0&color=yellow)](https://github.com/open-feature/spec/tree/v0.6.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk)
[![javadoc](https://javadoc.io/badge2/dev.openfeature/sdk/javadoc.svg)](https://javadoc.io/doc/dev.openfeature/sdk) 
[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
[![Known Vulnerabilities](https://snyk.io/test/github/open-feature/java-sdk/badge.svg)](https://snyk.io/test/github/open-feature/java-sdk)
[![on-merge](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml/badge.svg)](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml)
[![codecov](https://codecov.io/gh/open-feature/java-sdk/branch/main/graph/badge.svg?token=XMS9L7PBY1)](https://codecov.io/gh/open-feature/java-sdk)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/6241/badge)](https://bestpractices.coreinfrastructure.org/projects/6241)

## 👋 Hey there! Thanks for checking out the OpenFeature Java SDK

### What is OpenFeature?

[OpenFeature][openfeature-website] is an open standard that provides a vendor-agnostic, community-driven API for feature flagging that works with your favorite feature flag management tool.

### Why standardize feature flags?

Standardizing feature flags unifies tools and vendors behind a common interface which avoids vendor lock-in at the code level. Additionally, it offers a framework for building extensions and integrations and allows providers to focus on their unique value proposition.

## 🔍 Requirements

- Java 8+ (compiler target is 1.8)

Note that this library is intended to be used in server-side contexts and has not been evaluated for use in mobile devices.

## 📦 Installation

### Maven

<!-- x-release-please-start-version -->
```xml
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>1.4.0</version>
</dependency>
```
<!-- x-release-please-end-version -->

If you would like snapshot builds, this is the relevant repository information:

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <id>sonartype</id>
        <name>Sonartype Repository</name>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
</repositories>
```

### Gradle
<!-- x-release-please-start-version -->
```groovy
dependencies {
    implementation 'dev.openfeature:sdk:1.4.0'
}
```
<!-- x-release-please-end-version -->

### Software Bill of Materials (SBOM)

We publish SBOMs with all of our releases as of 0.3.0. You can find them in Maven Central alongside the artifacts.

## 🌟 Features

- support for various backend [providers](https://openfeature.dev/docs/reference/concepts/provider)
- easy integration and extension via [hooks](https://openfeature.dev/docs/reference/concepts/hooks)
- bool, string, numeric, and object flag types
- [context-aware](https://openfeature.dev/docs/reference/concepts/evaluation-context) evaluation

## 🚀 Usage

```java
public void example(){

    // configure a provider
    OpenFeatureAPI api = OpenFeatureAPI.getInstance();
    api.setProvider(new MyProviderOfChoice());

    // create a client
    Client client = api.getClient();

    // get a bool flag value
    boolean flagValue = client.getBooleanValue("boolFlag", false);
}
```

### Context-aware evaluation

Sometimes the value of a flag must take into account some dynamic criteria about the application or user, such as the user location, IP, email address, or the location of the server.
In OpenFeature, we refer to this as [`targeting`](https://openfeature.dev/specification/glossary#targeting).
If the flag system you're using supports targeting, you can provide the input data using the `EvaluationContext`.

```java
// global context for static data
OpenFeatureAPI api = OpenFeatureAPI.getInstance();
Map<String, Value> attributes = new HashMap<>();
attributes.put("appVersion", new Value(System.getEnv("APP_VERSION")));
EvaluationContext apiCtx = new ImmutableContext(attributes);
api.setEvaluationContext(apiCtx);

// request context
Map<String, Value> attributes = new HashMap<>();
attributes.put("email", new Value(session.getAttribute("email")));
attributes.put("product", new Value(productId));
String targetingKey = session.getId();
EvaluationContext reqCtx = new ImmutableContext(targetingKey, attributes);

// use merged contextual data to determine a flag value
boolean flagValue = client.getBooleanValue("some-flag", false, reqCtx);
```

### Events

Events allow you to react to state changes in the provider or underlying flag management system, such as flag definition changes, provider readiness, or error conditions.
Initialization events (`PROVIDER_READY` on success, `PROVIDER_ERROR` on failure) are dispatched for every provider.
Some providers support additional events, such as `PROVIDER_CONFIGURATION_CHANGED`.
Please refer to the documentation of the provider you're using to see what events are supported.

```java
// add an event handler to a client
client.onProviderConfigurationChanged((EventDetails eventDetails) -> {
    // do something when the provider's flag settings change
});

// add an event handler to the global API
OpenFeatureAPI.getInstance().onProviderStale((EventDetails eventDetails) -> {
    // do something when the provider's cache goes stale
});
```

### Hooks

A hook is a mechanism that allows for adding arbitrary behavior at well-defined points of the flag evaluation life-cycle.
Use cases include validating the resolved flag value, modifying or adding data to the evaluation context, logging, telemetry, and tracking.

```java
public class MyHook implements Hook {
    /**
     *
     * @param ctx     Information about the particular flag evaluation
     * @param details Information about how the flag was resolved, including any resolved values.
     * @param hints   An immutable mapping of data for users to communicate to the hooks.
     */
    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        System.out.println("After evaluation!");
    }
}
```

See [here](https://openfeature.dev/ecosystem?instant_search%5BrefinementList%5D%5Btype%5D%5B0%5D=Hook&instant_search%5BrefinementList%5D%5Btechnology%5D%5B0%5D=Java) for a catalog of available hooks.

### Logging:

The Java SDK uses SLF4J. See the [SLF4J manual](https://slf4j.org/manual.html) for complete documentation.

### Named clients

Clients can be given a name.
A name is a logical identifier which can be used to associate clients with a particular provider.
If a name has no associated provider, clients with that name use the global provider.

```java
FeatureProvider scopedProvider = new MyProvider();

// set this provider for clients named "my-name"
OpenFeatureAPI.getInstance().setProvider("my-name", provider);

// create a client bound to the provider above
Client client = OpenFeatureAPI.getInstance().getClient("my-name");
```

### Providers:

To develop a provider, you need to create a new project and include the OpenFeature SDK as a dependency.
This can be a new repository or included in [the existing contrib repository](https://github.com/open-feature/java-sdk-contrib) available under the OpenFeature organization.
Finally, you’ll then need to write the provider itself.
This can be accomplished by implementing the `FeatureProvider` interface exported by the OpenFeature SDK.

```java
public class MyProvider implements FeatureProvider {
@Override
    public Metadata getMetadata() {
        return () -> "My Provider";
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        // resolve a boolean flag value
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        // resolve a string flag value
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        // resolve an int flag value
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        // resolve a double flag value
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        // resolve an object flag value
    }
}
```

See [here](https://openfeature.dev/ecosystem?instant_search%5BrefinementList%5D%5Btype%5D%5B0%5D=Provider&instant_search%5BrefinementList%5D%5Btechnology%5D%5B0%5D=Java) for a catalog of available providers.

### Shutdown

The OpenFeature API provides a close function to perform a cleanup of all registered providers.
This should only be called when your application is in the process of shutting down.

```java
// shut down all providers
OpenFeatureAPI.getInstance().shutdown();
```

### Complete API documentation:

See [here](https://www.javadoc.io/doc/dev.openfeature/sdk/latest/index.html) for the complete API documentation.

## ⭐️ Support the project

- Give this repo a ⭐️!
- Follow us on social media:
  - Twitter: [@openfeature](https://twitter.com/openfeature)
  - LinkedIn: [OpenFeature](https://www.linkedin.com/company/openfeature/)
- Join us on [Slack](https://cloud-native.slack.com/archives/C0344AANLA1)
- For more, check out our [community page](https://openfeature.dev/community/)

## 🤝 Contributing

Interested in contributing? Great, we'd love your help! To get started, take a look at the [CONTRIBUTING](CONTRIBUTING.md) guide.

### Thanks to everyone that has already contributed

<a href="https://github.com/open-feature/java-sdk/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-feature/java-sdk" alt="Pictures of the folks who have contributed to the project" />
</a>

Made with [contrib.rocks](https://contrib.rocks).

## 📜 License

[Apache License 2.0](LICENSE)

[openfeature-website]: https://openfeature.dev
