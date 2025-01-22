package org.hyperledger.besu.evm.gascalculator.optimism;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.gascalculator.CancunGasCalculator;

public class FjordGasCalculator extends CancunGasCalculator {

  private int maxPrecompile;

  /** Instantiates a new Cancun Gas Calculator. */
  public FjordGasCalculator() {
    this(256);
    this.maxPrecompile = 256;
  }

  /**
   * Instantiates a new Cancun Gas Calculator
   *
   * @param maxPrecompile the max precompile
   */
  protected FjordGasCalculator(final int maxPrecompile) {
    super(maxPrecompile);
    this.maxPrecompile = maxPrecompile;
  }

  @Override
  public boolean isPrecompile(final Address address) {
    final byte[] addressBytes = address.toArrayUnsafe();
    for (int i = 0; i < 18; i++) {
      if (addressBytes[i] != 0) {
        return false;
      }
    }
    final int lastBytes = ((addressBytes[18] & 0xff) << 8) | (addressBytes[19] & 0xff);
    return lastBytes <= maxPrecompile && lastBytes != 0;
  }

}
