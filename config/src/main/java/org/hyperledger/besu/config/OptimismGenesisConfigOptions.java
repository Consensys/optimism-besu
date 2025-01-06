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
}
