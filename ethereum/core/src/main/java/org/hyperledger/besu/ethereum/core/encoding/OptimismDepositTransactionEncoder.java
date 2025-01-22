package org.hyperledger.besu.ethereum.core.encoding;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.OptimismTransaction;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.rlp.RLPOutput;

import java.math.BigInteger;

public class OptimismDepositTransactionEncoder {

  public static void encode(final Transaction transaction, final RLPOutput out) {
    if (transaction instanceof OptimismTransaction opTransaction) {
      out.startList();
      out.writeBytes(opTransaction.getSourceHash().orElseThrow());
      out.writeBytes(opTransaction.getSender());
      out.writeBytes(opTransaction.getTo().map(Bytes::copy).orElse(Bytes.EMPTY));
      out.writeUInt256Scalar(opTransaction.getMint().orElse(Wei.ZERO));
      out.writeUInt256Scalar(opTransaction.getValue() != null ? opTransaction.getValue() : Wei.ZERO);
      out.writeLongScalar(opTransaction.getGasLimit());
      out.writeBigIntegerScalar(
          opTransaction.getIsSystemTx().map(b -> b ? BigInteger.ONE : BigInteger.ZERO).orElseThrow());
      out.writeBytes(opTransaction.getPayload() != null ? opTransaction.getPayload() : Bytes.EMPTY);
      out.endList();
    }
  }

}
