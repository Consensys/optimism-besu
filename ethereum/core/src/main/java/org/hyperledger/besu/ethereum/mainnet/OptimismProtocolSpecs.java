package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.core.feemarket.CoinbaseFeePriceCalculator;
import org.hyperledger.besu.ethereum.mainnet.blockhash.CancunBlockHashProcessor;
import org.hyperledger.besu.ethereum.mainnet.feemarket.BaseFeeMarket;
import org.hyperledger.besu.ethereum.mainnet.feemarket.OpCancunFeeMarket;
import org.hyperledger.besu.ethereum.mainnet.feemarket.OpCanyonFeeMarket;
import org.hyperledger.besu.ethereum.mainnet.feemarket.OpLondonFeeMarket;
import org.hyperledger.besu.evm.MainnetEVMs;
import org.hyperledger.besu.evm.OptimismEVMs;
import org.hyperledger.besu.evm.contractvalidation.MaxCodeSizeRule;
import org.hyperledger.besu.evm.contractvalidation.PrefixCodeRule;
import org.hyperledger.besu.evm.gascalculator.CancunGasCalculator;
import org.hyperledger.besu.evm.gascalculator.optimism.FjordGasCalculator;
import org.hyperledger.besu.evm.gascalculator.LondonGasCalculator;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.processor.ContractCreationProcessor;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecs.SIGNATURE_ALGORITHM;
import static org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecs.berlinDefinition;
import static org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecs.shanghaiDefinition;

public class OptimismProtocolSpecs {

  static ProtocolSpecBuilder regolithDefinition(
      final Optional<BigInteger> chainId,
      final boolean enableRevertReason,
      final OptimismGenesisConfigOptions genesisConfigOptions,
      final EvmConfiguration evmConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final MetricsSystem metricsSystem) {
    final long londonForkBlockNumber =
        genesisConfigOptions.getLondonBlockNumber().orElse(Long.MAX_VALUE);
    final BaseFeeMarket londonFeeMarket =
        new OpLondonFeeMarket(
            londonForkBlockNumber,
            genesisConfigOptions.getBaseFeePerGas(),
            genesisConfigOptions);
    return berlinDefinition(
        chainId,
        enableRevertReason,
        evmConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem)
        .feeMarket(londonFeeMarket)
        .gasCalculator(LondonGasCalculator::new)
        .gasLimitCalculatorBuilder(
            feeMarket ->
                new LondonTargetingGasLimitCalculator(
                    londonForkBlockNumber, (BaseFeeMarket) feeMarket))
        .transactionValidatorFactoryBuilder(
            (evm, gasLimitCalculator, feeMarket) ->
                new OptimismTransactionValidatorFactory(
                    evm.getGasCalculator(),
                    gasLimitCalculator,
                    feeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559,
                        TransactionType.OPTIMISM_DEPOSIT),
                    evm.getEvmVersion().getMaxInitcodeSize(),
                    genesisConfigOptions))
        .transactionProcessorBuilder(
            (gasCalculator,
             feeMarket,
             transactionValidatorFactory,
             contractCreationProcessor,
             messageCallProcessor) ->
                new OptimismTransactionProcessor(
                    gasCalculator,
                    transactionValidatorFactory,
                    contractCreationProcessor,
                    messageCallProcessor,
                    true,
                    false,
                    evmConfiguration.evmStackSize(),
                    feeMarket,
                    CoinbaseFeePriceCalculator.eip1559(),
                    null,
                    genesisConfigOptions))
        .contractCreationProcessorBuilder(
            evm ->
                new ContractCreationProcessor(
                    evm,
                    true,
                    List.of(MaxCodeSizeRule.from(evm), PrefixCodeRule.of()),
                    1,
                    MainnetProtocolSpecs.SPURIOUS_DRAGON_FORCE_DELETE_WHEN_EMPTY_ADDRESSES))
        .evmBuilder(
            (gasCalculator, jdCacheConfig) ->
                MainnetEVMs.london(
                    gasCalculator, chainId.orElse(BigInteger.ZERO), evmConfiguration))
        .difficultyCalculator(MainnetDifficultyCalculators.LONDON)
        .blockHeaderValidatorBuilder(
            feeMarket ->
                MainnetBlockHeaderValidator.createBaseFeeMarketValidator((BaseFeeMarket) feeMarket))
        .ommerHeaderValidatorBuilder(
            feeMarket ->
                MainnetBlockHeaderValidator.createBaseFeeMarketOmmerValidator(
                    (BaseFeeMarket) feeMarket))
        .blockBodyValidatorBuilder(BaseFeeBlockBodyValidator::new)
        .withdrawalsValidator(new WithdrawalsValidator.AllowedWithdrawals())
        .name("Regolith");
  }

  static ProtocolSpecBuilder canyonDefinition(
      final Optional<BigInteger> chainId,
      final boolean enableRevertReason,
      final OptimismGenesisConfigOptions genesisConfigOptions,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final MetricsSystem metricsSystem) {
    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket cancunFeeMarket =
        new OpCanyonFeeMarket(
            londonForkBlockNumber,
            genesisConfigOptions.getBaseFeePerGas(),
            genesisConfigOptions);

    return shanghaiDefinition(
        chainId,
        enableRevertReason,
        genesisConfigOptions,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem)
        .feeMarket(cancunFeeMarket)
        // gas calculator for EIP-4844 blob gas
        .gasCalculator(CancunGasCalculator::new)
        // gas limit with EIP-4844 max blob gas per block
        .gasLimitCalculatorBuilder(
            feeMarket ->
                new CancunTargetingGasLimitCalculator(
                    londonForkBlockNumber, (BaseFeeMarket) feeMarket))
        // EVM changes to support EIP-1153: TSTORE and EIP-5656: MCOPY
        .evmBuilder(
            (gasCalculator, jdCacheConfig) ->
                MainnetEVMs.cancun(
                    gasCalculator, chainId.orElse(BigInteger.ZERO), evmConfiguration))
        // use Cancun fee market
        .transactionProcessorBuilder(
            (gasCalculator,
             feeMarket,
             transactionValidator,
             contractCreationProcessor,
             messageCallProcessor) ->
                new OptimismTransactionProcessor(
                    gasCalculator,
                    transactionValidator,
                    contractCreationProcessor,
                    messageCallProcessor,
                    true,
                    true,
                    evmConfiguration.evmStackSize(),
                    feeMarket,
                    CoinbaseFeePriceCalculator.eip1559(),
                    new CodeDelegationProcessor(
                        chainId, SIGNATURE_ALGORITHM.get().getHalfCurveOrder()),
                    genesisConfigOptions))
        // change to check for max blob gas per block for EIP-4844
        .transactionValidatorFactoryBuilder(
            (evm, gasLimitCalculator, feeMarket) ->
                new OptimismTransactionValidatorFactory(
                    evm.getGasCalculator(),
                    gasLimitCalculator,
                    feeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559,
                        TransactionType.OPTIMISM_DEPOSIT),
                    evm.getEvmVersion().getMaxInitcodeSize(),
                    genesisConfigOptions))
        .precompileContractRegistryBuilder(MainnetPrecompiledContractRegistries::cancun)
        .blockHeaderValidatorBuilder(MainnetBlockHeaderValidator::cancunBlockHeaderValidator)
        .blockHashProcessor(new CancunBlockHashProcessor())
        .name("Canyon");
  }

  static ProtocolSpecBuilder fjordDefinition(
      final Optional<BigInteger> chainId,
      final boolean enableRevertReason,
      final OptimismGenesisConfigOptions genesisConfigOptions,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final MetricsSystem metricsSystem) {

    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket cancunFeeMarket =
        new OpCancunFeeMarket(
            londonForkBlockNumber,
            genesisConfigOptions.getBaseFeePerGas(),
            genesisConfigOptions);

    return shanghaiDefinition(
        chainId,
        enableRevertReason,
        genesisConfigOptions,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem)
        .feeMarket(cancunFeeMarket)
        // gas calculator for EIP-4844 blob gas
        .gasCalculator(FjordGasCalculator::new)
        // gas limit with EIP-4844 max blob gas per block
        .gasLimitCalculatorBuilder(
            feeMarket ->
                new CancunTargetingGasLimitCalculator(
                    londonForkBlockNumber, (BaseFeeMarket) feeMarket))
        // EVM changes to support EIP-1153: TSTORE and EIP-5656: MCOPY
        .evmBuilder(
            (gasCalculator, jdCacheConfig) ->
                OptimismEVMs.fjord(gasCalculator, chainId.orElse(BigInteger.ZERO), evmConfiguration))
        // use Cancun fee market
        .transactionProcessorBuilder(
            (gasCalculator,
             feeMarket,
             transactionValidator,
             contractCreationProcessor,
             messageCallProcessor) ->
                new OptimismTransactionProcessor(
                    gasCalculator,
                    transactionValidator,
                    contractCreationProcessor,
                    messageCallProcessor,
                    true,
                    true,
                    evmConfiguration.evmStackSize(),
                    feeMarket,
                    CoinbaseFeePriceCalculator.eip1559(),
                    new CodeDelegationProcessor(
                        chainId, SIGNATURE_ALGORITHM.get().getHalfCurveOrder()),
                    genesisConfigOptions))
        // change to check for max blob gas per block for EIP-4844
        .transactionValidatorFactoryBuilder(
            (evm, gasLimitCalculator, feeMarket) ->
                new OptimismTransactionValidatorFactory(
                    evm.getGasCalculator(),
                    gasLimitCalculator,
                    feeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559,
                        TransactionType.OPTIMISM_DEPOSIT),
                    evm.getEvmVersion().getMaxInitcodeSize(),
                    genesisConfigOptions))
        .precompileContractRegistryBuilder(OptimismPrecompiledContractRegistries::fjord)
        .blockHeaderValidatorBuilder(MainnetBlockHeaderValidator::cancunBlockHeaderValidator)
        .blockHashProcessor(new CancunBlockHashProcessor())
        .name("Fjord");
  }

  static ProtocolSpecBuilder graniteDefinition(
      final Optional<BigInteger> chainId,
      final boolean enableRevertReason,
      final OptimismGenesisConfigOptions genesisConfigOptions,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final MetricsSystem metricsSystem) {

    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket cancunFeeMarket =
        new OpCancunFeeMarket(
            londonForkBlockNumber,
            genesisConfigOptions.getBaseFeePerGas(),
            genesisConfigOptions);

    return shanghaiDefinition(
        chainId,
        enableRevertReason,
        genesisConfigOptions,
        evmConfiguration,
        miningConfiguration,
        isParallelTxProcessingEnabled,
        metricsSystem)
        .feeMarket(cancunFeeMarket)
        // gas calculator for EIP-4844 blob gas
        .gasCalculator(FjordGasCalculator::new)
        // gas limit with EIP-4844 max blob gas per block
        .gasLimitCalculatorBuilder(
            feeMarket ->
                new CancunTargetingGasLimitCalculator(
                    londonForkBlockNumber, (BaseFeeMarket) feeMarket))
        // EVM changes to support EIP-1153: TSTORE and EIP-5656: MCOPY
        .evmBuilder(
            (gasCalculator, jdCacheConfig) ->
                OptimismEVMs.granite(gasCalculator, chainId.orElse(BigInteger.ZERO), evmConfiguration))
        // use Cancun fee market
        .transactionProcessorBuilder(
            (gasCalculator,
             feeMarket,
             transactionValidator,
             contractCreationProcessor,
             messageCallProcessor) ->
                new OptimismTransactionProcessor(
                    gasCalculator,
                    transactionValidator,
                    contractCreationProcessor,
                    messageCallProcessor,
                    true,
                    true,
                    evmConfiguration.evmStackSize(),
                    feeMarket,
                    CoinbaseFeePriceCalculator.eip1559(),
                    new CodeDelegationProcessor(
                        chainId, SIGNATURE_ALGORITHM.get().getHalfCurveOrder()),
                    genesisConfigOptions))
        // change to check for max blob gas per block for EIP-4844
        .transactionValidatorFactoryBuilder(
            (evm, gasLimitCalculator, feeMarket) ->
                new OptimismTransactionValidatorFactory(
                    evm.getGasCalculator(),
                    gasLimitCalculator,
                    feeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559,
                        TransactionType.OPTIMISM_DEPOSIT),
                    evm.getEvmVersion().getMaxInitcodeSize(),
                    genesisConfigOptions))
        .precompileContractRegistryBuilder(OptimismPrecompiledContractRegistries::granite)
        .blockHeaderValidatorBuilder(MainnetBlockHeaderValidator::cancunBlockHeaderValidator)
        .blockHashProcessor(new CancunBlockHashProcessor())
        .name("Granite");
  }

}
