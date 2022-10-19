# OpenFeature SDK for Java

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk)
[![javadoc](https://javadoc.io/badge2/dev.openfeature/sdk/javadoc.svg)](https://javadoc.io/doc/dev.openfeature/sdk) 
[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
[![v0.5.1](https://img.shields.io/static/v1?label=Specification&message=v0.5.1&color=yellow)](https://github.com/open-feature/spec/tree/v0.5.1)
[![Known Vulnerabilities](https://snyk.io/test/github/open-feature/java-sdk/badge.svg)](https://snyk.io/test/github/open-feature/java-sdk)
[![on-merge](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml/badge.svg)](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml)
[![codecov](https://codecov.io/gh/open-feature/java-sdk/branch/main/graph/badge.svg?token=XMS9L7PBY1)](https://codecov.io/gh/open-feature/java-sdk)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/6241/badge)](https://bestpractices.coreinfrastructure.org/projects/6241)

This is the Java implementation of [OpenFeature](https://openfeature.dev), a vendor-agnostic abstraction library for evaluating feature flags.

We support multiple data types for flags (numbers, strings, booleans, objects) as well as  hooks, which can alter the lifecycle of a flag evaluation.

This library is intended to be used in server-side contexts and has not been evaluated for use in mobile devices.

## Usage

```java
import dev.openfeature.javasdk.OpenFeatureAPI;

// configure a provider
OpenFeatureAPI api = OpenFeatureAPI.getInstance();
api.setProvider(new YourProviderOfChoice());

// create a client
Client client = api.getClient("my-app");

// get a boolean value
Boolean boolValue = client.getBooleanValue("boolFlag", false);

// get a string value
String stringValue = client.getStringValue("stringFlag", "default");

// get an integer value
Integer intValue = client.getIntegerValue("intFlag", 1);

// get a double value
Double doubleValue = client.getDoubleValue("doubleFlag", 0.9);

// get an object value
Value objectValue = client.getObjectValue("objectFlag", MyObjectInstance);

// get details of boolean evaluation
FlagEvaluationDetails<Boolean> boolDetails = client.getBooleanDetails("boolFlag", false);

// get details of string evaluation
FlagEvaluationDetails<String> stringDetails = client.getStringDetails("stringFlag", "default");

// get details of integer evaluation
FlagEvaluationDetails<Integer> intDetails = client.getIntegerDetails("intFlag", 1);

// get details of double evaluation
FlagEvaluationDetails<Doulbe> doubleDetails = client.getDoubleDetails("doubleFlag", .9);

// get details of object evaluation
FlagEvaluationDetails<Value> objectDetails = client.getObjectDetails<MyObject>("objectFlag", myObjectDefaultInstance);

```

For complete documentation visit https://docs.openfeature.dev/docs/category/concepts.

## Requirements
- Java 8+

## Installation

### Add it to your build

#### Maven
<!-- x-release-please-start-version -->
```xml
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>0.3.1</version>
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

#### Gradle
<!-- x-release-please-start-version -->
```groovy
dependencies {
    implementation 'dev.openfeature:sdk:0.3.1'
}
```
<!-- x-release-please-end-version -->

### Configure it
To configure it, you'll need to add a provider to the global singleton `OpenFeatureAPI`. From there, you can generate a `Client` which is usable by your code. While you'll likely want a provider for your specific backend, we've provided a `NoOpProvider`, which simply returns the default passed in.
```java
class MyApp {
    public void example(){
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        // Now use your `client` instance to evaluate some feature flags!
    }
}
```
## Contacting us
We hold regular meetings which you can see [here](https://github.com/open-feature/community/#meetings-and-events).

We are also present on the `#openfeature` channel in the [CNCF slack](https://slack.cncf.io/).

## Developing

### Integration tests

The continuous integration runs a set of [gherkin integration tests](https://github.com/open-feature/test-harness/blob/main/features/evaluation.feature) using [`flagd`](https://github.com/open-feature/flagd). These tests do not run with the default maven profile. If you'd like to run them locally, you can start the flagd testbed with `docker run -p 8013:8013 ghcr.io/open-feature/flagd-testbed:latest` and then run `mvn test -P integration-test`.

## Releasing

See [releasing](./docs/release.md).

### Software Bill of Materials (SBOM)

We publish SBOMs with all of our releases as of 0.3.0. You can find them in Maven Central alongside the artifacts.

## Contributors

Thanks so much to our contributors.

<a href="https://github.com/open-feature/java-sdk/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-feature/java-sdk" alt="Pictures of the folks who have contributed to the project"/>
</a>

Made with [contrib.rocks](https://contrib.rocks).
