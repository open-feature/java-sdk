# Breaking Changes - OpenFeature Java SDK v2.0.0

This document outlines all breaking changes introduced in the `feat/split-api-and-sdk` branch compared to the `main` branch (v1.18.0). These changes represent a major version bump to v2.0.0.

## 📊 Change Summary
- **32 commits** with comprehensive refactoring starting from v1.18.0
- **Complete architectural transformation** from single-module to multi-module Maven project
- **76 Java files** in original `src/main/java/dev/openfeature/sdk/` → **Split into 2 modules**
- **API module**: 84 files in `dev.openfeature.api` package structure
- **SDK module**: Implementation + compatibility layer + providers
- **Lombok completely removed** - replaced with hand-written builders
- **Full immutability transformation** - all POJOs now immutable with builders
- **Package reorganization** - interfaces moved from `dev.openfeature.sdk.*` to `dev.openfeature.api.*`
- **Comprehensive compatibility layer** for gradual migration (deprecated wrappers)
- **ServiceLoader integration** for API provider discovery

## 🏗️ Architecture Changes

### Module Structure & Maven Coordinates
**Breaking**: The monolithic SDK has been split into separate API and SDK modules with new Maven coordinates.

**Before (v1.18.0)**:
```xml
<!-- Single monolithic module -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>1.18.0</version>
</dependency>

<!-- All classes in dev.openfeature.sdk.* package -->
<!-- Example: FeatureProvider, OpenFeatureAPI, ProviderEvaluation, etc. -->
```

**After (v2.0.0)**:
```xml
<!-- For API-only usage (interfaces, POJOs, exceptions) -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>api</artifactId>
    <version>0.0.1</version> <!-- API module starts at 0.0.1 -->
</dependency>

<!-- For full SDK usage (includes API + implementation + compatibility layer) -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>2.0.0</version> <!-- SDK version is 2.0.0 -->
</dependency>

<!-- Classes now split across packages: -->
<!-- API: dev.openfeature.api.* (Provider, EvaluationClient, etc.) -->
<!-- SDK: dev.openfeature.sdk.* (OpenFeatureClient, providers, compatibility layer) -->
```

### Maven Project Structure
**Breaking**: Project structure changed from single module to multi-module Maven project.

**Before**: Single `pom.xml` with `artifactId=sdk`

**After**: Multi-module structure:
```
java-sdk/
├── pom.xml                    # Parent aggregator POM (artifactId: openfeature-java)
├── openfeature-api/
│   └── pom.xml               # API module POM (artifactId: api)
└── openfeature-sdk/
    └── pom.xml               # SDK module POM (artifactId: sdk)
```

**Parent POM Changes**:
- `artifactId` changed from `sdk` to `openfeature-java`
- `packaging` changed from `jar` to `pom`
- Added `<modules>` section with `openfeature-api` and `openfeature-sdk`
- Maintains all shared configuration (plugins, dependencies, etc.)

**Module Dependencies**:
- **API module** (`artifactId: api`): Standalone with minimal dependencies (SLF4J, SpotBugs annotations)
- **SDK module** (`artifactId: sdk`): Depends on API module and includes full implementation

**Migration**: 
- **Library Authors**: Switch to `dev.openfeature:api` for minimal dependencies
- **Application Developers**: Switch to `dev.openfeature:sdk` for full functionality (note: same `artifactId` but different structure)
- **Build Systems**: Update to reference new parent POM structure
- **CI/CD**: May need updates to handle multi-module Maven builds

---

## 🔒 POJO Immutability Changes

### ProviderEvaluation
**Breaking**: `ProviderEvaluation` transformed from Lombok `@Data` to immutable with builders.

**Before (v1.18.0 with Lombok)**:
```java
// Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
ProviderEvaluation<String> eval = new ProviderEvaluation<>();
eval.setValue("test");              // Lombok-generated setter
eval.setVariant("variant1");        // Lombok-generated setter
eval.setReason("DEFAULT");          // Lombok-generated setter

// Or Lombok builder
ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
    .value("test")
    .variant("variant1")
    .reason("DEFAULT")
    .build();
```

**After (v2.0.0 with hand-written builders)**:
```java
// Hand-written builder pattern only - no more Lombok
ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
    .value("test")
    .variant("variant1")
    .reason("DEFAULT")
    .errorCode(ErrorCode.NONE)
    .flagMetadata(metadata)
    .build();

// Object is immutable - no setters available
// eval.setValue("new"); // ❌ Compilation error - no Lombok setters
// Moved from dev.openfeature.sdk.ProviderEvaluation → dev.openfeature.api.evaluation.ProviderEvaluation
```

**Migration**: Replace constructor calls and setter usage with builder pattern.

### FlagEvaluationDetails
**Breaking**: `FlagEvaluationDetails` is now immutable with private constructors.

**Before**:
```java
// Public constructors
FlagEvaluationDetails<String> details = new FlagEvaluationDetails<>();
details.setFlagKey("my-flag");
details.setValue("test");

// Or constructor with parameters  
FlagEvaluationDetails<String> details = new FlagEvaluationDetails<>(
    "my-flag", "test", "variant1", "DEFAULT", ErrorCode.NONE, null, metadata);
```

**After**:
```java
// Builder pattern only
FlagEvaluationDetails<String> details = FlagEvaluationDetails.<String>builder()
    .flagKey("my-flag")
    .value("test") 
    .variant("variant1")
    .reason("DEFAULT")
    .build();
```

### EventDetails & ProviderEventDetails
**Breaking**: Constructor access removed, builder pattern required.

**Before**:
```java
ProviderEventDetails details = new ProviderEventDetails();
EventDetails event = new EventDetails("provider", "domain", details);
```

**After**:
```java
ProviderEventDetails details = ProviderEventDetails.builder()
    .message("Configuration changed")
    .flagsChanged(Arrays.asList("flag1", "flag2"))
    .build();
    
EventDetails event = EventDetails.builder()
    .providerName("provider")
    .domain("domain")
    .providerEventDetails(details)
    .build();
```

---

## 🏗️ Builder Pattern Changes

### Builder Class Names
**Breaking**: All builder class names standardized to `Builder`.

**Before**:
```java
ImmutableMetadata.ImmutableMetadataBuilder builder = ImmutableMetadata.builder();
FlagEvaluationDetails.FlagEvaluationDetailsBuilder<String> builder = 
    FlagEvaluationDetails.builder();
ProviderEvaluation.ProviderEvaluationBuilder<String> builder = 
    ProviderEvaluation.builder();
```

**After**:
```java
ImmutableMetadata.Builder builder = ImmutableMetadata.builder();
FlagEvaluationDetails.Builder<String> builder = FlagEvaluationDetails.builder();
ProviderEvaluation.Builder<String> builder = ProviderEvaluation.builder();
```

**Migration**: Update any explicit builder type references (rare in typical usage).

### Removed Convenience Methods
**Breaking**: Convenience methods removed in favor of consistent builder patterns.

**Before**:
```java
// Convenience methods
EventDetails details = EventDetails.fromProviderEventDetails(providerDetails);
HookContext<String> context = HookContext.from(otherContext);
FlagEvaluationDetails<String> details = FlagEvaluationDetails.from(evaluation);
```

**After**:
```java
// Builder pattern only
EventDetails details = EventDetails.builder()
    .providerEventDetails(providerDetails)
    .providerName(providerName)
    .build();
    
HookContext<String> context = HookContext.<String>builder()
    .flagKey(flagKey)
    .type(FlagValueType.STRING)
    .defaultValue(defaultValue)
    .build();
```

**Migration**: Replace convenience method calls with explicit builder usage.

---

## 📦 Package and Class Changes

### DefaultOpenFeatureAPI Encapsulation
**Breaking**: `DefaultOpenFeatureAPI` constructor is now package-private.

**Before**:
```java
// Direct instantiation possible (not recommended)
DefaultOpenFeatureAPI api = new DefaultOpenFeatureAPI();
```

**After**:
```java
// Package-private constructor - use factory methods
OpenFeatureAPI api = OpenFeature.getApi(); // Recommended approach
```

**Migration**: Use `OpenFeature.getApi()` instead of direct instantiation.

### Interface Reorganization & Package Changes
**Breaking**: Major package reorganization with new interface names and locations.

**Original Structure (v1.18.0)**:
```
src/main/java/dev/openfeature/sdk/
├── FeatureProvider.java           # Main provider interface
├── Features.java                  # Client interface
├── OpenFeatureAPI.java           # API singleton
├── ProviderEvaluation.java       # Evaluation result (Lombok @Data)
├── EvaluationContext.java        # Context interface
├── Value.java                    # Value type
├── ErrorCode.java                # Error enum
├── Hook.java                     # Hook interface
└── exceptions/
    ├── OpenFeatureError.java     # Base exception
    └── ...
```

**New Structure (v2.0.0)**:
```
openfeature-api/src/main/java/dev/openfeature/api/
├── Provider.java                  # Renamed from FeatureProvider
├── evaluation/
│   ├── EvaluationClient.java     # Renamed from Features
│   ├── ProviderEvaluation.java   # Moved here, now immutable
│   └── EvaluationContext.java    # Moved here
├── types/
│   └── Value.java                # Moved here
├── ErrorCode.java                # Moved here
├── lifecycle/
│   └── Hook.java                 # Moved here
└── exceptions/
    └── OpenFeatureError.java     # Moved here

openfeature-sdk/src/main/java/dev/openfeature/sdk/
├── FeatureProvider.java          # Deprecated wrapper → extends Provider
├── Features.java                 # Deprecated wrapper → extends EvaluationClient
├── OpenFeatureClient.java        # Implementation
├── compat/
│   └── CompatibilityGuide.java   # Migration helper
└── providers/memory/             # Concrete providers
```

**Interface Migration**:
- `dev.openfeature.sdk.FeatureProvider` → `dev.openfeature.api.Provider`
- `dev.openfeature.sdk.Features` → `dev.openfeature.api.evaluation.EvaluationClient`
- `dev.openfeature.sdk.OpenFeatureAPI` → `dev.openfeature.api.OpenFeatureAPI`

**EvaluationClient Optimization**:
- **Reduced from 30 methods to 10 abstract methods** + 20 default methods
- Default methods handle parameter delegation (empty context, default options)
- `get{Type}Value` methods now delegate to `get{Type}Details().getValue()`
- Massive reduction in boilerplate for implementers

**Migration**: Update import statements and leverage new default method implementations.

### ServiceLoader Integration
**Breaking**: New ServiceLoader pattern for provider discovery.

**New File**: `openfeature-sdk/src/main/resources/META-INF/services/dev.openfeature.api.OpenFeatureAPIProvider`

**Impact**: Enables automatic discovery of OpenFeature API implementations.

### Internal Class Movement
**Breaking**: Internal utility classes moved from API to SDK module.

**Moved Classes**:
- `AutoCloseableLock` → SDK module
- `AutoCloseableReentrantReadWriteLock` → SDK module
- `ObjectUtils` → SDK module
- `TriConsumer` → SDK module (kept in API for internal use)

**Migration**: These were internal classes - external usage should be minimal. If used, switch to SDK dependency.

---

## 🔧 API Consistency Changes

### Event Details Architecture
**Breaking**: Event details now use composition over inheritance.

**Before**:
```java
// EventDetails extended ProviderEventDetails
EventDetails details = new EventDetails(...);
details.getFlagsChanged(); // Inherited method
```

**After**:
```java  
// EventDetails composes ProviderEventDetails
EventDetails details = EventDetails.builder()...build();
details.getFlagsChanged(); // Delegates to composed object
```

**Impact**: Behavioral compatibility maintained, but inheritance relationship removed.

### Required Provider Names
**Breaking**: Provider names now required for EventDetails per OpenFeature spec.

**Before**:
```java
// Provider name could be null
EventDetails details = EventDetails.builder()
    .domain("domain")
    .build();
```

**After**:
```java
// Provider name is required
EventDetails details = EventDetails.builder()
    .providerName("my-provider") // Required
    .domain("domain")
    .build(); // Will throw if providerName is null
```

**Migration**: Always provide provider names when creating EventDetails.

---

## 📦 Lombok Dependency Removal
**Breaking**: Complete removal of Lombok dependency from both API and SDK modules.

**Original State (v1.18.0)**:
```java
// Heavy Lombok usage throughout codebase
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderEvaluation<T> implements BaseEvaluation<T> {
    T value;
    String variant;
    private String reason;
    ErrorCode errorCode;
    private String errorMessage;
    @Builder.Default
    private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();
}

// Similar @Data pattern used in:
// - FlagEvaluationDetails, EventDetails, ProviderEventDetails
// - ImmutableContext, ImmutableMetadata, Value
// - Many other POJOs
```

**Transformed State (v2.0.0)**:
```java
// Hand-written immutable classes with custom builders
public final class ProviderEvaluation<T> implements BaseEvaluation<T> {
    private final T value;
    private final String variant;
    private final String reason;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final ImmutableMetadata flagMetadata;

    private ProviderEvaluation(Builder<T> builder) {
        this.value = builder.value;
        this.variant = builder.variant;
        // ... (all fields set from builder)
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        // Hand-written builder implementation
    }

    // Hand-written getters (no setters - immutable)
    public T getValue() { return value; }
    // ...
}
```

**Impact**:
- **No more Lombok annotations**: `@Data`, `@Builder`, `@Value`, `@Slf4j` completely removed
- **All builder patterns now hand-written** with consistent naming (`Builder` instead of `ClassNameBuilder`)
- **Improved IDE compatibility** - no more IDE plugins required for Lombok
- **Better debugging experience** - actual source code instead of generated methods
- **Cleaner bytecode** - no Lombok magic
- **Explicit control** over builder behavior and validation

**Migration**: No user action required - all Lombok-generated functionality replaced with equivalent hand-written code. Builder patterns remain the same from user perspective.

---

## 🔧 Recent Interface Optimizations (Latest Changes)

### EvaluationClient Default Method Implementation
**Enhancement**: Major reduction in implementation burden for interface implementers.

**Before**: Implementers had to override all 30 methods manually
```java
public class MyClient implements EvaluationClient {
    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return getBooleanValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    // ... 28 more similar delegating methods
}
```

**After**: Only core methods need implementation, defaults handle delegation
```java
public class MyClient implements EvaluationClient {
    // Only need to implement these 10 core methods:
    // - get{Type}Details(key, defaultValue, ctx, options) - 5 methods
    // All other 25 methods provided as defaults that delegate properly
}
```

**Impact**:
- **75% reduction** in required method implementations
- **NoOpClient reduced from ~200 lines to ~50 lines**
- Consistent delegation logic across all implementations
- Future-proof: changes to delegation only need to happen in interface

---

## 🚫 Removed Public APIs

### Public Setters
**Breaking**: All public setters removed from immutable POJOs.

**Removed Methods**:
- `ProviderEvaluation.setValue(T)`
- `ProviderEvaluation.setVariant(String)`
- `ProviderEvaluation.setReason(String)`
- `ProviderEvaluation.setErrorCode(ErrorCode)`
- `ProviderEvaluation.setErrorMessage(String)`
- `ProviderEvaluation.setFlagMetadata(ImmutableMetadata)`
- `FlagEvaluationDetails.setFlagKey(String)`
- `FlagEvaluationDetails.setValue(T)`
- `FlagEvaluationDetails.setVariant(String)`
- `FlagEvaluationDetails.setReason(String)`
- `FlagEvaluationDetails.setErrorCode(ErrorCode)`
- `FlagEvaluationDetails.setErrorMessage(String)`
- `FlagEvaluationDetails.setFlagMetadata(ImmutableMetadata)`

**Migration**: Use builders to create objects with desired state instead of mutation.

### Public Constructors
**Breaking**: Public constructors removed from POJOs.

**Removed Constructors**:
- `ProviderEvaluation()` 
- `ProviderEvaluation(T, String, String, ErrorCode, String, ImmutableMetadata)`
- `FlagEvaluationDetails()`
- `FlagEvaluationDetails(String, T, String, String, ErrorCode, String, ImmutableMetadata)`
- `EventDetails(String, String, ProviderEventDetails)`
- `ProviderEventDetails()` (deprecated, now private)
- `ProviderEventDetails(List<String>, String, ImmutableMetadata, ErrorCode)`

**Migration**: Use builder patterns exclusively for object creation.

---

## 🔄 Migration Summary

### For Library Authors (Feature Flag Provider Implementers)
1. **Update Dependencies**: Change from old monolithic `sdk` to new `api` module
   ```xml
   <!-- OLD (v1.18.0) - Single module with everything -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>1.18.0</version>
   </dependency>

   <!-- NEW (v2.0.0) - API-only module for minimal dependencies -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>api</artifactId>
       <version>0.0.1</version>  <!-- Note: API starts at 0.0.1 -->
   </dependency>
   ```

2. **Update Package Imports**: Change all package references
   ```java
   // OLD imports (v1.18.0)
   import dev.openfeature.sdk.FeatureProvider;
   import dev.openfeature.sdk.ProviderEvaluation;
   import dev.openfeature.sdk.EvaluationContext;
   import dev.openfeature.sdk.ErrorCode;

   // NEW imports (v2.0.0)
   import dev.openfeature.api.Provider;                    // FeatureProvider → Provider
   import dev.openfeature.api.evaluation.ProviderEvaluation;
   import dev.openfeature.api.evaluation.EvaluationContext;
   import dev.openfeature.api.ErrorCode;
   ```
2. **Review Package Access**: Ensure no usage of moved internal classes
3. **Update Documentation**: Reference new module structure
4. **Verify Scope**: API module contains only interfaces and POJOs needed for provider implementation

### For SDK Users (Application Developers)
1. **Update Dependencies**: Update `sdk` dependency version (same artifactId, major refactor)
   ```xml
   <!-- OLD (v1.18.0) - Monolithic SDK -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>1.18.0</version>
   </dependency>

   <!-- NEW (v2.0.0) - SDK now includes API module + compatibility layer -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>2.0.0</version>
   </dependency>
   ```

2. **Gradual Migration Strategy**: v2.0.0 includes compatibility layer
   ```java
   // IMMEDIATE: These still work but show deprecation warnings
   import dev.openfeature.sdk.FeatureProvider;     // @Deprecated, extends Provider
   import dev.openfeature.sdk.Features;            // @Deprecated, extends EvaluationClient

   // FUTURE: Migrate imports gradually (before v2.1.0 when compatibility layer is removed)
   import dev.openfeature.api.Provider;
   import dev.openfeature.api.evaluation.EvaluationClient;
   ```
2. **Replace Constructors**: Use builders for all POJO creation
3. **Remove Setter Usage**: Objects are now immutable
4. **Update Convenience Methods**: Use builders instead of `from()` methods
5. **Ensure Provider Names**: Always specify provider names in events

### For Build Systems & CI/CD
1. **Multi-module Builds**: Update build scripts to handle Maven multi-module structure
2. **Artifact Publishing**: Both API and SDK modules are now published separately
3. **Version Management**: Parent POM manages versions for both modules
4. **Testing**: Tests are distributed across both modules

### Quick Migration Checklist

#### Maven/Gradle Dependencies
- [ ] **Library Authors**: Update from `dev.openfeature:sdk` → `dev.openfeature:api`
- [ ] **App Developers**: Keep `dev.openfeature:sdk` but update version to `2.0.0`
- [ ] Update `groupId` (remains `dev.openfeature`)
- [ ] Update version to `2.0.0`
- [ ] Note: Parent POM is now `dev.openfeature:openfeature-java`

#### Build System Changes
- [ ] Update CI/CD scripts for multi-module Maven structure
- [ ] Verify artifact publishing handles both API and SDK modules
- [ ] Update documentation references to new artifact names

#### Code Changes
- [ ] Replace `new ProviderEvaluation<>()` with `ProviderEvaluation.<T>builder().build()`
- [ ] Replace `new FlagEvaluationDetails<>()` with `FlagEvaluationDetails.<T>builder().build()`
- [ ] Replace `new EventDetails()` with `EventDetails.builder().build()`
- [ ] Remove all setter method calls on POJOs
- [ ] Replace convenience methods with builder patterns
- [ ] Add provider names to all EventDetails creation
- [ ] Update any explicit builder type references

## 💡 Benefits of These Changes

### Thread Safety
- All POJOs are now immutable and thread-safe by default
- No risk of concurrent modification

### API Consistency  
- Unified builder patterns across all POJOs
- Predictable object creation patterns
- Clear separation between API contracts and implementation

### OpenFeature Compliance
- Event details architecture now complies with OpenFeature specification
- Required fields are enforced at build time

### Module Separation & Dependency Management
- **Clean Architecture**: Clear separation between API contracts (`openfeature-api`) and SDK implementation (`openfeature-sdk`)
- **Smaller Dependencies**: Library authors can depend on API-only module (lighter footprint)
- **Better Dependency Management**: Applications can choose between API-only or full SDK
- **Multi-module Maven Structure**: Better organization and build management
- **Independent Versioning**: Modules can evolve independently (though currently versioned together)

### Build & Deployment Benefits
- **Parallel Builds**: Maven can build modules in parallel
- **Selective Deployment**: Can deploy API and SDK modules independently
- **Better Testing**: Test isolation between API contracts and implementation
- **Cleaner Artifacts**: API module contains only interfaces, POJOs, and exceptions

---

**Note**: This is a major version release (v2.0.0) due to the breaking nature of these changes. All changes improve API consistency, thread safety, and OpenFeature specification compliance while maintaining the same core functionality.