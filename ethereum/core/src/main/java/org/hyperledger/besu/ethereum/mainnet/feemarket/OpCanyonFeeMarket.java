package org.hyperledger.besu.ethereum.mainnet.feemarket;

import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.Wei;

import java.util.Optional;

public class OpCanyonFeeMarket extends OpLondonFeeMarket{

  public OpCanyonFeeMarket(final long londonForkBlockNumber, final Optional<Wei> baseFeePerGasOverride, final OptimismGenesisConfigOptions genesisConfigOptions) {
    super(londonForkBlockNumber, baseFeePerGasOverride, genesisConfigOptions);
  }

  @Override
  public long getBasefeeMaxChangeDenominator() {
    return this.genesisConfigOptions.getOptimismConfigOptions().getEIP1559DenominatorCanyon().orElseThrow();
  }
}
