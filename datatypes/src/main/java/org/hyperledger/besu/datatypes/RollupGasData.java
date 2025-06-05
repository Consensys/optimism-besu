/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.datatypes;

/**
 * Optimism roll up data records.
 *
 * @param zeroes the number of zeroes after tx encoded to bytes
 * @param ones the number of non-zeroes after tx encoded to bytes
 * @param fastLzSize the length of the tx encoded to bytes after compression
 */
@SuppressWarnings("UnusedVariable")
public record RollupGasData(long zeroes, long ones, long fastLzSize) {

  public static final RollupGasData EMPTY = new RollupGasData(0L, 0L, 0L);

  /**
   * Get the number of zeroes.
   *
   * @return the number of zeroes
   */
  @Override
  public long zeroes() {
    return zeroes;
  }

  /**
   * Get the number of non-zeroes.
   *
   * @return the number of non-zeroes
   */
  @Override
  public long ones() {
    return ones;
  }

  /**
   * Get the number of fast-lz size.
   *
   * @return the number of fast-lz size
   */
  @Override
  public long fastLzSize() {
    return fastLzSize;
  }
}
