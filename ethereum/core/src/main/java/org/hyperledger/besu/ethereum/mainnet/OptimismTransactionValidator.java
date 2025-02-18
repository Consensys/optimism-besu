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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.core.OptimismTransaction;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

/** Optimism transaction validator. */
public class OptimismTransactionValidator extends MainnetTransactionValidator {

  private final OptimismGenesisConfigOptions genesisOptions;

  public OptimismTransactionValidator(
      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final FeeMarket feeMarket,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId,
      final Set<TransactionType> acceptedTransactionTypes,
      final int maxInitcodeSize,
      final OptimismGenesisConfigOptions genesisOptions) {
    super(
        gasCalculator,
        gasLimitCalculator,
        feeMarket,
        checkSignatureMalleability,
        chainId,
        acceptedTransactionTypes,
        maxInitcodeSize);
    this.genesisOptions = genesisOptions;
  }

  public ValidationResult<TransactionInvalidReason> validate(
      final OptimismTransaction transaction,
      final long blockTimestamp,
      final Optional<Wei> baseFee,
      final Optional<Wei> blobFee,
      final TransactionValidationParams transactionValidationParams) {

    if (transaction.getType().equals(TransactionType.OPTIMISM_DEPOSIT)) {
      var opTransaction = (org.hyperledger.besu.datatypes.OptimismTransaction) transaction;
      if (opTransaction.getIsSystemTx().orElse(false)) {
        if (this.genesisOptions.isRegolith(blockTimestamp)) {
          // todo define optimism invalide reason enum
          return ValidationResult.invalid(
              // not support system tx when regolith hardfork is activation
              TransactionInvalidReason.PRIVATE_TRANSACTION_INVALID,
              String.format(
                  "system tx not supported: address = %s", transaction.getSender().toHexString()));
        }
      }
      return ValidationResult.valid();
    }
    return validate(transaction, baseFee, blobFee, transactionValidationParams);
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validateForSender(
      final Transaction transaction,
      final Account sender,
      final TransactionValidationParams validationParams) {
    if (TransactionType.OPTIMISM_DEPOSIT.equals(transaction.getType())) {
      return ValidationResult.valid();
    }
    return super.validateForSender(transaction, sender, validationParams);
  }
}
