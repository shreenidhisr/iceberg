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
package org.apache.iceberg.io;

import java.io.Serializable;
import java.util.Map;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public interface StorageCredential extends Serializable {

  /**
   * Property that names the storage prefix a credential refresh applies to.
   *
   * <p>When a refresh returns several credentials, the credential whose prefix applies to this
   * value is kept. When this property is absent, a refresh expects a single credential.
   */
  String PREFIX_PROPERTY = "storage-credential.prefix";

  String prefix();

  Map<String, String> config();

  /**
   * Returns whether a storage credential prefix applies to a location.
   *
   * <p>A prefix that contains {@code ://} applies to that location and to paths under it. A
   * trailing slash is ignored, so the prefix does not apply to a sibling location that only shares
   * a string prefix. A prefix without {@code ://} applies to any location that starts with it.
   *
   * @param prefix credential prefix
   * @param location storage location or path
   * @return true when the prefix applies to the location
   */
  static boolean covers(String prefix, String location) {
    Preconditions.checkArgument(
        prefix != null && !prefix.isEmpty(), "Invalid prefix: null or empty");
    Preconditions.checkArgument(
        location != null && !location.isEmpty(), "Invalid location: null or empty");
    if (prefix.contains("://")) {
      String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
      String normalizedLocation = location.endsWith("/") ? location : location + "/";
      return normalizedLocation.startsWith(normalizedPrefix);
    }

    return location.startsWith(prefix);
  }

  default void validate() {
    Preconditions.checkArgument(!prefix().isEmpty(), "Invalid prefix: must be non-empty");
    Preconditions.checkArgument(!config().isEmpty(), "Invalid config: must be non-empty");
  }

  static StorageCredential create(String prefix, Map<String, String> config) {
    return ImmutableStorageCredential.builder().prefix(prefix).config(config).build();
  }
}
