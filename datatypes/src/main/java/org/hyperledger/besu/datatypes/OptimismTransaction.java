package org.hyperledger.besu.datatypes;

import java.util.Optional;

public interface OptimismTransaction extends Transaction {

  /**
   * Return the source hash for this transaction.
   *
   * @return optional source hash
   */
  Optional<Hash> getSourceHash();

  /**
   * Return the mint value for this transaction.
   *
   * @return optional mint value
   */
  Optional<Wei> getMint();

  /**
   * Return the is system transaction flag for this transaction.
   *
   * @return optional is system transaction flag
   */
  Optional<Boolean> getIsSystemTx();

}
