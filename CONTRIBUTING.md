## Welcome! Super happy to have you here.

A few things.

First, we have [a code of
conduct](https://github.com/open-feature/.github/blob/main/CODE_OF_CONDUCT.md). Don't
be a jerk.

We're not keen on vendor-specific stuff in this library, but if there are changes that need to happen in the spec to enable vendor-specific stuff in user code or other extension points, check out [the spec](https://github.com/open-feature/spec).

Any contributions you make are expected to be tested with unit tests. You can validate these work with `mvn test`.
Further, it is recommended to verify code styling and static code analysis with `mvn verify -P !deploy`.
Regardless, the automation itself will run them for you when you open a PR.

> [!TIP]
> For easier usage maven wrapper is available. Example usage: `./mvnw verify`

Your code is supposed to work with Java 8+.

If you think we might be out of date with the spec, you can check that by invoking `python spec_finder.py` in the root of the repository. This will validate we have tests defined for all of the specification entries we know about.

If you're adding tests to cover something in the spec, use the `@Specification` annotation like you see throughout the test suites.

## Code Styles

### Overview
Our project follows strict code formatting standards to maintain consistency and readability across the codebase. We use [Spotless](https://github.com/diffplug/spotless) integrated with the [Palantir Java Format](https://github.com/palantir/palantir-java-format) for code formatting.

**Spotless** ensures that all code complies with the formatting rules automatically, reducing style-related issues during code reviews.

### How to Format Your Code
1. **Before Committing Changes:**
   Run the Spotless plugin to format your code. This will apply the Palantir Java Format style:
   ```bash
   mvn spotless:apply
   ```

2. **Verify Formatting:**
   To check if your code adheres to the style guidelines without making changes:
   ```bash
   mvn spotless:check
   ```

    - If this command fails, your code does not follow the required formatting. Use `mvn spotless:apply` to fix it.

### CI/CD Integration
Our Continuous Integration (CI) pipeline automatically checks code formatting using the Spotless plugin. Any code that does not pass the `spotless:check` step will cause the build to fail.

### Best Practices
- Regularly run `mvn spotless:apply` during your work to ensure your code remains aligned with the standards.
- Configure your IDE (e.g., IntelliJ IDEA or Eclipse) to follow the Palantir Java format guidelines to reduce discrepancies during development.

### Support
If you encounter issues with code formatting, please raise a GitHub issue or contact the maintainers.

## End-to-End Tests

The continuous integration runs a set of [gherkin e2e tests](https://github.com/open-feature/spec/blob/main/specification/assets/gherkin/evaluation.feature) using `InMemoryProvider`.

to run alone:
```
mvn test -P e2e
```

## Benchmarking

There is a small JMH benchmark suite for testing allocations that can be run with:

```sh
mvn -P benchmark clean compile test-compile jmh:benchmark -Djmh.f=1  -Djmh.prof='dev.openfeature.sdk.benchmark.AllocationProfiler'
```

If you are concerned about the repercussions of a change on memory usage, run this an compare the results to the committed. `benchmark.txt` file.
Note that the ONLY MEANINGFUL RESULTS of this benchmark are the `totalAllocatedBytes` and the `totalAllocatedInstances`.
The `run` score, and maven task time are not relevant since this benchmark is purely memory-related and has nothing to do with speed.
You can also view the heap breakdown to see which objects are taking up the most memory.

## Releasing

See [releasing](./docs/release.md).

Thanks and looking forward to your issues and pull requests.
