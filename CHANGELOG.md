# Changelog

## [1.0.0](https://github.com/open-feature/java-sdk/compare/v0.3.1...v1.0.0) (2022-10-25)


### Miscellaneous Chores

* release 1.0.0 ([#163](https://github.com/open-feature/java-sdk/issues/163)) ([c9ba9c9](https://github.com/open-feature/java-sdk/commit/c9ba9c9275ad4417a206b148e830fa78d265adb6))

## [0.3.1](https://github.com/open-feature/java-sdk/compare/v0.3.0...v0.3.1) (2022-10-13)


### Bug Fixes

* merge eval context ([#149](https://github.com/open-feature/java-sdk/issues/149)) ([fad0f35](https://github.com/open-feature/java-sdk/commit/fad0f35fc8a6469672ef67820f1850f20741b66a))

## [0.3.0](https://github.com/open-feature/java-sdk/compare/v0.2.2...v0.3.0) (2022-10-13)


### ⚠ BREAKING CHANGES

* add rw locks to client/api, hook accessor name (#131)
* use evaluation context interface (#112)
* Change the package name. Everyone knows it's java (or it doesn't matter) (#111)
* errorCode as enum, reason as string (#80)
* use value for object resolver
* use instant not zoneddatetime

### Features

* Add asObjectMap to get the EvaluationContext as Map<String,Object> ([#75](https://github.com/open-feature/java-sdk/issues/75)) ([2eec1a5](https://github.com/open-feature/java-sdk/commit/2eec1a5519b9efab7d7f9dc8b1cbd84d9218368b))
* add object to value wrapper ([0152a1e](https://github.com/open-feature/java-sdk/commit/0152a1eef93ea1b5253ddae78718a9805c98aaf7))
* add rw locks to client/api, hook accessor name ([#131](https://github.com/open-feature/java-sdk/issues/131)) ([2192932](https://github.com/open-feature/java-sdk/commit/21929328630eba00be741392457f68bacf59f376))
* errorCode as enum, reason as string ([#80](https://github.com/open-feature/java-sdk/issues/80)) ([84f220d](https://github.com/open-feature/java-sdk/commit/84f220d8139035a1222d13b2dd6f8b048932c192))
* Support for generating CycloneDX sboms ([#119](https://github.com/open-feature/java-sdk/issues/119)) ([9647c3f](https://github.com/open-feature/java-sdk/commit/9647c3f04d8ace10a9d512bfe30fd9ef2c5631d1))
* use evaluation context interface ([#112](https://github.com/open-feature/java-sdk/issues/112)) ([e9732b5](https://github.com/open-feature/java-sdk/commit/e9732b582dc9e3fa7be51c834e1afe7ad890c4e3))
* use instant not zoneddatetime ([3e62414](https://github.com/open-feature/java-sdk/commit/3e6241422266825f267043e4acd116803c4939b0))
* use value for object resolver ([5d26247](https://github.com/open-feature/java-sdk/commit/5d262470e8ec47d2af35f0aabe55e8c969e992ac))


### Bug Fixes

* **deps:** update dependency io.cucumber:cucumber-bom to v7.8.0 ([#100](https://github.com/open-feature/java-sdk/issues/100)) ([5e96d14](https://github.com/open-feature/java-sdk/commit/5e96d140c1195a1e8eb175feae3ec29db4439367))
* **deps:** update junit5 monorepo ([#92](https://github.com/open-feature/java-sdk/issues/92)) ([8ca655a](https://github.com/open-feature/java-sdk/commit/8ca655a788273c61e5270ce7bf175064f42d605d))
* isList check in Value checks type of list ([#70](https://github.com/open-feature/java-sdk/issues/70)) ([81ab071](https://github.com/open-feature/java-sdk/commit/81ab0710ea56af65eb65c7f95832b8f58c559a51))


### Code Refactoring

* Change the package name. Everyone knows it's java (or it doesn't matter) ([#111](https://github.com/open-feature/java-sdk/issues/111)) ([6eeeddd](https://github.com/open-feature/java-sdk/commit/6eeeddd2ea8040b47d1fd507b68d42c3bce52db4))

## [0.2.2](https://github.com/open-feature/java-sdk/compare/dev.openfeature.javasdk-v0.2.1...dev.openfeature.javasdk-v0.2.2) (2022-09-20)


### Features

* Add asObjectMap to get the EvaluationContext as Map<String,Object> ([#75](https://github.com/open-feature/java-sdk/issues/75)) ([2eec1a5](https://github.com/open-feature/java-sdk/commit/2eec1a5519b9efab7d7f9dc8b1cbd84d9218368b))

## [0.2.1](https://github.com/open-feature/java-sdk/compare/dev.openfeature.javasdk-v0.2.0...dev.openfeature.javasdk-v0.2.1) (2022-09-13)


### Bug Fixes

* isList check in Value checks type of list ([#70](https://github.com/open-feature/java-sdk/issues/70)) ([81ab071](https://github.com/open-feature/java-sdk/commit/81ab0710ea56af65eb65c7f95832b8f58c559a51))

## [0.2.0](https://github.com/open-feature/java-sdk/compare/dev.openfeature.javasdk-v0.1.1...dev.openfeature.javasdk-v0.2.0) (2022-09-13)


### ⚠ BREAKING CHANGES

* use value for object resolver
* use instant not zoneddatetime

### Features

* add object to value wrapper ([0152a1e](https://github.com/open-feature/java-sdk/commit/0152a1eef93ea1b5253ddae78718a9805c98aaf7))
* use instant not zoneddatetime ([3e62414](https://github.com/open-feature/java-sdk/commit/3e6241422266825f267043e4acd116803c4939b0))
* use value for object resolver ([5d26247](https://github.com/open-feature/java-sdk/commit/5d262470e8ec47d2af35f0aabe55e8c969e992ac))
