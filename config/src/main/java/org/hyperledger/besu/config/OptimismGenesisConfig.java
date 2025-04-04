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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

/** Interface for Optimism genesis config file. */
public class OptimismGenesisConfig extends GenesisConfig {

  OptimismGenesisConfig(final GenesisReader loader) {
    super(loader);
  }

  /**
   * Mainnet genesis config file.
   *
   * @return the genesis config file
   */
  public static OptimismGenesisConfig mainnet() {
    return fromSource(GenesisConfig.class.getResource("/optimism-mainnet.json"));
  }

  /**
   * Genesis file from URL.
   *
   * @param jsonSource the URL
   * @return the genesis config file
   */
  public static OptimismGenesisConfig fromSource(final URL jsonSource) {
    return fromConfig(JsonUtil.objectNodeFromURL(jsonSource, false));
  }

  /**
   * Genesis file from resource.
   *
   * @param resourceName the resource name
   * @return the genesis config file
   */
  public static OptimismGenesisConfig fromResource(final String resourceName) {
    return fromConfig(GenesisConfig.class.getResource(resourceName));
  }

  /**
   * From config genesis config file.
   *
   * @param jsonSource the json string
   * @return the genesis config file
   */
  public static OptimismGenesisConfig fromConfig(final URL jsonSource) {
    return new OptimismGenesisConfig(new GenesisReader.FromURL(jsonSource));
  }

  /**
   * From config genesis config file.
   *
   * @param json the json string
   * @return the genesis config file
   */
  public static OptimismGenesisConfig fromConfig(final String json) {
    return fromConfig(JsonUtil.objectNodeFromString(json, false));
  }

  /**
   * From config genesis config file.
   *
   * @param config the config
   * @return the genesis config file
   */
  public static OptimismGenesisConfig fromConfig(final ObjectNode config) {
    return new OptimismGenesisConfig(new GenesisReader.FromObjectNode(config));
  }

  /**
   * Gets parent hash.
   *
   * @return the parent hash
   */
  public String getStateHash() {
    return JsonUtil.getString(genesisRoot, "statehash", "");
  }

  /**
   * Gets config options, including any overrides.
   *
   * @return the config options
   */
  @Override
  public JsonOptimismGenesisConfigOptions getConfigOptions() {
    final ObjectNode config = loader.getConfig();
    // are there any overrides to apply?
    if (this.overrides == null) {
      return JsonOptimismGenesisConfigOptions.fromJsonObject(config);
    }
    // otherwise apply overrides
    Map<String, String> overridesRef = this.overrides;

    // if baseFeePerGas has been explicitly configured, pass it as an override:
    final var optBaseFee = getBaseFeePerGas();
    if (optBaseFee.isPresent()) {
      // streams and maps cannot handle null values.
      overridesRef = new HashMap<>(this.overrides);
      overridesRef.put("baseFeePerGas", optBaseFee.get().toShortHexString());
    }

    return JsonOptimismGenesisConfigOptions.fromJsonObjectWithOverrides(config, overridesRef);
  }
}
