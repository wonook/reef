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
package org.apache.reef.tang.implementation.types;

import org.apache.reef.tang.types.Node;
import org.apache.reef.tang.types.PackageNode;

public class PackageNodeImpl extends AbstractNode implements PackageNode {
  public PackageNodeImpl(final Node parent, final String name, final String fullName) {
    super(parent, name, fullName);
  }

  public PackageNodeImpl() {
    this(null, "", "[root node]");
  }

  /**
   * Unlike normal nodes, the root node needs to take the package name of its
   * children into account.  Therefore, we use the full name as the key when
   * we insert nodes into the root.
   */
  @Override
  public void put(final Node n) {
    super.children.put(n.getFullName(), n);
  }

}
