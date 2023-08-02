## Welcome! Super happy to have you here.

A few things.

First, we have [a code of
conduct](https://github.com/open-feature/.github/blob/main/CODE_OF_CONDUCT.md). Don't
be a jerk.

We're not keen on vendor-specific stuff in this library, but if there are changes that need to happen in the spec to enable vendor-specific stuff in user code or other extension points, check out [the spec](https://github.com/open-feature/spec).

Any contributions you make are expected to be tested with unit tests. You can validate these work with `gradle test`, or the automation itself will run them for you when you make a PR.

Your code is supposed to work with Java 8+.

If you think we might be out of date with the spec, you can check that by invoking `python spec_finder.py` in the root of the repository. This will validate we have tests defined for all of the specification entries we know about.

If you're adding tests to cover something in the spec, use the `@Specification` annotation like you see throughout the test suites.

## End-to-End Tests

<!-- TODO: this section should be updated with https://github.com/open-feature/java-sdk/issues/523 -->

The continuous integration runs a set of [gherkin e2e tests](https://github.com/open-feature/test-harness/blob/main/features/evaluation.feature) using [`flagd`](https://github.com/open-feature/flagd). These tests do not run with the default maven profile. If you'd like to run them locally, you can start the flagd testbed with

```
docker run -p 8013:8013 ghcr.io/open-feature/flagd-testbed:latest
```
and then run 
```
mvn test -P e2e-test
```

## Releasing

See [releasing](./docs/release.md).

Thanks and looking forward to your issues and pull requests.
