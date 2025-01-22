package org.hyperledger.besu.evm;

import org.hyperledger.besu.evm.gascalculator.CancunGasCalculator;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.gascalculator.optimism.FjordGasCalculator;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.operation.OperationRegistry;

import java.math.BigInteger;
@SuppressWarnings("UnusedMethod")
public class OptimismEVMs {

  /**
   * Fjord evm.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM fjord(final EvmConfiguration evmConfiguration) {
    return fjord(MainnetEVMs.DEV_NET_CHAIN_ID, evmConfiguration);
  }

  /**
   * Fjord evm.
   *
   * @param chainId the chain id
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM fjord(final BigInteger chainId, final EvmConfiguration evmConfiguration) {
    return fjord(new FjordGasCalculator(), chainId, evmConfiguration);
  }

  /**
   * Fjord evm.
   *
   * @param gasCalculator the gas calculator
   * @param chainId the chain id
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM fjord(
      final GasCalculator gasCalculator,
      final BigInteger chainId,
      final EvmConfiguration evmConfiguration) {
    return new EVM(
        fjordOperations(gasCalculator, chainId),
        gasCalculator,
        evmConfiguration,
        // todo refactor EVMSpecVersion, and add optimism evm spec version, fjord
        EvmSpecVersion.CANCUN);
  }

  /**
   * Operation registry for fjord's operations.
   *
   * @param gasCalculator the gas calculator
   * @param chainId the chain id
   * @return the operation registry
   */
  public static OperationRegistry fjordOperations(
      final GasCalculator gasCalculator, final BigInteger chainId) {
    OperationRegistry operationRegistry = new OperationRegistry();
    registerFjordOperations(operationRegistry, gasCalculator, chainId);
    return operationRegistry;
  }

  /**
   * Register fjord operations.
   *
   * @param registry the registry
   * @param gasCalculator the gas calculator
   * @param chainID the chain id
   */
  public static void registerFjordOperations(
      final OperationRegistry registry,
      final GasCalculator gasCalculator,
      final BigInteger chainID) {
    MainnetEVMs.registerCancunOperations(registry, gasCalculator, chainID);
  }

  /**
   * Granite evm.
   *
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM granite(final EvmConfiguration evmConfiguration) {
    return granite(MainnetEVMs.DEV_NET_CHAIN_ID, evmConfiguration);
  }

  /**
   * Granite evm.
   *
   * @param chainId the chain id
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM granite(final BigInteger chainId, final EvmConfiguration evmConfiguration) {
    return granite(new FjordGasCalculator(), chainId, evmConfiguration);
  }

  /**
   * Granite evm.
   *
   * @param gasCalculator the gas calculator
   * @param chainId the chain id
   * @param evmConfiguration the evm configuration
   * @return the evm
   */
  public static EVM granite(
      final GasCalculator gasCalculator,
      final BigInteger chainId,
      final EvmConfiguration evmConfiguration) {
    return new EVM(
        graniteOperations(gasCalculator, chainId),
        gasCalculator,
        evmConfiguration,
        // todo refactor EVMSpecVersion, and add optimism evm spec version, granite
        EvmSpecVersion.CANCUN);
  }

  /**
   * Operation registry for granite's operations.
   *
   * @param gasCalculator the gas calculator
   * @param chainId the chain id
   * @return the operation registry
   */
  public static OperationRegistry graniteOperations(
      final GasCalculator gasCalculator, final BigInteger chainId) {
    OperationRegistry operationRegistry = new OperationRegistry();
    registerGraniteOperations(operationRegistry, gasCalculator, chainId);
    return operationRegistry;
  }

  /**
   * Register granite operations.
   *
   * @param registry the registry
   * @param gasCalculator the gas calculator
   * @param chainID the chain id
   */
  public static void registerGraniteOperations(
      final OperationRegistry registry,
      final GasCalculator gasCalculator,
      final BigInteger chainID) {
    registerFjordOperations(registry, gasCalculator, chainID);
  }

}
