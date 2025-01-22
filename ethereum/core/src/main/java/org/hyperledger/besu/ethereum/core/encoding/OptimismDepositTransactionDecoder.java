package org.hyperledger.besu.ethereum.core.encoding;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.OptimismTransaction;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

import java.math.BigInteger;

public class OptimismDepositTransactionDecoder {

  public static OptimismTransaction decode(final RLPInput input) {
    input.enterList();
    final OptimismTransaction.Builder builder =
        OptimismTransaction.builder()
            .type(TransactionType.OPTIMISM_DEPOSIT)
            .sourceHash(Hash.wrap(input.readBytes32()))
            .sender(Address.wrap(input.readBytes()))
            .to(input.readBytes(v -> v.isEmpty() ? null : Address.wrap(v)))
            .mint(Wei.of(input.readUInt256Scalar()))
            .value(Wei.of(input.readUInt256Scalar()))
            .gasLimit(input.readLongScalar())
            .isSystemTx(input.readBigIntegerScalar().compareTo(BigInteger.ONE) == 0)
            .payload(input.readBytes());
    input.leaveList();
    return builder.build();
  }

}
