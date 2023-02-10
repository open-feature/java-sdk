# Changelog

## [1.2.0](https://github.com/open-feature/java-sdk/compare/v1.1.0...v1.2.0) (2023-02-10)


### Features

* added implementation of immutable evaluation context ([#210](https://github.com/open-feature/java-sdk/issues/210)) ([6c14d87](https://github.com/open-feature/java-sdk/commit/6c14d87c2e54c953eff351fa1ccdd914fa08b6ed))


### Bug Fixes

* **deps:** update dependency io.cucumber:cucumber-bom to v7.11.1 ([#271](https://github.com/open-feature/java-sdk/issues/271)) ([8845242](https://github.com/open-feature/java-sdk/commit/88452423303f8c1feada733f1c9d4db845d64a5b))
* **deps:** update dependency org.projectlombok:lombok to v1.18.26 ([#277](https://github.com/open-feature/java-sdk/issues/277)) ([aad036a](https://github.com/open-feature/java-sdk/commit/aad036a0113e2d248e493d0d87e52320a66df7a2))
* improve error logs for evaluation failure ([#276](https://github.com/open-feature/java-sdk/issues/276)) ([9349997](https://github.com/open-feature/java-sdk/commit/93499975d0b9ae30aa34db999d8aa3d7c955da70))
* MutableContext and ImmutableContext merge are made recursive ([#280](https://github.com/open-feature/java-sdk/issues/280)) ([bd4e12e](https://github.com/open-feature/java-sdk/commit/bd4e12e16f3c4af5cdcad490977ccc0842e1ded6))

## [1.1.0](https://github.com/open-feature/java-sdk/compare/v1.0.1...v1.1.0) (2023-01-24)


### Features

* add STATIC, CACHED reasons ([#240](https://github.com/open-feature/java-sdk/issues/240)) ([d069a8f](https://github.com/open-feature/java-sdk/commit/d069a8fa9d7c1795f6713f4b331657119e6f7d8f))
* add STATIC, CACHED reasons. ([d069a8f](https://github.com/open-feature/java-sdk/commit/d069a8fa9d7c1795f6713f4b331657119e6f7d8f))


### Bug Fixes

* **deps:** update dependency io.cucumber:cucumber-bom to v7.10.0 ([#195](https://github.com/open-feature/java-sdk/issues/195)) ([0544597](https://github.com/open-feature/java-sdk/commit/0544597511471a2c10fbe2a3296de5629730ea7c))
* **deps:** update dependency io.cucumber:cucumber-bom to v7.11.0 ([#208](https://github.com/open-feature/java-sdk/issues/208)) ([6103dd2](https://github.com/open-feature/java-sdk/commit/6103dd2d39adceaaeeb0f63de6fb10437be3a743))
* **deps:** update junit5 monorepo ([#230](https://github.com/open-feature/java-sdk/issues/230)) ([67b15c6](https://github.com/open-feature/java-sdk/commit/67b15c6e104fe7539f7a197810be28d69634cbfc))

## [1.0.1](https://github.com/open-feature/java-sdk/compare/v1.0.0...v1.0.1) (2022-11-30)


### Bug Fixes

* **deps:** Spot bug scope change ([#173](https://github.com/open-feature/java-sdk/issues/173)) ([113b5e5](https://github.com/open-feature/java-sdk/commit/113b5e5f2ed8b72e5c23dedbf8d13d0fd4d4f878))
* **deps:** update dependency io.cucumber:cucumber-bom to v7.9.0 ([#172](https://github.com/open-feature/java-sdk/issues/172)) ([fcc8972](https://github.com/open-feature/java-sdk/commit/fcc8972022dd78fcdf5311373a8b8ad238368baa))

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
