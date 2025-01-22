package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.math.BigInteger;
import java.util.Optional;
import java.util.OptionalLong;

public class OptimismProtocolSpecFactory extends MainnetProtocolSpecFactory {

  public OptimismProtocolSpecFactory(
      final Optional<BigInteger> chainId,
      final boolean isRevertReasonEnabled,
      final OptionalLong ecip1017EraRounds,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final MetricsSystem metricsSystem) {
    super(
        chainId,
        isRevertReasonEnabled,
        ecip1017EraRounds,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem);
  }

  public ProtocolSpecBuilder regolithDefinition(final GenesisConfigOptions genesisConfigOptions) {
    if (!(genesisConfigOptions instanceof OptimismGenesisConfigOptions optimismConfig)) {
      throw new IllegalArgumentException("config options must be OptimismGenesisConfigOptions");
    }
    return OptimismProtocolSpecs.regolithDefinition(
        chainId,
        isRevertReasonEnabled,
        optimismConfig,
        evmConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem);
  }

  public ProtocolSpecBuilder canyonDefinition(final GenesisConfigOptions genesisConfigOptions) {
    if (!(genesisConfigOptions instanceof OptimismGenesisConfigOptions optimismConfig)) {
      throw new IllegalArgumentException("config options must be OptimismGenesisConfigOptions");
    }
    return OptimismProtocolSpecs.canyonDefinition(
        chainId,
        isRevertReasonEnabled,
        optimismConfig,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem);
  }

  public ProtocolSpecBuilder fjordDefinition(final GenesisConfigOptions genesisConfigOptions) {
    if (!(genesisConfigOptions instanceof OptimismGenesisConfigOptions optimismConfig)) {
      throw new IllegalArgumentException("config options must be OptimismGenesisConfigOptions");
    }
    return OptimismProtocolSpecs.fjordDefinition(
        chainId,
        isRevertReasonEnabled,
        optimismConfig,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem);
  }

  public ProtocolSpecBuilder graniteDefinition(final GenesisConfigOptions genesisConfigOptions) {
    if (!(genesisConfigOptions instanceof OptimismGenesisConfigOptions optimismConfig)) {
      throw new IllegalArgumentException("config options must be OptimismGenesisConfigOptions");
    }
    return OptimismProtocolSpecs.graniteDefinition(
        chainId,
        isRevertReasonEnabled,
        optimismConfig,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem);
  }

}
