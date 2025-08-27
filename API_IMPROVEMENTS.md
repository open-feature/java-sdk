# OpenFeature Java API Improvements

This document outlines improvement opportunities for the OpenFeature Java API based on analysis of the current codebase structure.

## Status

✅ **Completed**: Builder class names have been standardized to use `Builder` instead of class-specific names (e.g., `ImmutableMetadataBuilder` → `Builder`)

## 1. POJO Structure Consistency

### Current Issues
- **Mixed Mutability Patterns**: `ProviderEvaluation` is mutable with both builder pattern AND public setters, while event details are immutable with builders only
- **Constructor Overloading vs Builders**: Some classes provide multiple constructors alongside builders

### Improvement Prompts
```
Make ProviderEvaluation immutable by:
1. Making all fields final
2. Removing public setters (setValue, setVariant, etc.)
3. Adding private constructor that takes all parameters
4. Ensuring builder is the only way to create instances

Remove constructor overloads from POJOs and standardize on builder pattern only for:
- FlagEvaluationDetails
- ProviderEvaluation  
- EventDetails
- ProviderEventDetails
```

## 2. Builder Pattern Standardization

### Current Issues
- **Inconsistent Method Names**: Some builders use `addX()` (ImmutableMetadata), others use `x()`
- **Validation Inconsistency**: Some builders validate in `build()`, others in setter methods

### Improvement Prompts
```
Update ImmutableMetadata.Builder to use consistent naming:
- Change addString() → string()
- Change addInteger() → integer()  
- Change addLong() → longValue()
- Change addFloat() → floatValue()
- Change addDouble() → doubleValue()
- Change addBoolean() → boolean()

Move all validation logic to build() methods instead of setter methods for:
- All builder classes that currently validate in setters
- Add comprehensive validation in build() with clear error messages

Ensure all builder methods return 'this' for fluent interface consistency
```

## 3. Structure Handling Unification

### Current Issues
- **Inconsistent Context Constructors**: `ImmutableContext` and `MutableContext` have different constructor patterns
- **Value Conversion Duplication**: `convertValue()` method exists in multiple places

### Improvement Prompts
```
Create unified context creation patterns:
1. Add static factory methods: Context.of(), Context.empty(), Context.withTargeting(key)
2. Standardize constructor patterns between ImmutableContext and MutableContext
3. Add builder() static methods to both context types for consistency

Extract value conversion logic:
1. Create ValueConverter utility class
2. Move convertValue() from Structure interface to utility
3. Update all implementations to use centralized conversion logic

Add convenience methods for common structure operations:
- Structure.empty()
- Structure.of(Map<String, Object>)
- Structure.withAttribute(key, value)
```

## 4. Metadata Handling Improvements

### Current Issues
- **Interface Hierarchy**: `ClientMetadata` has deprecated `getName()` method
- **Builder Inconsistency**: `ImmutableMetadata` builder uses `addType()` methods vs standard patterns

### Improvement Prompts
```
Clean up metadata interfaces:
1. Remove deprecated getName() method from ClientMetadata after checking usage
2. Create clear separation between ClientMetadata and generic Metadata
3. Consider if both interfaces are needed or can be unified

Add metadata factory methods:
- Metadata.empty()
- Metadata.of(String name)
- Metadata.builder() shortcuts for common cases

Improve metadata builder ergonomics:
- Add putAll(Map<String, Object>) method
- Add convenience methods for common metadata patterns
```

## 5. Event Details Architecture Refinement

### Current Issues
- **Complex Composition**: `EventDetails` composes `ProviderEventDetails` but both implement same interface

### Improvement Prompts
```
Evaluate event details architecture:
1. Consider if separate ProviderEventDetails and EventDetails are necessary
2. Document the relationship and usage patterns clearly
3. If keeping both, ensure clear distinction in naming and purpose

Add event details convenience methods:
- EventDetails.forProviderError(String providerName, ErrorCode code, String message)
- EventDetails.forProviderReady(String providerName) 
- EventDetails.forConfigurationChange(String providerName, List<String> flagsChanged)

Improve event details validation:
- Ensure providerName is always required per OpenFeature spec
- Add validation in builder.build() methods
- Provide clear error messages for invalid states
```

## 6. API Ergonomics and Developer Experience

### High Priority Improvements
```
Add static import friendly factory methods:
- Value.of(Object) for common value creation
- Context.of(String targetingKey) for simple contexts
- Context.of(String targetingKey, Map<String, Object> attributes)

Add null safety annotations:
- @Nullable for optional parameters and return values
- @NonNull for required parameters
- Import javax.annotation or create custom annotations

Create fluent shortcuts for common patterns:
- EvaluationContext.withTargeting(String key)
- FlagEvaluationDetails.success(String flagKey, T value)
- FlagEvaluationDetails.error(String flagKey, T defaultValue, ErrorCode code, String message)
```

### Medium Priority Improvements
```
Reduce method overloading where builders can be used:
- Evaluate if multiple overloaded constructors are needed
- Prefer builder pattern over method overloads for complex objects

Improve error messages and validation:
- Add descriptive error messages in builder validation
- Include parameter names and expected values in exceptions
- Add @throws documentation for checked exceptions

Consider Optional usage for nullable returns:
- Evaluate using Optional<T> instead of null returns
- Focus on public API methods that commonly return null
- Document null-safety contracts clearly
```

### Low Priority Improvements
```
Package structure optimization:
- Consider if all API classes need to be in single package
- Evaluate creating sub-packages for: contexts, events, metadata, evaluation
- Maintain backward compatibility during any restructuring

Documentation improvements:
- Add usage examples in class-level Javadocs
- Include common patterns and anti-patterns
- Add code examples for complex builder usage

Performance optimizations:
- Reduce object allocation in hot paths (evaluation)
- Consider object pooling for frequently created objects
- Optimize map operations in context merging
```

## 7. Checkstyle Fixes Needed

Based on current checkstyle errors:

```
Fix checkstyle issues in:
1. ProviderEventDetails.java:19 - Add empty line before @deprecated tag
2. FlagEvaluationDetails.java:4 - Remove unused Optional import  
3. EventDetails.java - Add Javadoc comments for missing builder methods:
   - flagsChanged() method
   - message() method  
   - eventMetadata() method
   - errorCode() method
```

## Implementation Guidelines

### Breaking Changes
- Document all breaking changes with migration guides
- Consider deprecation periods for public API changes
- Provide automated migration tools where possible

### Backward Compatibility
- Maintain existing public API surface where possible
- Use @Deprecated annotations with clear migration paths
- Version new features appropriately

### Testing Strategy
- Add comprehensive tests for all builder patterns
- Test null safety and validation thoroughly  
- Include integration tests for common usage patterns
- Maintain test coverage above 80% for all changes

## Next Steps

1. **Fix Checkstyle Issues** - Address the 6 current checkstyle violations
2. **Prioritize High-Value Changes** - Start with POJO consistency and builder standardization
3. **Create Migration Guide** - Document any breaking changes for users
4. **Update Documentation** - Refresh examples and usage patterns
5. **Performance Testing** - Ensure changes don't negatively impact performance

This document serves as a roadmap for incrementally improving the API while maintaining stability and backward compatibility.