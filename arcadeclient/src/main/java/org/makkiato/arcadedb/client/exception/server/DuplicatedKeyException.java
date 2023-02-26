/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
package org.makkiato.arcadedb.client.exception.server;

public class DuplicatedKeyException extends ArcadeDBException {
  private final String indexName;
  private final String keys;
  private final String currentIndexedRID;

  public DuplicatedKeyException(final String detail, final String indexName, final String keys, final String currentIndexedRID) {
    super(detail);
    this.indexName = indexName;
    this.keys = keys;
    this.currentIndexedRID = currentIndexedRID;
  }

  public DuplicatedKeyException(String detail) {
    super(detail);
    this.indexName = this.keys = this.currentIndexedRID = null;
  }

  public String getIndexName() {
    return indexName;
  }

  public String getKeys() {
    return keys;
  }

  public String getCurrentIndexedRID() {
    return currentIndexedRID;
  }
}
