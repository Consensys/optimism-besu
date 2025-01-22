package org.hyperledger.besu.ethereum.mainnet.feemarket;

import org.apache.tuweni.units.bigints.UInt256s;
import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.Wei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalLong;

public class OpLondonFeeMarket extends LondonFeeMarket {

    protected final OptimismGenesisConfigOptions genesisConfigOptions;

    public OpLondonFeeMarket(final long londonForkBlockNumber, final Optional<Wei> baseFeePerGasOverride, final OptimismGenesisConfigOptions genesisConfigOptions) {
        super(londonForkBlockNumber, baseFeePerGasOverride);
        this.genesisConfigOptions = genesisConfigOptions;
    }

    @Override
    public long getBasefeeMaxChangeDenominator() {
      return this.genesisConfigOptions.getOptimismConfigOptions().getEIP1559Denominator().orElseThrow();
    }

    @Override
    public long getSlackCoefficient() {
        return genesisConfigOptions.getOptimismConfigOptions().getEIP1559Elasticity().orElseThrow();
    }
}
