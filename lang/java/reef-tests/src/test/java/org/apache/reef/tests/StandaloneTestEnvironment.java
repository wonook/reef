/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reef.tests;

import org.apache.reef.io.ConfigurableDirectoryTempFileCreator;
import org.apache.reef.io.TempFileCreator;
import org.apache.reef.io.parameters.TempFileRootFolder;
import org.apache.reef.runtime.standalone.client.StandaloneRuntimeConfiguration;
import org.apache.reef.runtime.standalone.driver.RuntimeIdentifier;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.JavaConfigurationBuilder;
import org.apache.reef.tang.Tang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * A TestEnvironment for the standalone runtime.
 */
public final class StandaloneTestEnvironment extends TestEnvironmentBase implements TestEnvironment {

  // Used to make sure the tests call the methods in the right order.
  private boolean ready = false;
  private final String nodeListFilePath = "../NodeList.txt";

  @Override
  public synchronized void setUp() {
    this.ready = true;

    // Create a file with localhost on it
    try {
      final List<String> lines = Arrays.asList("localhost");
      final Path file = Paths.get(nodeListFilePath);
      Files.write(file, lines, Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("Error while writing to " + nodeListFilePath, e);
    }
  }

  @Override
  public synchronized Configuration getRuntimeConfiguration() {
    assert this.ready;
    final String rootFolder = System.getProperty("org.apache.reef.runtime.standalone.folder");
    final JavaConfigurationBuilder jcb = Tang.Factory.getTang().newConfigurationBuilder();
    jcb.bindNamedParameter(TempFileRootFolder.class, "./target/reef/temp");
    jcb.bindImplementation(TempFileCreator.class, ConfigurableDirectoryTempFileCreator.class);
    if (rootFolder == null) {
      return Configurations.merge(jcb.build(), StandaloneRuntimeConfiguration.CONF
          .set(StandaloneRuntimeConfiguration.RUNTIME_ROOT_FOLDER, "target/REEF_STANDALONE_RUNTIME")
          .set(StandaloneRuntimeConfiguration.NODE_LIST_FILE_PATH, nodeListFilePath)
          .build());
    } else {
      return Configurations.merge(jcb.build(), StandaloneRuntimeConfiguration.CONF
          .set(StandaloneRuntimeConfiguration.RUNTIME_ROOT_FOLDER, rootFolder)
          .set(StandaloneRuntimeConfiguration.NODE_LIST_FILE_PATH, nodeListFilePath)
          .build());
    }
  }

  @Override
  public synchronized void tearDown() {
    assert this.ready;
    this.ready = false;
  }

  @Override
  public int getTestTimeout() {
    return 60000; // 1 min
  }

  @Override
  public String getRuntimeName() {
    return RuntimeIdentifier.RUNTIME_NAME;
  }
}
