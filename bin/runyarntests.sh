#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

export REEF_TEST=YARN
DEPENDENCY_JAR=`echo $REEF_HOME/lang/java/reef-tests/target/reef-tests-*-test-jar-with-dependencies.jar`
CLASSPATH=`yarn classpath`

if [ -z ${REEF_HOME+x} ]
then
  echo "REEF_HOME is not set. Please set REEF_HOME on your bash."
fi

PREREQUISITE="cd $REEF_HOME/lang/java/reef-tests && mvn jar:test-jar && cd $REEF_HOME"
echo $PREREQUISITE
$PREREQUISITE

CMD="java -cp $YARN_CONF_DIR:$DEPENDENCY_JAR:$CLASSPATH org.junit.runner.JUnitCore org.apache.reef.tests.AllTestsSuite $*" 
echo $CMD
$CMD
