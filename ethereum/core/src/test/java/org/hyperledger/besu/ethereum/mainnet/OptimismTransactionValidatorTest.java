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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.besu.ethereum.mainnet.TransactionValidationParams.processingBlockParams;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.crypto.KeyPair;
import org.hyperledger.besu.crypto.SignatureAlgorithm;
import org.hyperledger.besu.crypto.SignatureAlgorithmFactory;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.core.OptimismTransaction;
import org.hyperledger.besu.ethereum.core.OptimismTransactionTestFixture;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OptimismTransactionValidatorTest {

  private static final Supplier<SignatureAlgorithm> SIGNATURE_ALGORITHM =
      Suppliers.memoize(SignatureAlgorithmFactory::getInstance);
  protected static final KeyPair senderKeys = SIGNATURE_ALGORITHM.get().generateKeyPair();

  private static final TransactionValidationParams transactionValidationParams =
      processingBlockParams;

  @Mock protected GasCalculator gasCalculator;

  @Mock protected OptimismGenesisConfigOptions genesisConfigOptions;

  protected OptimismTransactionValidator createTransactionValidator(
      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final FeeMarket feeMarket,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId,
      final Set<TransactionType> acceptedTransactionTypes,
      final int maxInitcodeSize,
      final OptimismGenesisConfigOptions genesisConfigOptions) {
    return new OptimismTransactionValidator(
        gasCalculator,
        gasLimitCalculator,
        feeMarket,
        checkSignatureMalleability,
        chainId,
        acceptedTransactionTypes,
        maxInitcodeSize,
        genesisConfigOptions);
  }

  @Test
  public void shouldRejectSystemTxWhenRegolithActivation() {
    when(genesisConfigOptions.isRegolith(anyLong())).thenReturn(Boolean.TRUE);
    final OptimismTransactionTestFixture txCreator = new OptimismTransactionTestFixture();
    txCreator.isSystemTx(Boolean.TRUE);
    txCreator.sender(Address.extract(Hash.hash(senderKeys.getPublicKey().getEncodedBytes())));
    final OptimismTransaction transaction = txCreator.createTransaction(senderKeys);
    final OptimismTransactionValidator validator =
        createTransactionValidator(
            gasCalculator,
            GasLimitCalculator.constant(),
            FeeMarket.london(0L),
            false,
            Optional.of(BigInteger.ONE),
            Set.of(new TransactionType[] {TransactionType.OPTIMISM_DEPOSIT}),
            Integer.MAX_VALUE,
            genesisConfigOptions);

    assertThat(
            validator.validate(
                transaction, 0, Optional.empty(), Optional.empty(), transactionValidationParams))
        .isEqualTo(
            ValidationResult.invalid(
                // not support system tx when regolith hardfork is activation
                TransactionInvalidReason.PRIVATE_TRANSACTION_INVALID,
                String.format(
                    "system tx not supported: address = %s",
                    transaction.getSender().toHexString())));
  }

  @Test
  public void shouldValidateOtherTypeTx() {
    when(genesisConfigOptions.isRegolith(anyLong())).thenReturn(Boolean.TRUE);
    final OptimismTransactionTestFixture txCreator = new OptimismTransactionTestFixture();
    txCreator.sender(Address.extract(Hash.hash(senderKeys.getPublicKey().getEncodedBytes())));
    txCreator.type(TransactionType.EIP1559);
    final OptimismTransaction transaction = txCreator.createTransaction(senderKeys);
    final OptimismTransactionValidator validator =
        spy(
            createTransactionValidator(
                gasCalculator,
                GasLimitCalculator.constant(),
                FeeMarket.london(0L),
                false,
                Optional.of(BigInteger.ONE),
                Set.of(
                    new TransactionType[] {
                      TransactionType.FRONTIER,
                      TransactionType.EIP1559,
                      TransactionType.ACCESS_LIST,
                      TransactionType.BLOB
                    }),
                Integer.MAX_VALUE,
                genesisConfigOptions));
    doReturn(ValidationResult.valid()).when(validator).validate(any(), any(), any(), any());

    // Optimism transaction validator's validate method has 5 parameters.
    assertThat(
            validator.validate(
                transaction, 0, Optional.empty(), Optional.empty(), transactionValidationParams))
        .isEqualTo(ValidationResult.valid());

    // check super.validate is called
    verify(validator).validate(any(), any(), any(), any());
  }

  /**
   * After regolith is activation, a normal deposit transaction must have isSystemTx as false and
   * transactionType as OPTIMISM_DEPOSIT to be valid.
   */
  @Test
  public void shouldAcceptNormalDepositTx() {
    when(genesisConfigOptions.isRegolith(anyLong())).thenReturn(Boolean.TRUE);
    final OptimismTransactionTestFixture txCreator = new OptimismTransactionTestFixture();
    txCreator.sender(Address.extract(Hash.hash(senderKeys.getPublicKey().getEncodedBytes())));
    final OptimismTransaction transaction = txCreator.createTransaction(senderKeys);
    final OptimismTransactionValidator validator =
        createTransactionValidator(
            gasCalculator,
            GasLimitCalculator.constant(),
            FeeMarket.london(0L),
            false,
            Optional.of(BigInteger.ONE),
            Set.of(new TransactionType[] {TransactionType.OPTIMISM_DEPOSIT}),
            Integer.MAX_VALUE,
            genesisConfigOptions);

    assertThat(
            validator.validate(
                transaction, 0, Optional.empty(), Optional.empty(), transactionValidationParams))
        .isEqualTo(ValidationResult.valid());
  }

  @Test
  public void shouldAcceptSystemTxBeforeRegolithActivation() {
    when(genesisConfigOptions.isRegolith(anyLong())).thenReturn(Boolean.FALSE);
    final OptimismTransactionTestFixture txCreator = new OptimismTransactionTestFixture();
    txCreator.isSystemTx(Boolean.TRUE);
    txCreator.sender(Address.extract(Hash.hash(senderKeys.getPublicKey().getEncodedBytes())));
    final OptimismTransaction transaction = txCreator.createTransaction(senderKeys);
    final OptimismTransactionValidator validator =
        createTransactionValidator(
            gasCalculator,
            GasLimitCalculator.constant(),
            FeeMarket.london(0L),
            false,
            Optional.of(BigInteger.ONE),
            Set.of(new TransactionType[] {TransactionType.OPTIMISM_DEPOSIT}),
            Integer.MAX_VALUE,
            genesisConfigOptions);
    assertThat(
            validator.validate(
                transaction, 0, Optional.empty(), Optional.empty(), transactionValidationParams))
        .isEqualTo(ValidationResult.valid());
  }

  @Test
  public void shouldAcceptAllDepositTxForSender() {
    final OptimismTransactionTestFixture txCreator = new OptimismTransactionTestFixture();
    txCreator.sender(Address.extract(Hash.hash(senderKeys.getPublicKey().getEncodedBytes())));
    final OptimismTransaction transaction = txCreator.createTransaction(senderKeys);
    final OptimismTransactionValidator validator =
        createTransactionValidator(
            gasCalculator,
            GasLimitCalculator.constant(),
            FeeMarket.london(0L),
            false,
            Optional.of(BigInteger.ONE),
            Set.of(new TransactionType[] {TransactionType.OPTIMISM_DEPOSIT}),
            Integer.MAX_VALUE,
            genesisConfigOptions);

    assertThat(validator.validateForSender(transaction, null, transactionValidationParams))
        .isEqualTo(ValidationResult.valid());
  }
}
