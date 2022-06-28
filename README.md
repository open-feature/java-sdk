# OpenFeature SDK for Java

[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
[![Known Vulnerabilities](https://snyk.io/test/github/open-feature/java-sdk/badge.svg)](https://snyk.io/test/github/open-feature/java-sdk)
[![on-merge](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml/badge.svg)](https://github.com/open-feature/java-sdk/actions/workflows/merge.yml)
[![codecov](https://codecov.io/gh/open-feature/java-sdk/branch/main/graph/badge.svg?token=XMS9L7PBY1)](https://codecov.io/gh/open-feature/java-sdk)


This is the Java implementation of [OpenFeature](https://openfeature.dev), a vendor-agnostic abstraction library for evaluating feature flags.

We support multiple data types for flags (numbers, strings, booleans, objects) as well as  hooks, which can alter the lifecycle of a flag evaluation.

This library is intended to be used in server-side contexts and has not been evaluated for use in mobile devices.

## Usage

While `Boolean` provides the simplest introduction, we offer a variety of flag types. 

```java
class MyClass {
    public UI booleanExample() {
        // Should we render the redesign? Or the default webpage? 
        if (client.getBooleanValue("redesign_enabled", false)) {
            return render_redesign();
        }
        return render_normal();
    }
    
    public Template stringExample() {
        // Get the template to load for the custom new homepage
        String template = client.getStringValue("homepage_template", "default-homepage.html")
        return render_template(template);
    }
    
    public List<Module> numberExample() {
        // How many modules should we be fetching?
        Integer count = client.getIntegerValue("module-fetch-count", 4);
        return fetch_modules(count);
    }
    
    public Module structureExample() {
        // This deserializes into the Module structure for you.
        Module heroModule = client.getObjectValue("hero-module", myExampleModule);
        return heroModule;
    }
}
```

## Requirements
- Java 8+

## Installation

### Add it to your build

Maven:
```xml
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>javasdk</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Gradle:
```groovy
dependencies {
    implementation 'dev.openfeature:javasdk:0.0.1-SNAPSHOT'
}
```

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

## Contributors

Thanks so much to our contributors.

<a href="https://github.com/open-feature/java-sdk/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-feature/java-sdk" />
</a>

Made with [contrib.rocks](https://contrib.rocks).