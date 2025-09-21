# OpenFeature Java SDK v2.0.0 - Compatibility Layer Guide

## 🎯 Overview

This compatibility layer provides backward compatibility for OpenFeature Java SDK v2.0.0, allowing existing code to continue working with minimal changes while encouraging migration to the new API structure.

## ⚠️ Important Notice

**All classes and interfaces in this compatibility layer are marked as `@Deprecated(since = "2.0.0", forRemoval = true)` and will be removed in version 2.1.0.**

## 🛡️ What's Provided

### ✅ **Immediate Compatibility** (Works out of the box)

#### Interface Aliases
```java
// These continue to work with deprecation warnings
FeatureProvider provider = new MyProvider();  // ✅ Works, but deprecated
Features client = OpenFeature.getClient();    // ✅ Works, but deprecated
Client client2 = OpenFeature.getClient();     // ✅ Works, but deprecated
```

#### Enum/Constant Re-exports
```java
// These continue to work exactly as before
ErrorCode code = ErrorCode.PROVIDER_NOT_READY;  // ✅ Works
String reason = Reason.DEFAULT;                 // ✅ Works
FlagValueType type = FlagValueType.BOOLEAN;     // ✅ Works
```

#### Exception Classes
```java
// Exception handling continues to work
throw new GeneralError("Something went wrong");      // ✅ Works
throw new ProviderNotReadyError("Not ready");       // ✅ Works
throw new FatalError("Fatal error occurred");       // ✅ Works
```

### ⚠️ **Partial Compatibility** (Works with limitations)

#### Immutable Object Construction
```java
// Constructor usage works - creates immutable objects
ProviderEvaluation<String> eval = new ProviderEvaluation<>();           // ✅ Works
FlagEvaluationDetails<String> details = new FlagEvaluationDetails<>();  // ✅ Works
ImmutableContext context = ImmutableContext.builder().build();          // ✅ Works
ImmutableMetadata metadata = ImmutableMetadata.builder().build();       // ✅ Works
```

#### Builder Patterns (Preferred)
```java
// Builder usage works exactly as before (recommended)
ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
    .value("test")
    .variant("variant1")
    .build();  // ✅ Works
```

### ❌ **Breaking Changes** (Requires code changes)

#### Setter Methods on Immutable Objects
```java
// These now throw UnsupportedOperationException with helpful messages
ProviderEvaluation<String> eval = new ProviderEvaluation<>();
eval.setValue("test");  // ❌ Throws exception with migration guidance
```

## 🔄 Migration Strategy

### Phase 1: **Update Dependencies** (Required)
```xml
<!-- Update your POM dependency -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>2.0.0</version>  <!-- Updated version -->
</dependency>
```

### Phase 2: **Fix Breaking Changes** (Required immediately)
```java
// BEFORE: Mutable pattern (will fail)
ProviderEvaluation<String> eval = new ProviderEvaluation<>();
eval.setValue("test");        // ❌ Throws UnsupportedOperationException
eval.setVariant("variant1");  // ❌ Throws UnsupportedOperationException

// AFTER: Immutable pattern (works)
ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
    .value("test")
    .variant("variant1")
    .build();  // ✅ Works
```

### Phase 3: **Update Imports** (Gradual migration)
```java
// BEFORE: Compatibility imports (deprecated warnings)
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Features;
import dev.openfeature.sdk.ProviderEvaluation;

// AFTER: New API imports (no warnings)
import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationClient;
import dev.openfeature.api.evaluation.ProviderEvaluation;
```

### Phase 4: **Update Interface Names** (Before v2.1.0)
```java
// BEFORE: Deprecated interfaces
public class MyProvider implements FeatureProvider { }
Features client = OpenFeature.getClient();

// AFTER: New interface names
public class MyProvider implements Provider { }
EvaluationClient client = OpenFeature.getClient();
```

## 🛠️ Common Migration Patterns

### Pattern 1: **Provider Implementation**
```java
// BEFORE (v1.x)
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;

public class MyProvider implements FeatureProvider {
    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        ProviderEvaluation<String> eval = new ProviderEvaluation<>();
        eval.setValue("result");
        eval.setReason("DEFAULT");
        return eval;
    }
}

// COMPATIBILITY LAYER (v2.0 - works with warnings)
import dev.openfeature.sdk.FeatureProvider;  // ⚠️ Deprecated
import dev.openfeature.sdk.ProviderEvaluation; // ⚠️ Deprecated

public class MyProvider implements FeatureProvider {  // ⚠️ Deprecated
    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        // ✅ This works but uses builder pattern internally
        return ProviderEvaluation.<String>builder()
            .value("result")
            .reason("DEFAULT")
            .build();
    }
}

// FULLY MIGRATED (v2.0+ recommended)
import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.ProviderEvaluation;

public class MyProvider implements Provider {
    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder()
            .value("result")
            .reason("DEFAULT")
            .build();
    }
}
```

### Pattern 2: **Client Usage**
```java
// BEFORE (v1.x)
import dev.openfeature.sdk.Features;

Features client = OpenFeature.getClient();
String value = client.getStringValue("flag-key", "default");

// COMPATIBILITY LAYER (v2.0 - works with warnings)
import dev.openfeature.sdk.Features;  // ⚠️ Deprecated

Features client = OpenFeature.getClient();  // ⚠️ Deprecated warning
String value = client.getStringValue("flag-key", "default");  // ✅ Works

// FULLY MIGRATED (v2.0+ recommended)
import dev.openfeature.api.evaluation.EvaluationClient;

EvaluationClient client = OpenFeature.getClient();
String value = client.getStringValue("flag-key", "default");
```

### Pattern 3: **Metadata Building**
```java
// BEFORE (v1.x with Lombok)
import dev.openfeature.sdk.ImmutableMetadata;

ImmutableMetadata metadata = ImmutableMetadata.builder()
    .addString("version", "1.0")
    .addInteger("timeout", 5000)
    .build();

// COMPATIBILITY LAYER (v2.0 - works with warnings)
import dev.openfeature.sdk.ImmutableMetadata;  // ⚠️ Deprecated

ImmutableMetadata metadata = ImmutableMetadata.builder()  // ⚠️ Deprecated
    .addString("version", "1.0")      // ⚠️ Deprecated method
    .addInteger("timeout", 5000)      // ⚠️ Deprecated method
    .build();

// FULLY MIGRATED (v2.0+ recommended)
import dev.openfeature.api.types.ImmutableMetadata;

ImmutableMetadata metadata = ImmutableMetadata.builder()
    .string("version", "1.0")         // ✅ New method names
    .integer("timeout", 5000)         // ✅ New method names
    .build();
```

## 🚨 Error Messages Guide

When using deprecated setter methods, you'll see helpful error messages:

```java
ProviderEvaluation<String> eval = new ProviderEvaluation<>();
eval.setValue("test");
// UnsupportedOperationException:
// "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().value(value).build() instead.
//  See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
```

## 📋 Migration Checklist

### Immediate Actions (Required for v2.0)
- [ ] Update Maven dependency to `dev.openfeature:sdk:2.0.0`
- [ ] Replace all setter usage with builder patterns
- [ ] Test your application thoroughly
- [ ] Fix any compilation errors

### Gradual Migration (Before v2.1.0)
- [ ] Update import statements to use new packages
- [ ] Change `FeatureProvider` to `Provider`
- [ ] Change `Features` to `EvaluationClient`
- [ ] Update metadata builder method names (`addString` → `string`)
- [ ] Remove any usage of deprecated convenience methods

### Verification Steps
- [ ] All deprecation warnings resolved
- [ ] No `UnsupportedOperationException` errors in tests
- [ ] All imports use `dev.openfeature.api.*` packages
- [ ] Code compiles without warnings

## 🆘 Getting Help

1. **Documentation**: [OpenFeature Java SDK v2 Migration Guide](https://docs.openfeature.dev/java-sdk/v2-migration)
2. **GitHub Issues**: [Report migration issues](https://github.com/open-feature/java-sdk/issues)
3. **Stack Overflow**: Tag questions with `openfeature` and `java`

## ⏰ Timeline

- **v2.0.0**: Compatibility layer available, deprecation warnings
- **v2.1.0**: Compatibility layer removed, breaking changes

**Migrate before v2.1.0 to avoid compilation failures.**