package org.hyperledger.besu.ethereum.mainnet;

import com.google.common.base.Suppliers;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

public class OptimismTransactionValidatorFactory extends TransactionValidatorFactory {

  public OptimismTransactionValidatorFactory(
      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final FeeMarket feeMarket,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId,
      final Set<TransactionType> acceptedTransactionTypes,
      final int maxInitcodeSize,
      final OptimismGenesisConfigOptions genesisOptions) {
    super();
    this.transactionValidatorSupplier =
        Suppliers.memoize(
            () ->
                new OptimismTransactionValidator(
                    gasCalculator,
                    gasLimitCalculator,
                    feeMarket,
                    checkSignatureMalleability,
                    chainId,
                    acceptedTransactionTypes,
                    maxInitcodeSize,
                    genesisOptions));
  }
}
