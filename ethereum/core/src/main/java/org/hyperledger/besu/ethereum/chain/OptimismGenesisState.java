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
package org.hyperledger.besu.ethereum.chain;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.OptimismGenesisConfig;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.worldstate.DataStorageConfiguration;

import java.net.URL;

import com.google.common.annotations.VisibleForTesting;

public class OptimismGenesisState extends GenesisState {

  OptimismGenesisState(final Block block, final GenesisConfig genesisConfigFile) {
    super(block, genesisConfigFile);
  }

  /**
   * Construct a {@link GenesisState} from a JSON string.
   *
   * @param json A JSON string describing the genesis block
   * @param protocolSchedule A protocol Schedule associated with
   * @return A new {@link GenesisState}.
   */
  public static OptimismGenesisState fromJson(
      final String json, final ProtocolSchedule protocolSchedule) {
    return fromConfig(OptimismGenesisConfig.fromConfig(json), protocolSchedule);
  }

  /**
   * Construct a {@link GenesisState} from a URL
   *
   * @param dataStorageConfiguration A {@link DataStorageConfiguration} describing the storage
   *     configuration
   * @param jsonSource A URL pointing to JSON genesis file
   * @param protocolSchedule A protocol Schedule associated with
   * @return A new {@link GenesisState}.
   */
  @VisibleForTesting
  static GenesisState fromJsonSource(
      final DataStorageConfiguration dataStorageConfiguration,
      final URL jsonSource,
      final ProtocolSchedule protocolSchedule) {
    return fromConfig(
        dataStorageConfiguration, OptimismGenesisConfig.fromConfig(jsonSource), protocolSchedule);
  }

  /**
   * Construct a {@link GenesisState} from a genesis file object.
   *
   * @param config A {@link OptimismGenesisState} describing the genesis block.
   * @param protocolSchedule A protocol Schedule associated with
   * @return A new {@link GenesisState}.
   */
  public static OptimismGenesisState fromConfig(
      final OptimismGenesisConfig config, final ProtocolSchedule protocolSchedule) {
    return OptimismGenesisState.fromConfig(
        DataStorageConfiguration.DEFAULT_CONFIG, config, protocolSchedule);
  }

  /**
   * Construct a {@link GenesisState} from a JSON object for Optimism.
   *
   * @param dataStorageConfiguration A {@link DataStorageConfiguration} describing the storage
   *     configuration
   * @param genesisConfigFile A {@link OptimismGenesisConfig} describing the genesis block.
   * @param protocolSchedule A protocol Schedule associated with
   * @return A new {@link GenesisState}.
   */
  public static OptimismGenesisState fromConfig(
      final DataStorageConfiguration dataStorageConfiguration,
      final OptimismGenesisConfig genesisConfigFile,
      final ProtocolSchedule protocolSchedule) {
    // for optimism mainnet, it will modify genesis state root.
    final Hash genesisStateRoot;
    if (!genesisConfigFile.getStateHash().isEmpty()
        && genesisConfigFile.streamAllocations().findAny().isEmpty()) {
      genesisStateRoot = Hash.fromHexStringLenient(genesisConfigFile.getStateHash());
    } else {
      // other optimism network, it will calculate genesis state root.
      genesisStateRoot =
          GenesisState.calculateGenesisStateRoot(dataStorageConfiguration, genesisConfigFile);
    }
    final Block block =
        new Block(
            GenesisState.buildHeader(genesisConfigFile, genesisStateRoot, protocolSchedule),
            GenesisState.buildBody(genesisConfigFile));
    return new OptimismGenesisState(block, genesisConfigFile);
  }
}
