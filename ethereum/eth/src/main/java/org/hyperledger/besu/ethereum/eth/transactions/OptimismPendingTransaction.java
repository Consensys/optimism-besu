/*
 * Copyright contributors to Besu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.eth.transactions;

import org.hyperledger.besu.ethereum.core.Transaction;

public abstract class OptimismPendingTransaction extends PendingTransaction {

  static final int DEPOSIT_SIZE = 872;
  static final int SOURCE_HASH_SIZE = 32;
  static final int IS_SYSTEM_TX_SIZE = 1;
  static final int MINT_SIZE = 32;

  protected OptimismPendingTransaction(
      final Transaction transaction, final long addedAt, final long sequence, final byte score) {
    super(transaction, addedAt, sequence, score);
  }

  @Override
  protected int computeMemorySize() {
    return switch (transaction.getType()) {
          case FRONTIER -> computeFrontierMemorySize();
          case ACCESS_LIST -> computeAccessListMemorySize();
          case EIP1559 -> computeEIP1559MemorySize();
          case BLOB -> computeBlobMemorySize();
          case DELEGATE_CODE -> computeSetCodeMemorySize();
          case OPTIMISM_DEPOSIT -> computeOpDepositMemorySize();
        }
        + PENDING_TRANSACTION_MEMORY_SIZE;
  }

  /** correct memory size for OptimismDeposit transactions. */
  private int computeOpDepositMemorySize() {
    return DEPOSIT_SIZE
        + computePayloadMemorySize()
        + computeToMemorySize()
        + SOURCE_HASH_SIZE
        + IS_SYSTEM_TX_SIZE
        + MINT_SIZE;
  }
}
