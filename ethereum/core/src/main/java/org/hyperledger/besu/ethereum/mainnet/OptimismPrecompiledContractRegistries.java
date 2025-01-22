package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.evm.precompile.PrecompileContractRegistry;

import static org.hyperledger.besu.evm.precompile.OptimismPrecompiledContracts.populateForFjord;
import static org.hyperledger.besu.evm.precompile.OptimismPrecompiledContracts.populateForGranite;

public class OptimismPrecompiledContractRegistries {

  static PrecompileContractRegistry fjord(
      final PrecompiledContractConfiguration precompiledContractConfiguration) {
    final PrecompileContractRegistry registry = new PrecompileContractRegistry();
    populateForFjord(registry, precompiledContractConfiguration.getGasCalculator());
    return registry;
  }

  static PrecompileContractRegistry granite(
      final PrecompiledContractConfiguration precompiledContractConfiguration) {
    final PrecompileContractRegistry registry = new PrecompileContractRegistry();
    populateForGranite(registry, precompiledContractConfiguration.getGasCalculator());
    return registry;
  }


}
