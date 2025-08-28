# Breaking Changes - OpenFeature Java SDK v2.0.0

This document outlines all breaking changes introduced in the `feat/split-api-and-sdk` branch compared to the `main` branch. These changes represent a major version bump to v2.0.0.

## 🏗️ Architecture Changes

### Module Structure & Maven Coordinates
**Breaking**: The monolithic SDK has been split into separate API and SDK modules with new Maven coordinates.

**Before (v1.x)**:
```xml
<!-- Single monolithic module -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>1.17.0</version>
</dependency>
```

**After (v2.0.0)**:
```xml
<!-- For API-only usage (interfaces, POJOs, exceptions) -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>api</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- For full SDK usage (includes API + implementation) -->
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>2.0.0</version>
</dependency>
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
**Breaking**: `ProviderEvaluation` is now immutable with private constructors.

**Before**:
```java
// Public constructors
ProviderEvaluation<String> eval = new ProviderEvaluation<>();
eval.setValue("test");
eval.setVariant("variant1");
eval.setReason("DEFAULT");

// Or constructor with parameters
ProviderEvaluation<String> eval = new ProviderEvaluation<>(
    "test", "variant1", "DEFAULT", ErrorCode.NONE, null, metadata);
```

**After**:
```java
// Builder pattern only
ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
    .value("test")
    .variant("variant1")
    .reason("DEFAULT")
    .errorCode(ErrorCode.NONE)
    .flagMetadata(metadata)
    .build();

// Object is immutable - no setters available
// eval.setValue("new"); // ❌ Compilation error
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

### Internal Class Movement
**Breaking**: Internal utility classes moved from API to SDK module.

**Moved Classes**:
- `AutoCloseableLock` → SDK module
- `AutoCloseableReentrantReadWriteLock` → SDK module  
- `ObjectUtils` → SDK module
- `TriConsumer` → SDK module

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
1. **Update Dependencies**: Change from old `sdk` to new `api` module
   ```xml
   <!-- OLD -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>1.17.0</version>
   </dependency>
   
   <!-- NEW -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>api</artifactId>
       <version>2.0.0</version>
   </dependency>
   ```
2. **Review Package Access**: Ensure no usage of moved internal classes
3. **Update Documentation**: Reference new module structure
4. **Verify Scope**: API module contains only interfaces and POJOs needed for provider implementation

### For SDK Users (Application Developers)
1. **Update Dependencies**: Update `sdk` dependency (same artifactId, new structure)
   ```xml
   <!-- OLD -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>1.17.0</version>
   </dependency>
   
   <!-- NEW -->
   <dependency>
       <groupId>dev.openfeature</groupId>
       <artifactId>sdk</artifactId>
       <version>2.0.0</version>
   </dependency>
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