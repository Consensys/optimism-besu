package org.hyperledger.besu.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import static java.util.Objects.isNull;

public class JsonOptimismConfigOptions extends JsonGenesisConfigOptions implements OptimismConfigOptions {
  /** The constant DEFAULT. */
  public static final OptimismConfigOptions DEFAULT =
      new JsonOptimismConfigOptions();

  private static final String OPTIMISM_KEY = "optimism";

  private final ObjectNode optimismConfigRoot;

  /**
   * Instantiates a new empty Optimism config options.
   */
  JsonOptimismConfigOptions() {
    super(null, null, null);
    this.optimismConfigRoot = JsonUtil.createEmptyObjectNode();
  }

  /**
   * Instantiates a new Optimism config options.
   *
   * @param maybeConfig the optional config
   * @param configOverrides the config overrides map
   * @param transitionsConfig the transitions configuration
   */
  JsonOptimismConfigOptions(
          final ObjectNode maybeConfig,
          final Map<String, String> configOverrides,
          final TransitionsConfigOptions transitionsConfig) {
    super(maybeConfig, configOverrides, transitionsConfig);
    Optional<ObjectNode> optionNode = JsonUtil.getObjectNode(maybeConfig, OPTIMISM_KEY);
    this.optimismConfigRoot = optionNode.orElseGet(JsonUtil::createEmptyObjectNode);
  }

  /**
   * Gets EIP1559 elasticity.
   *
   * @return the EIP1559 elasticity
   */
  public OptionalLong getEIP1559Elasticity() {
    OptionalLong eip1559elasticity = JsonUtil.getLong(optimismConfigRoot, "eip1559elasticity");
    return eip1559elasticity.isEmpty() ? OptionalLong.of(6L) : eip1559elasticity;
  }

  /**
   * Gets EIP1559 denominator.
   *
   * @return the EIP1559 denominator
   */
  public OptionalLong getEIP1559Denominator() {
    OptionalLong eip1559Denominator = JsonUtil.getLong(optimismConfigRoot, "eip1559denominator");
    return eip1559Denominator.isEmpty() ? OptionalLong.of(50L) : eip1559Denominator;
  }

  /**
   * Gets EIP1559 denominatorCanyon.
   *
   * @return the EIP1559 denominatorCanyon
   */
  public OptionalLong getEIP1559DenominatorCanyon() {
    OptionalLong eip1559DenominatorCanyon =
        JsonUtil.getLong(optimismConfigRoot, "eip1559denominatorcanyon");
    return eip1559DenominatorCanyon.isEmpty() ? OptionalLong.of(250L) : eip1559DenominatorCanyon;
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

    getContractSizeLimit().ifPresent(l -> builder.put("contractSizeLimit", l));
    getEvmStackSize().ifPresent(l -> builder.put("evmstacksize", l));
    getEcip1017EraRounds().ifPresent(l -> builder.put("ecip1017EraRounds", l));

    getWithdrawalRequestContractAddress()
            .ifPresent(l -> builder.put("withdrawalRequestContractAddress", l));
    getDepositContractAddress().ifPresent(l -> builder.put("depositContractAddress", l));
    getConsolidationRequestContractAddress()
            .ifPresent(l -> builder.put("consolidationRequestContractAddress", l));

    if (isClique()) {
      builder.put("clique", getCliqueConfigOptions().asMap());
    }
    if (isEthHash()) {
      builder.put("ethash", getEthashConfigOptions().asMap());
    }
    if (isIbft2()) {
      builder.put("ibft2", getBftConfigOptions().asMap());
    }
    if (isQbft()) {
      builder.put("qbft", getQbftConfigOptions().asMap());
    }

    if (isZeroBaseFee()) {
      builder.put("zeroBaseFee", true);
    }

    if (isFixedBaseFee()) {
      builder.put("fixedBaseFee", true);
    }

    getEIP1559Elasticity().ifPresent(l -> builder.put("eip1559Elasticity", l));
    getEIP1559Denominator().ifPresent(l -> builder.put("eip1559Denominator", l));
    getEIP1559DenominatorCanyon().ifPresent(l -> builder.put("eip1559DenominatorCanyon", l));
    return builder.build();
  }
}
