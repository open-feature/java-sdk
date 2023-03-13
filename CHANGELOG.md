# Changelog

## [1.3.0](https://github.com/open-feature/java-sdk/compare/v1.2.0...v1.3.0) (2023-03-12)


### 💥 Breaking Changes

* remove the deprecated setTargetingKey method in EvaluationContext. ([#290](https://github.com/open-feature/java-sdk/issues/290)) ([d78c99c](https://github.com/open-feature/java-sdk/commit/d78c99ce16be906452bf7961cd43972b72855dd3))


### 🐛 Bug Fixes

* Do not throw null reference exception accessing a missing item. ([#300](https://github.com/open-feature/java-sdk/issues/300)) ([464820d](https://github.com/open-feature/java-sdk/commit/464820d5da0d70ae3682d0819da766460ca0e6ce))
* handling of double and integer ([#316](https://github.com/open-feature/java-sdk/issues/316)) ([0a27a77](https://github.com/open-feature/java-sdk/commit/0a27a77fc1b46355eb382cb17177e2fbe2e69631))


### 🧹 Chore

* add changelog sections ([#320](https://github.com/open-feature/java-sdk/issues/320)) ([cb18a09](https://github.com/open-feature/java-sdk/commit/cb18a099c5f28fe05a5e9dfc62543f1f78c47603))
* **deps:** update actions/cache digest to 69d9d44 ([#303](https://github.com/open-feature/java-sdk/issues/303)) ([45d3c0f](https://github.com/open-feature/java-sdk/commit/45d3c0fcc1ed48bbda9caa81174a0f026808d38e))
* **deps:** update actions/cache digest to 81b7281 ([#298](https://github.com/open-feature/java-sdk/issues/298)) ([4098bc8](https://github.com/open-feature/java-sdk/commit/4098bc86e8cb1c70c96bff443a0180d09d050866))
* **deps:** update actions/cache digest to 940f3d7 ([#321](https://github.com/open-feature/java-sdk/issues/321)) ([ec8f129](https://github.com/open-feature/java-sdk/commit/ec8f129ffcf79b7e9d145a02c683ff7a7f951b01))
* **deps:** update actions/cache digest to e0d6227 ([#311](https://github.com/open-feature/java-sdk/issues/311)) ([44443e4](https://github.com/open-feature/java-sdk/commit/44443e4e5424c09e75ade3f868b8e261cf355513))
* **deps:** update actions/checkout digest to 27135e3 ([#323](https://github.com/open-feature/java-sdk/issues/323)) ([c4d7b71](https://github.com/open-feature/java-sdk/commit/c4d7b71f09b664f53830341ab11372db40201e07))
* **deps:** update actions/setup-java digest to 0de5c66 ([#322](https://github.com/open-feature/java-sdk/issues/322)) ([71c5fc2](https://github.com/open-feature/java-sdk/commit/71c5fc2c0fa9e5e0ee9dfac0aa7ebcd19590d664))
* **deps:** update actions/setup-java digest to 888b400 ([#326](https://github.com/open-feature/java-sdk/issues/326)) ([33ee57a](https://github.com/open-feature/java-sdk/commit/33ee57a862e021675c615f5b16d87418f49b0a8b))
* **deps:** update amannn/action-semantic-pull-request digest to b6bca70 ([#292](https://github.com/open-feature/java-sdk/issues/292)) ([237a0bc](https://github.com/open-feature/java-sdk/commit/237a0bcbba91164ccb227ad934df86b14a4852fc))
* **deps:** update codecov/codecov-action digest to 13d8b07 ([#325](https://github.com/open-feature/java-sdk/issues/325)) ([0f34a95](https://github.com/open-feature/java-sdk/commit/0f34a95174857c9f568204eea7405749c89a67ba))
* **deps:** update codecov/codecov-action digest to 4b062cb ([#313](https://github.com/open-feature/java-sdk/issues/313)) ([579f8ab](https://github.com/open-feature/java-sdk/commit/579f8ab7504e108056642b71b070aba81ecc0327))
* **deps:** update codecov/codecov-action digest to 83bb3d0 ([#295](https://github.com/open-feature/java-sdk/issues/295)) ([1b72aeb](https://github.com/open-feature/java-sdk/commit/1b72aeb0e09a84814df41aa2e5df9c873f21838e))
* **deps:** update codecov/codecov-action digest to 9b87723 ([#327](https://github.com/open-feature/java-sdk/issues/327)) ([cbd4618](https://github.com/open-feature/java-sdk/commit/cbd4618871b408794c5dfc26cc5b53df3e8edd6f))
* **deps:** update codecov/codecov-action digest to ce0bcc6 ([#304](https://github.com/open-feature/java-sdk/issues/304)) ([259a749](https://github.com/open-feature/java-sdk/commit/259a749a5f0c7069b3ed1c7e654324346d78356a))
* **deps:** update dependency com.github.spotbugs:spotbugs-maven-plugin to v4.7.3.1 ([#306](https://github.com/open-feature/java-sdk/issues/306)) ([69a1a8f](https://github.com/open-feature/java-sdk/commit/69a1a8fad3e6a672b6f55ed72dd6a030a9856343))
* **deps:** update dependency com.github.spotbugs:spotbugs-maven-plugin to v4.7.3.2 ([#308](https://github.com/open-feature/java-sdk/issues/308)) ([a8caae6](https://github.com/open-feature/java-sdk/commit/a8caae6e29d419d6f8a368dfd40c001be2c7b7eb))
* **deps:** update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.11.0 ([#310](https://github.com/open-feature/java-sdk/issues/310)) ([1d731f6](https://github.com/open-feature/java-sdk/commit/1d731f6fa317eb8efef74e76dfd9e57269da6e2b))
* **deps:** update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.5.0 ([#299](https://github.com/open-feature/java-sdk/issues/299)) ([4296aa4](https://github.com/open-feature/java-sdk/commit/4296aa48bdc0177187071afb4ae08ac56eab0519))
* **deps:** update dependency org.cyclonedx:cyclonedx-maven-plugin to v2.7.5 ([#301](https://github.com/open-feature/java-sdk/issues/301)) ([7459eaa](https://github.com/open-feature/java-sdk/commit/7459eaa028c270bc0b522263b4b3215d1feafe2d))
* **deps:** update github/codeql-action digest to 204eada ([#328](https://github.com/open-feature/java-sdk/issues/328)) ([c92c271](https://github.com/open-feature/java-sdk/commit/c92c271f0900fa3d8f07876d075ef053642f80c1))
* **deps:** update github/codeql-action digest to 237a258 ([#305](https://github.com/open-feature/java-sdk/issues/305)) ([883a4cd](https://github.com/open-feature/java-sdk/commit/883a4cd5151e776f947aab25372bfb69ec1b94f4))
* **deps:** update github/codeql-action digest to 3dde1f3 ([#302](https://github.com/open-feature/java-sdk/issues/302)) ([59429ed](https://github.com/open-feature/java-sdk/commit/59429ed55d90954228d2a2c0a8df33000dc54393))
* **deps:** update github/codeql-action digest to 6ef6e50 ([#314](https://github.com/open-feature/java-sdk/issues/314)) ([8ee4d89](https://github.com/open-feature/java-sdk/commit/8ee4d89c0b0d5397d870fa20d868d3139feddb0e))
* **deps:** update github/codeql-action digest to 89c5165 ([#293](https://github.com/open-feature/java-sdk/issues/293)) ([c9f5899](https://github.com/open-feature/java-sdk/commit/c9f5899128718c02e0ae4ee71d823bbbae6a23b7))
* **deps:** update github/codeql-action digest to 903be79 ([#309](https://github.com/open-feature/java-sdk/issues/309)) ([a3a9d0e](https://github.com/open-feature/java-sdk/commit/a3a9d0eafdb28d83ad0b0640f949c5dd5e6e685f))
* **deps:** update github/codeql-action digest to a589d40 ([#312](https://github.com/open-feature/java-sdk/issues/312)) ([7b79bc8](https://github.com/open-feature/java-sdk/commit/7b79bc818aeef27244299dbab6fb66ee143e5f64))
* **deps:** update github/codeql-action digest to e00cd12 ([#296](https://github.com/open-feature/java-sdk/issues/296)) ([7a19ac8](https://github.com/open-feature/java-sdk/commit/7a19ac84ce76ade7cd6d03a1fc7c9b3340e232a6))
* **deps:** update github/codeql-action digest to e12a2ec ([#324](https://github.com/open-feature/java-sdk/issues/324)) ([7753bfe](https://github.com/open-feature/java-sdk/commit/7753bfec07985a28195d6dc41feca39e925e03ca))
* **deps:** update github/codeql-action digest to e4b846c ([#318](https://github.com/open-feature/java-sdk/issues/318)) ([db11450](https://github.com/open-feature/java-sdk/commit/db114507da17c637aedac3b5c48ecbf0f758a9d5))
* **deps:** update github/codeql-action digest to f13b180 ([#319](https://github.com/open-feature/java-sdk/issues/319)) ([20a9da6](https://github.com/open-feature/java-sdk/commit/20a9da619486f02d775f132f20a2ccfa834f4fba))
* **deps:** update google-github-actions/release-please-action digest to 57bb5dc ([#315](https://github.com/open-feature/java-sdk/issues/315)) ([80fe25a](https://github.com/open-feature/java-sdk/commit/80fe25ae285a484c82567baf5293bdb13c7a771e))
* **deps:** update google-github-actions/release-please-action digest to d3c71f9 ([#297](https://github.com/open-feature/java-sdk/issues/297)) ([514a99e](https://github.com/open-feature/java-sdk/commit/514a99e5ad34afed15b6b0997cd55668abfaff6e))
* **deps:** update google-github-actions/release-please-action digest to e0b9d18 ([#317](https://github.com/open-feature/java-sdk/issues/317)) ([09824e7](https://github.com/open-feature/java-sdk/commit/09824e7c529d36b84086ee4287e97ba1bd60ba6e))

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
