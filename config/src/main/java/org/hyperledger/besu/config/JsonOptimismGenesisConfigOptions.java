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

import java.util.Map;
import java.util.OptionalLong;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

/** Json impolementation of Optimism genesis config options. */
public class JsonOptimismGenesisConfigOptions extends JsonGenesisConfigOptions
    implements OptimismGenesisConfigOptions {

  private static final String OPTIMISM_CONFIG_KEY = "optimism";

  /**
   * Instantiates a new Optimism genesis options.
   *
   * @param maybeConfig the optional config
   * @param configOverrides the config overrides map
   * @param transitionsConfig the transitions configuration
   */
  JsonOptimismGenesisConfigOptions(
      final ObjectNode maybeConfig,
      final Map<String, String> configOverrides,
      final TransitionsConfigOptions transitionsConfig) {
    super(maybeConfig, configOverrides, transitionsConfig);
  }

  @Override
  public boolean isOptimism() {
    return configRoot.has(OPTIMISM_CONFIG_KEY);
  }

  @Override
  public OptimismConfigOptions getOptimismConfigOptions() {
    return JsonUtil.getObjectNode(configRoot, OPTIMISM_CONFIG_KEY)
        .map(JsonOptimismConfigOptions::new)
        .orElse(JsonOptimismConfigOptions.DEFAULT);
  }

  @Override
  public OptionalLong getBedrockBlock() {
    return getOptionalLong("bedrockblock");
  }

  @Override
  public boolean isBedrockBlock(final long headBlock) {
    OptionalLong bedrockBlock = getBedrockBlock();
    if (bedrockBlock.isEmpty()) {
      return false;
    }
    return bedrockBlock.getAsLong() <= headBlock;
  }

  @Override
  public OptionalLong getRegolithTime() {
    return getOptionalLong("regolithtime");
  }

  @Override
  public boolean isRegolith(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var regolithTime = getRegolithTime();
    if (regolithTime.isPresent()) {
      return regolithTime.getAsLong() <= headTime;
    }
    return false;
  }

  @Override
  public OptionalLong getCanyonTime() {
    return getOptionalLong("canyontime");
  }

  @Override
  public boolean isCanyon(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var canyonTime = getCanyonTime();
    if (canyonTime.isPresent()) {
      return canyonTime.getAsLong() <= headTime;
    }
    return false;
  }

  @Override
  public OptionalLong getEcotoneTime() {
    return getOptionalLong("ecotonetime");
  }

  @Override
  public boolean isEcotone(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var ecotoneTime = getEcotoneTime();
    if (ecotoneTime.isPresent()) {
      return ecotoneTime.getAsLong() <= headTime;
    }
    return false;
  }

  @Override
  public OptionalLong getFjordTime() {
    return getOptionalLong("fjordtime");
  }

  @Override
  public boolean isFjord(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var fjordTime = getFjordTime();
    if (fjordTime.isPresent()) {
      return fjordTime.getAsLong() <= headTime;
    }
    return false;
  }

  @Override
  public OptionalLong getHoloceneTime() {
    return OptionalLong.empty();
  }

  @Override
  public boolean isHolocene(final long headTime) {
    return false;
  }

  @Override
  public OptionalLong getGraniteTime() {
    return getOptionalLong("granitetime");
  }

  @Override
  public boolean isGranite(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var graniteTime = getGraniteTime();
    if (graniteTime.isPresent()) {
      return graniteTime.getAsLong() <= headTime;
    }
    return false;
  }

  @Override
  public OptionalLong getInteropTime() {
    return getOptionalLong("interoptime");
  }

  @Override
  public boolean isInterop(final long headTime) {
    if (!isOptimism()) {
      return false;
    }
    var ecotoneTime = getEcotoneTime();
    if (ecotoneTime.isPresent()) {
      return ecotoneTime.getAsLong() <= headTime;
    }
    return false;
  }

  /**
   * As map.
   *
   * @return the map
   */
  @Override
  public Map<String, Object> asMap() {
    final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    getChainId().ifPresent(chainId -> builder.put("chainId", chainId));

    // mainnet fork blocks
    getHomesteadBlockNumber().ifPresent(l -> builder.put("homesteadBlock", l));
    getDaoForkBlock().ifPresent(l -> builder.put("daoForkBlock", l));
    getTangerineWhistleBlockNumber().ifPresent(l -> builder.put("eip150Block", l));
    getSpuriousDragonBlockNumber().ifPresent(l -> builder.put("eip158Block", l));
    getByzantiumBlockNumber().ifPresent(l -> builder.put("byzantiumBlock", l));
    getConstantinopleBlockNumber().ifPresent(l -> builder.put("constantinopleBlock", l));
    getPetersburgBlockNumber().ifPresent(l -> builder.put("petersburgBlock", l));
    getIstanbulBlockNumber().ifPresent(l -> builder.put("istanbulBlock", l));
    getMuirGlacierBlockNumber().ifPresent(l -> builder.put("muirGlacierBlock", l));
    getBerlinBlockNumber().ifPresent(l -> builder.put("berlinBlock", l));
    getLondonBlockNumber().ifPresent(l -> builder.put("londonBlock", l));
    getArrowGlacierBlockNumber().ifPresent(l -> builder.put("arrowGlacierBlock", l));
    getGrayGlacierBlockNumber().ifPresent(l -> builder.put("grayGlacierBlock", l));
    getMergeNetSplitBlockNumber().ifPresent(l -> builder.put("mergeNetSplitBlock", l));
    getShanghaiTime().ifPresent(l -> builder.put("shanghaiTime", l));
    getCancunTime().ifPresent(l -> builder.put("cancunTime", l));
    getCancunEOFTime().ifPresent(l -> builder.put("cancunEOFTime", l));
    getPragueTime().ifPresent(l -> builder.put("pragueTime", l));
    getOsakaTime().ifPresent(l -> builder.put("osakaTime", l));
    getTerminalBlockNumber().ifPresent(l -> builder.put("terminalBlockNumber", l));
    getTerminalBlockHash().ifPresent(h -> builder.put("terminalBlockHash", h.toHexString()));
    getFutureEipsTime().ifPresent(l -> builder.put("futureEipsTime", l));
    getExperimentalEipsTime().ifPresent(l -> builder.put("experimentalEipsTime", l));

    // classic fork blocks
    getClassicForkBlock().ifPresent(l -> builder.put("classicForkBlock", l));
    getEcip1015BlockNumber().ifPresent(l -> builder.put("ecip1015Block", l));
    getDieHardBlockNumber().ifPresent(l -> builder.put("dieHardBlock", l));
    getGothamBlockNumber().ifPresent(l -> builder.put("gothamBlock", l));
    getDefuseDifficultyBombBlockNumber().ifPresent(l -> builder.put("ecip1041Block", l));
    getAtlantisBlockNumber().ifPresent(l -> builder.put("atlantisBlock", l));
    getAghartaBlockNumber().ifPresent(l -> builder.put("aghartaBlock", l));
    getPhoenixBlockNumber().ifPresent(l -> builder.put("phoenixBlock", l));
    getThanosBlockNumber().ifPresent(l -> builder.put("thanosBlock", l));
    getMagnetoBlockNumber().ifPresent(l -> builder.put("magnetoBlock", l));
    getMystiqueBlockNumber().ifPresent(l -> builder.put("mystiqueBlock", l));
    getSpiralBlockNumber().ifPresent(l -> builder.put("spiralBlock", l));

    // optimism fork blocks
    getBedrockBlock().ifPresent(l -> builder.put("bedrockBlock", l));
    getRegolithTime().ifPresent(l -> builder.put("regolithTime", l));
    getCanyonTime().ifPresent(l -> builder.put("canyonTime", l));
    getEcotoneTime().ifPresent(l -> builder.put("ecotoneTime", l));
    getFjordTime().ifPresent(l -> builder.put("fjordTime", l));
    getHoloceneTime().ifPresent(l -> builder.put("holoceneTime", l));
    getGraniteTime().ifPresent(l -> builder.put("graniteTime", l));
    getInteropTime().ifPresent(l -> builder.put("interopTime", l));

    getContractSizeLimit().ifPresent(l -> builder.put("contractSizeLimit", l));
    getEvmStackSize().ifPresent(l -> builder.put("evmstacksize", l));
    getEcip1017EraRounds().ifPresent(l -> builder.put("ecip1017EraRounds", l));

    getWithdrawalRequestContractAddress()
        .ifPresent(l -> builder.put("withdrawalRequestContractAddress", l));
    getDepositContractAddress().ifPresent(l -> builder.put("depositContractAddress", l));
    getConsolidationRequestContractAddress()
        .ifPresent(l -> builder.put("consolidationRequestContractAddress", l));

    if (isZeroBaseFee()) {
      builder.put("zeroBaseFee", true);
    }

    if (isFixedBaseFee()) {
      builder.put("fixedBaseFee", true);
    }
    if (isOptimism()) {
      builder.put("optimism", getOptimismConfigOptions().asMap());
    }
    return builder.build();
  }
}
