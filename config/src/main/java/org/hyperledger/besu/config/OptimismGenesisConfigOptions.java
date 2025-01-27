/*
 * Copyright contributors to Besu.
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
package org.hyperledger.besu.config;

import java.util.OptionalLong;

public interface OptimismGenesisConfigOptions extends GenesisConfigOptions {
  /**
   * Is Optimism boolean
   *
   * @return the boolean
   */
  boolean isOptimism();

  /**
   * Gets bedrock switch block number.
   *
   * @return the bedrock switch block number
   */
  OptionalLong getBedrockBlock();

  /**
   * Returns whether a fork scheduled at bedrock block number is active at the given head block
   * number
   *
   * @param headBlock the head block height
   * @return the boolean
   */
  boolean isBedrockBlock(long headBlock);

  /**
   * Gets regolith time.
   *
   * @return the regolith time
   */
  OptionalLong getRegolithTime();

  /**
   * Returns whether a fork scheduled at regolith timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isRegolith(long headTime);

  /**
   * Gets canyon time.
   *
   * @return the canyon time
   */
  OptionalLong getCanyonTime();

  /**
   * Returns whether a fork scheduled at canyon timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isCanyon(long headTime);

  /**
   * Gets ecotone time.
   *
   * @return the ecotone time
   */
  OptionalLong getEcotoneTime();

  /**
   * Returns whether a fork scheduled at ecotone timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isEcotone(long headTime);

  /**
   * Gets fjord time.
   *
   * @return the fjord time
   */
  OptionalLong getFjordTime();

  /**
   * Returns whether a fork scheduled at fjord timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isFjord(long headTime);

  /**
   * Gets Holocene time.
   *
   * @return the holocene time
   */
  OptionalLong getHoloceneTime();

  /**
   * Returns whether a fork scheduled at holocene timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isHolocene(long headTime);

  /**
   * Gets granite time.
   *
   * @return the granite time
   */
  OptionalLong getGraniteTime();

  /**
   * Returns whether a fork scheduled at granite timestamp is active at the given head timestamp.
   *
   * @param headTime the current head time
   * @return the boolean
   */
  boolean isGranite(long headTime);

  /**
   * Gets optimism config options
   *
   * @return the optimism config options.
   */
  OptimismConfigOptions getOptimismConfigOptions();

  OptionalLong getInteropTime();

  boolean isInterop(long headTime);
}
