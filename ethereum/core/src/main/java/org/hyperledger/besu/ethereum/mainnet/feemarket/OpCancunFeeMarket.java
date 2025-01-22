package org.hyperledger.besu.ethereum.mainnet.feemarket;

import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.BlobGas;
import org.hyperledger.besu.datatypes.Wei;
import java.util.Optional;

/**
 * The OpCancun fee market.
 */
public class OpCancunFeeMarket extends OpCanyonFeeMarket {

    private final CancunFeeMarket cancunFeeMarket;

    public OpCancunFeeMarket(final long londonForkBlockNumber, final Optional<Wei> baseFeePerGasOverride, final OptimismGenesisConfigOptions genesisConfigOptions) {
        super(londonForkBlockNumber, baseFeePerGasOverride, genesisConfigOptions);
        this.cancunFeeMarket = new CancunFeeMarket(londonForkBlockNumber, baseFeePerGasOverride);
    }

    @Override
    public boolean implementsDataFee() {
        return true;
    }

    @Override
    public Wei blobGasPricePerGas(final BlobGas excessBlobGas) {
      return this.cancunFeeMarket.blobGasPricePerGas(excessBlobGas);
    }
}
