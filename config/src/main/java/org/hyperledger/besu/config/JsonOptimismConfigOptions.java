package org.hyperledger.besu.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.OptionalLong;

public class JsonOptimismConfigOptions implements OptimismConfigOptions {

  public static final JsonOptimismConfigOptions DEFAULT =
      new JsonOptimismConfigOptions(JsonUtil.createEmptyObjectNode());

  private static final String EIP1559_ELASTICITY = "eip1559elasticity";

  private static final String EIP1559_DENOMINATOR = "eip1559denominator";

  private static final String EIP1559_DENOMINATOR_CANYON = "eip1559denominatorcanyon";

  private final ObjectNode optimismConfigRoot;

  /**
   * Instantiates a new optimism options.
   *
   * @param optimismConfigRoot the optimism config root
   */
  public JsonOptimismConfigOptions(final ObjectNode optimismConfigRoot) {
    this.optimismConfigRoot = optimismConfigRoot;
  }

  /**
   * Gets EIP1559 elasticity.
   *
   * @return the EIP1559 elasticity
   */
  @Override
  public OptionalLong getEIP1559Elasticity() {
    OptionalLong eip1559elasticity = JsonUtil.getLong(optimismConfigRoot, EIP1559_ELASTICITY);
    return eip1559elasticity.isEmpty() ? OptionalLong.of(6L) : eip1559elasticity;
  }

  /**
   * Gets EIP1559 denominator.
   *
   * @return the EIP1559 denominator
   */
  @Override
  public OptionalLong getEIP1559Denominator() {
    OptionalLong eip1559Denominator = JsonUtil.getLong(optimismConfigRoot, EIP1559_DENOMINATOR);
    return eip1559Denominator.isEmpty() ? OptionalLong.of(50L) : eip1559Denominator;
  }

  /**
   * Gets EIP1559 denominatorCanyon.
   *
   * @return the EIP1559 denominatorCanyon
   */
  @Override
  public OptionalLong getEIP1559DenominatorCanyon() {
    OptionalLong eip1559DenominatorCanyon =
        JsonUtil.getLong(optimismConfigRoot, EIP1559_DENOMINATOR_CANYON);
    return eip1559DenominatorCanyon.isEmpty() ? OptionalLong.of(250L) : eip1559DenominatorCanyon;
  }

  @Override
  public Map<String, Object> asMap() {
    final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    if (optimismConfigRoot.has(EIP1559_ELASTICITY)) {
      builder.put(EIP1559_ELASTICITY, getEIP1559Elasticity());
    }
    if (optimismConfigRoot.has(EIP1559_DENOMINATOR)) {
      builder.put(EIP1559_DENOMINATOR, getEIP1559Denominator());
    }
    if (optimismConfigRoot.has(EIP1559_DENOMINATOR_CANYON)) {
      builder.put(EIP1559_DENOMINATOR_CANYON, getEIP1559DenominatorCanyon());
    }
    return builder.build();
  }
}
