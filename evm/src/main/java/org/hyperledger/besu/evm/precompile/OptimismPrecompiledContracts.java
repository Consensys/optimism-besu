package org.hyperledger.besu.evm.precompile;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import static org.hyperledger.besu.evm.precompile.MainnetPrecompiledContracts.populateForCancun;

public interface OptimismPrecompiledContracts {

  /** The constant P256_VERIFY. */
  Address P256_VERIFY = Address.precompiled(0x0100);

  /**
   * Fjord precompile contract registry.
   *
   * @param gasCalculator the gas calculator
   * @return the precompile contract registry
   */
  static PrecompileContractRegistry fjord(final GasCalculator gasCalculator) {
    PrecompileContractRegistry precompileContractRegistry = new PrecompileContractRegistry();
    populateForFjord(precompileContractRegistry, gasCalculator);
    return precompileContractRegistry;
  }

  /**
   * Populate registry for Fjord.
   *
   * @param registry the registry
   * @param gasCalculator the gas calculator
   */
  static void populateForFjord(
      final PrecompileContractRegistry registry, final GasCalculator gasCalculator) {
    populateForCancun(registry, gasCalculator);
    registry.put(P256_VERIFY, new P256VerifyPrecompiledContract());
  }

  /**
   * Fjord precompile contract registry.
   *
   * @param gasCalculator the gas calculator
   * @return the precompile contract registry
   */
  static PrecompileContractRegistry granite(final GasCalculator gasCalculator) {
    PrecompileContractRegistry precompileContractRegistry = new PrecompileContractRegistry();
    populateForGranite(precompileContractRegistry, gasCalculator);
    return precompileContractRegistry;
  }

  /**
   * Populate registry for Fjord.
   *
   * @param registry the registry
   * @param gasCalculator the gas calculator
   */
  static void populateForGranite(
      final PrecompileContractRegistry registry, final GasCalculator gasCalculator) {
    populateForFjord(registry, gasCalculator);
    registry.put(
        Address.ALTBN128_PAIRING, AltBN128PairingPrecompiledContract.granite(gasCalculator));
  }

}
