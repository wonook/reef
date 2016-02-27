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
package org.apache.reef.runtime.standalone.client;

import org.apache.reef.runtime.common.parameters.JVMHeapSlack;
import org.apache.reef.runtime.standalone.driver.StandaloneDriverConfiguration;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.tang.formats.ConfigurationModule;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

/**
 * Helper class that assembles the driver configuration when run on the local runtime.
 */
public final class DriverConfigurationProvider {

  private final double jvmHeapSlack;

  @Inject
  DriverConfigurationProvider(@Parameter(JVMHeapSlack.class) final double jvmHeapSlack) {
    this.jvmHeapSlack = jvmHeapSlack;
  }

  private Configuration getDriverConfiguration(final File jobFolder,
                                               final String clientRemoteId,
                                               final String jobId,
                                               final Set<String> nodeInfoSet,
                                               final String nodeFolder) {
    ConfigurationModule configModule = StandaloneDriverConfiguration.CONF
        .set(StandaloneDriverConfiguration.ROOT_FOLDER, jobFolder.getAbsolutePath())
        .set(StandaloneDriverConfiguration.NODE_FOLDER, nodeFolder)
        .set(StandaloneDriverConfiguration.JVM_HEAP_SLACK, this.jvmHeapSlack)
        .set(StandaloneDriverConfiguration.CLIENT_REMOTE_IDENTIFIER, clientRemoteId)
        .set(StandaloneDriverConfiguration.JOB_IDENTIFIER, jobId);

    for (final String nodeInfo : nodeInfoSet) {
      configModule = configModule.set(StandaloneDriverConfiguration.NODE_INFO_SET, nodeInfo);
    }

    return configModule.build();
  }

  /**
   * Assembles the driver configuration.
   *
   * @param jobFolder                The folder in which the local runtime will execute this job.
   * @param clientRemoteId           the remote identifier of the client. It is used by the Driver to establish a
   *                                 connection back to the client.
   * @param jobId                    The identifier of the job.
   * @param applicationConfiguration The configuration of the application, e.g. a filled out DriverConfiguration
   * @return The Driver configuration to be used to instantiate the Driver.
   */
  public Configuration getDriverConfiguration(final File jobFolder,
                                              final String clientRemoteId,
                                              final String jobId,
                                              final Configuration applicationConfiguration,
                                              final Set<String> nodeInfoSet,
                                              final String nodeFolder) {
    return Configurations.merge(getDriverConfiguration(jobFolder, clientRemoteId, jobId, nodeInfoSet, nodeFolder),
        applicationConfiguration);
  }
}
