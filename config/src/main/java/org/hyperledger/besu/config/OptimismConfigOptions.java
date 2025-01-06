package org.hyperledger.besu.config;

import java.util.OptionalLong;

public interface OptimismConfigOptions {

  /**
   * Gets EIP1559 elasticity.
   *
   * @return the EIP1559 elasticity
   */
  OptionalLong getEIP1559Elasticity();

  /**
   * Gets EIP1559 denominator.
   *
   * @return the EIP1559 denominator
   */
  OptionalLong getEIP1559Denominator();

  /**
   * Gets EIP1559 denominatorCanyon.
   *
   * @return the EIP1559 denominatorCanyon
   */
  OptionalLong getEIP1559DenominatorCanyon();
}
