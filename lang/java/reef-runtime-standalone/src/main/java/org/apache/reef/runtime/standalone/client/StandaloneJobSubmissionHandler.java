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

import org.apache.reef.annotations.audience.ClientSide;
import org.apache.reef.annotations.audience.Private;
import org.apache.reef.runtime.common.client.api.JobSubmissionEvent;
import org.apache.reef.runtime.common.client.api.JobSubmissionHandler;
import org.apache.reef.runtime.common.files.REEFFileNames;
import org.apache.reef.runtime.standalone.client.parameters.NodeFolder;
import org.apache.reef.runtime.standalone.client.parameters.RootFolder;
import org.apache.reef.runtime.standalone.client.parameters.NodeListFilePath;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.tang.formats.ConfigurationSerializer;
import org.apache.reef.util.logging.LoggingScope;
import org.apache.reef.util.logging.LoggingScopeFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Job Submissions for the Standalone Runtime.
 */
@Private
@ClientSide
final class StandaloneJobSubmissionHandler implements JobSubmissionHandler {


  private static final Logger LOG = Logger.getLogger(StandaloneJobSubmissionHandler.class.getName());
  private final ExecutorService executor;
  private final String rootFolderName;
  private final ConfigurationSerializer configurationSerializer;
  private final REEFFileNames fileNames;
  private final PreparedDriverFolderLauncher driverLauncher;
  private final LoggingScopeFactory loggingScopeFactory;
  private final DriverConfigurationProvider driverConfigurationProvider;
  private final Set<String> nodeInfoSet;
  private final String nodeFolder;

  @Inject
  StandaloneJobSubmissionHandler(
      final ExecutorService executor,
      @Parameter(RootFolder.class) final String rootFolderName,
      @Parameter(NodeListFilePath.class) final String nodeListFilePath,
      @Parameter(NodeFolder.class) final String nodeFolder,
      final ConfigurationSerializer configurationSerializer,
      final REEFFileNames fileNames,

      final PreparedDriverFolderLauncher driverLauncher,
      final LoggingScopeFactory loggingScopeFactory,
      final DriverConfigurationProvider driverConfigurationProvider) {

    this.executor = executor;
    this.configurationSerializer = configurationSerializer;
    this.fileNames = fileNames;

    this.driverLauncher = driverLauncher;
    this.driverConfigurationProvider = driverConfigurationProvider;
    this.rootFolderName = new File(rootFolderName).getAbsolutePath();
    this.loggingScopeFactory = loggingScopeFactory;
    this.nodeFolder = nodeFolder;

    LOG.log(Level.FINEST, "Reading NodeListFilePath");
    this.nodeInfoSet = new HashSet<>();
    try {
      final InputStream in = new FileInputStream(nodeListFilePath);
      final Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
      final BufferedReader br = new BufferedReader(reader);
      while (true) {
        final String line = br.readLine();
        if (line == null) {
          break;
        }
        nodeInfoSet.add(line);
      }
      br.close();
    } catch (final FileNotFoundException ex) {
      LOG.log(Level.SEVERE, "Failed to open file in NodeListFilePath: {0}", nodeListFilePath);
      throw new RuntimeException("Failed to open file in NodeListFilePath: " + nodeListFilePath, ex);
    } catch (final IOException ex) {
      LOG.log(Level.SEVERE, "Failed to read file");
      throw new RuntimeException("Failed to read file", ex);
    }

    LOG.log(Level.FINE, "Instantiated 'StandaloneJobSubmissionHandler'");
  }

  @Override
  public void close() {
    this.executor.shutdown();
  }

  @Override
  public void onNext(final JobSubmissionEvent t) {
    try (final LoggingScope lf = loggingScopeFactory.localJobSubmission()) {
      try {
        LOG.log(Level.FINEST, "Starting standalone job {0}", t.getIdentifier());

        final File jobFolder = new File(new File(rootFolderName),
            "/" + t.getIdentifier() + "-" + System.currentTimeMillis() + "/");

        final File driverFolder = new File(jobFolder, PreparedDriverFolderLauncher.DRIVER_FOLDER_NAME);
        if (!driverFolder.exists() && !driverFolder.mkdirs()) {
          LOG.log(Level.WARNING, "Failed to create [{0}]", driverFolder.getAbsolutePath());
        }

        final DriverFiles driverFiles = DriverFiles.fromJobSubmission(t, this.fileNames);
        driverFiles.copyTo(driverFolder);

        final Configuration driverConfiguration = this.driverConfigurationProvider
            .getDriverConfiguration(jobFolder, t.getRemoteId(), t.getIdentifier(),
                    t.getConfiguration(), nodeInfoSet, nodeFolder);

        this.configurationSerializer.toFile(driverConfiguration,
            new File(driverFolder, this.fileNames.getDriverConfigurationPath()));
        this.driverLauncher.launch(driverFolder, t.getIdentifier(), t.getRemoteId());
      } catch (final Exception e) {
        LOG.log(Level.SEVERE, "Unable to setup driver.", e);
        throw new RuntimeException("Unable to setup driver.", e);
      }
    }
  }
}
