#!/bin/bash -eu
# Copyright 2021 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
################################################################################


#mvn package

#mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:get -Dartifact=com.fasterxml.jackson.core:jackson-databind:2.13.2.2
mvn package

CURRENT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
-Dexpression=project.version -q -DforceStdout)
ls -la .
pwd
env
cp "target/clusterfuzzlite-demo-$CURRENT_VERSION-jar-with-dependencies.jar" $OUT/clusterfuzzlite-demo.jar

PROJECT_JARS="clusterfuzzlite-demo.jar"


# All .jar and .class files lie in the same directory as the fuzzer at runtime.
BUILD_CLASSPATH=$(echo $PROJECT_JARS | xargs printf -- "$OUT/%s:"):$JAZZER_API_PATH

# All class files lie in the same directory as the fuzzer at runtime.
RUNTIME_CLASSPATH=$(echo $PROJECT_JARS | xargs printf -- "\$this_dir/%s:"):\$this_dir

cp "$SRC/jazzer_agent_deploy.jar" "$OUT/jazzer_agent_deploy.jar"

for fuzzer in $(find $SRC -name '*Fuzzer.java' -or -name '*FuzzerNative.java'); do
  fuzzer_basename=$(basename -s .java $fuzzer)
  javac -cp $BUILD_CLASSPATH $fuzzer
  cp $PWD/$fuzzer_basename.class $OUT/

  driver=jazzer_driver

  cp default.options $OUT/"$fuzzer_basename".options
  # Create execution wrapper.
  echo "#!/bin/sh
# LLVMFuzzerTestOneInput for fuzzer detection.
this_dir=\$(dirname \"\$0\")
LD_LIBRARY_PATH=\"$JVM_LD_LIBRARY_PATH\":\$this_dir/native \
ASAN_OPTIONS=\$ASAN_OPTIONS:symbolize=1:external_symbolizer_path=\$this_dir/llvm-symbolizer:detect_leaks=0 \
\$this_dir/$driver --agent_path=\$this_dir/jazzer_agent_deploy.jar \
--cp=$RUNTIME_CLASSPATH \
--target_class=$fuzzer_basename \
--jvm_args=\"-Xmx2048m\" \
\$@ -use_value_profile=1" > $OUT/$fuzzer_basename
  chmod +x $OUT/$fuzzer_basename
done

