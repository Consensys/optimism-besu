package org.hyperledger.besu.evm.fluent;

import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.OptimismEVMs;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.precompile.OptimismPrecompiledContracts;

import java.math.BigInteger;

@SuppressWarnings("UnusedMethod")
public class OpEVMExecutor extends EVMExecutor {

  OpEVMExecutor(final EVM evm) {
    super(evm);
  }

  /**
   * Instantiate Fjord evm executor.
   *
   * @param chainId the chain ID
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  public static EVMExecutor fjord(
      final BigInteger chainId, final EvmConfiguration evmConfiguration) {
    final EVMExecutor executor = new EVMExecutor(OptimismEVMs.fjord(chainId, evmConfiguration));
    executor.precompileContractRegistry =
        OptimismPrecompiledContracts.fjord(executor.evm.getGasCalculator());
    return executor;
  }

  /**
   * Instantiate Granite evm executor.
   *
   * @param chainId the chain ID
   * @param evmConfiguration the evm configuration
   * @return the evm executor
   */
  private static EVMExecutor granite(
      final BigInteger chainId, final EvmConfiguration evmConfiguration) {
    final EVMExecutor executor = new EVMExecutor(OptimismEVMs.granite(chainId, evmConfiguration));
    executor.precompileContractRegistry =
        OptimismPrecompiledContracts.granite(executor.evm.getGasCalculator());
    return executor;
  }

}
