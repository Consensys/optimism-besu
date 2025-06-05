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

import static org.hyperledger.besu.ethereum.mainnet.PrivateStateUtils.KEY_IS_PERSISTING_PRIVATE_STATE;
import static org.hyperledger.besu.ethereum.mainnet.PrivateStateUtils.KEY_PRIVATE_METADATA_UPDATER;
import static org.hyperledger.besu.ethereum.mainnet.PrivateStateUtils.KEY_TRANSACTION;
import static org.hyperledger.besu.ethereum.mainnet.PrivateStateUtils.KEY_TRANSACTION_HASH;

import org.hyperledger.besu.collections.trie.BytesTrieSet;
import org.hyperledger.besu.config.OptimismGenesisConfigOptions;
import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.OptimismTransaction;
import org.hyperledger.besu.ethereum.core.ProcessableBlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.feemarket.CoinbaseFeePriceCalculator;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.ethereum.mainnet.feemarket.L1CostCalculator;
import org.hyperledger.besu.ethereum.privacy.storage.PrivateMetadataUpdater;
import org.hyperledger.besu.ethereum.processing.TransactionProcessingResult;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.ethereum.trie.MerkleTrieException;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.account.MutableAccount;
import org.hyperledger.besu.evm.code.CodeInvalid;
import org.hyperledger.besu.evm.code.CodeV0;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.operation.BlockHashOperation;
import org.hyperledger.besu.evm.processor.AbstractMessageProcessor;
import org.hyperledger.besu.evm.tracing.OperationTracer;
import org.hyperledger.besu.evm.worldstate.EVMWorldUpdater;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Optimism transaction processor. */
public class OptimismTransactionProcessor extends MainnetTransactionProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(OptimismTransactionProcessor.class);

  private final OptimismGenesisConfigOptions genesisConfigOptions;

  private final L1CostCalculator l1CostCalculator;

  public OptimismTransactionProcessor(
      final GasCalculator gasCalculator,
      final TransactionValidatorFactory transactionValidatorFactory,
      final AbstractMessageProcessor contractCreationProcessor,
      final AbstractMessageProcessor messageCallProcessor,
      final boolean clearEmptyAccounts,
      final boolean warmCoinbase,
      final int maxStackSize,
      final FeeMarket feeMarket,
      final CoinbaseFeePriceCalculator coinbaseFeePriceCalculator,
      final OptimismGenesisConfigOptions genesisConfigOptions) {
    this(
        gasCalculator,
        transactionValidatorFactory,
        contractCreationProcessor,
        messageCallProcessor,
        clearEmptyAccounts,
        warmCoinbase,
        maxStackSize,
        feeMarket,
        coinbaseFeePriceCalculator,
        null,
        genesisConfigOptions);
  }

  public OptimismTransactionProcessor(
      final GasCalculator gasCalculator,
      final TransactionValidatorFactory transactionValidatorFactory,
      final AbstractMessageProcessor contractCreationProcessor,
      final AbstractMessageProcessor messageCallProcessor,
      final boolean clearEmptyAccounts,
      final boolean warmCoinbase,
      final int maxStackSize,
      final FeeMarket feeMarket,
      final CoinbaseFeePriceCalculator coinbaseFeePriceCalculator,
      final CodeDelegationProcessor maybeCodeDelegationProcessor,
      final OptimismGenesisConfigOptions genesisConfigOptions) {
    super(
        gasCalculator,
        transactionValidatorFactory,
        contractCreationProcessor,
        messageCallProcessor,
        clearEmptyAccounts,
        warmCoinbase,
        maxStackSize,
        feeMarket,
        coinbaseFeePriceCalculator,
        maybeCodeDelegationProcessor);
    this.genesisConfigOptions = genesisConfigOptions;
    this.l1CostCalculator = new L1CostCalculator(genesisConfigOptions);
  }

  @Override
  public TransactionProcessingResult processTransaction(
      final WorldUpdater worldState,
      final ProcessableBlockHeader blockHeader,
      final Transaction transaction,
      final Address miningBeneficiary,
      final OperationTracer operationTracer,
      final BlockHashOperation.BlockHashLookup blockHashLookup,
      final Boolean isPersistingPrivateState,
      final TransactionValidationParams transactionValidationParams,
      final PrivateMetadataUpdater privateMetadataUpdater,
      final Wei blobGasPrice) {
    final EVMWorldUpdater evmWorldUpdater = new EVMWorldUpdater(worldState, gasCalculator);
    var opTransaction = (OptimismTransaction) transaction;
    if (transaction.getType() == TransactionType.OPTIMISM_DEPOSIT
        && opTransaction.getMint().isPresent()) {
      WorldUpdater mintUpdater = worldState.updater();
      final MutableAccount sender = mintUpdater.getOrCreateSenderAccount(opTransaction.getSender());
      sender.incrementBalance(opTransaction.getMint().orElse(Wei.ZERO));
      mintUpdater.commit();
    }
    try {
      final var transactionValidator =
          (OptimismTransactionValidator) transactionValidatorFactory.get();
      LOG.trace("Starting execution of {}", opTransaction);
      ValidationResult<TransactionInvalidReason> validationResult =
          transactionValidator.validate(
              opTransaction,
              blockHeader.getTimestamp(),
              blockHeader.getBaseFee(),
              Optional.ofNullable(blobGasPrice),
              transactionValidationParams);
      // Make sure the transaction is intrinsically valid before trying to
      // compare against a sender account (because the transaction may not
      // be signed correctly to extract the sender).
      if (!validationResult.isValid()) {
        LOG.debug("Invalid transaction: {}", validationResult.getErrorMessage());
        return TransactionProcessingResult.invalid(validationResult);
      }

      final Address senderAddress = opTransaction.getSender();
      final MutableAccount sender = evmWorldUpdater.getOrCreateSenderAccount(senderAddress);

      validationResult =
          transactionValidator.validateForSender(
              opTransaction, sender, transactionValidationParams);
      if (!validationResult.isValid()) {
        LOG.debug("Invalid transaction: {}", validationResult.getErrorMessage());
        if (opTransaction.getType() == TransactionType.OPTIMISM_DEPOSIT) {
          return opDepositTxFailed(
              worldState, blockHeader, opTransaction, validationResult.getErrorMessage());
        }
        return TransactionProcessingResult.invalid(validationResult);
      }

      operationTracer.tracePrepareTransaction(evmWorldUpdater, opTransaction);

      final Set<Address> warmAddressList = new BytesTrieSet<>(Address.SIZE);

      final long previousNonce = sender.incrementNonce();
      LOG.trace(
          "Incremented sender {} nonce ({} -> {})",
          senderAddress,
          previousNonce,
          sender.getNonce());

      Wei transactionGasPrice = Wei.ZERO;
      if (opTransaction.getType() != TransactionType.OPTIMISM_DEPOSIT) {
        transactionGasPrice =
            feeMarket
                .getTransactionPriceCalculator()
                .price(opTransaction, blockHeader.getBaseFee());

        final long blobGas = gasCalculator.blobGasCost(opTransaction.getBlobCount());

        final Wei upfrontGasCost =
            opTransaction.getUpfrontGasCost(transactionGasPrice, blobGasPrice, blobGas);
        final Wei previousBalance = sender.decrementBalance(upfrontGasCost);
        LOG.trace(
            "Deducted sender {} upfront gas cost {} ({} -> {})",
            senderAddress,
            upfrontGasCost,
            previousBalance,
            sender.getBalance());
      }

      Wei l1CostGasFee = Wei.ZERO;
      if (!TransactionType.OPTIMISM_DEPOSIT.equals(opTransaction.getType())) {
        l1CostCalculator.l1Cost(blockHeader, opTransaction, worldState);
        sender.decrementBalance(l1CostGasFee);
      }

      long codeDelegationRefund = 0L;
      if (opTransaction.getCodeDelegationList().isPresent()) {
        if (maybeCodeDelegationProcessor.isEmpty()) {
          throw new RuntimeException("Code delegation processor is required for 7702 transactions");
        }

        final CodeDelegationResult codeDelegationResult =
            maybeCodeDelegationProcessor.get().process(evmWorldUpdater, opTransaction);
        warmAddressList.addAll(codeDelegationResult.accessedDelegatorAddresses());
        codeDelegationRefund =
            gasCalculator.calculateDelegateCodeGasRefund(
                (codeDelegationResult.alreadyExistingDelegators()));

        evmWorldUpdater.commit();
      }

      final List<AccessListEntry> accessListEntries =
          opTransaction.getAccessList().orElse(List.of());
      // we need to keep a separate hash set of addresses in case they specify no storage.
      // No-storage is a common pattern, especially for Externally Owned Accounts
      final Multimap<Address, Bytes32> storageList = HashMultimap.create();
      int accessListStorageCount = 0;
      for (final var entry : accessListEntries) {
        final Address address = entry.address();
        warmAddressList.add(address);
        final List<Bytes32> storageKeys = entry.storageKeys();
        storageList.putAll(address, storageKeys);
        accessListStorageCount += storageKeys.size();
      }
      if (warmCoinbase) {
        warmAddressList.add(miningBeneficiary);
      }

      final long intrinsicGas =
          gasCalculator.transactionIntrinsicGasCost(
              opTransaction.getPayload(), opTransaction.isContractCreation());
      final long accessListGas =
          gasCalculator.accessListGasCost(accessListEntries.size(), accessListStorageCount);
      final long codeDelegationGas =
          gasCalculator.delegateCodeGasCost(opTransaction.codeDelegationListSize());
      final long gasAvailable =
          opTransaction.getGasLimit() - intrinsicGas - accessListGas - codeDelegationGas;
      LOG.trace(
          "Gas available for execution {} = {} - {} - {} - {} (limit - intrinsic - accessList - codeDelegation)",
          gasAvailable,
          opTransaction.getGasLimit(),
          intrinsicGas,
          accessListGas,
          codeDelegationGas);

      final WorldUpdater worldUpdater = evmWorldUpdater.updater();
      final ImmutableMap.Builder<String, Object> contextVariablesBuilder =
          ImmutableMap.<String, Object>builder()
              .put(KEY_IS_PERSISTING_PRIVATE_STATE, isPersistingPrivateState)
              .put(KEY_TRANSACTION, opTransaction)
              .put(KEY_TRANSACTION_HASH, opTransaction.getHash());
      if (privateMetadataUpdater != null) {
        contextVariablesBuilder.put(KEY_PRIVATE_METADATA_UPDATER, privateMetadataUpdater);
      }

      operationTracer.traceStartTransaction(worldUpdater, opTransaction);

      final MessageFrame.Builder commonMessageFrameBuilder =
          MessageFrame.builder()
              .maxStackSize(maxStackSize)
              .worldUpdater(worldUpdater.updater())
              .initialGas(gasAvailable)
              .originator(senderAddress)
              .gasPrice(transactionGasPrice)
              .blobGasPrice(blobGasPrice)
              .sender(senderAddress)
              .value(opTransaction.getValue())
              .apparentValue(opTransaction.getValue())
              .blockValues(blockHeader)
              .completer(__ -> {})
              .miningBeneficiary(miningBeneficiary)
              .blockHashLookup(blockHashLookup)
              .contextVariables(contextVariablesBuilder.build())
              .accessListWarmStorage(storageList);

      if (opTransaction.getVersionedHashes().isPresent()) {
        commonMessageFrameBuilder.versionedHashes(
            Optional.of(opTransaction.getVersionedHashes().get().stream().toList()));
      } else {
        commonMessageFrameBuilder.versionedHashes(Optional.empty());
      }

      final MessageFrame initialFrame;
      if (opTransaction.isContractCreation()) {
        final Address contractAddress =
            Address.contractAddress(senderAddress, sender.getNonce() - 1L);

        final Bytes initCodeBytes = opTransaction.getPayload();
        Code code = contractCreationProcessor.getCodeFromEVMForCreation(initCodeBytes);
        initialFrame =
            commonMessageFrameBuilder
                .type(MessageFrame.Type.CONTRACT_CREATION)
                .address(contractAddress)
                .contract(contractAddress)
                .inputData(initCodeBytes.slice(code.getSize()))
                .code(code)
                .accessListWarmAddresses(warmAddressList)
                .build();
      } else {
        @SuppressWarnings("OptionalGetWithoutIsPresent") // isContractCall tests isPresent
        final Address to = opTransaction.getTo().get();
        final Optional<Account> maybeContract = Optional.ofNullable(evmWorldUpdater.get(to));

        if (maybeContract.isPresent() && maybeContract.get().hasDelegatedCode()) {
          warmAddressList.add(maybeContract.get().delegatedCodeAddress().get());
        }

        initialFrame =
            commonMessageFrameBuilder
                .type(MessageFrame.Type.MESSAGE_CALL)
                .address(to)
                .contract(to)
                .inputData(opTransaction.getPayload())
                .code(
                    maybeContract
                        .map(c -> messageCallProcessor.getCodeFromEVM(c.getCodeHash(), c.getCode()))
                        .orElse(CodeV0.EMPTY_CODE))
                .accessListWarmAddresses(warmAddressList)
                .build();
      }
      Deque<MessageFrame> messageFrameStack = initialFrame.getMessageFrameStack();

      if (initialFrame.getCode().isValid()) {
        while (!messageFrameStack.isEmpty()) {
          process(messageFrameStack.peekFirst(), operationTracer);
        }
      } else {
        initialFrame.setState(MessageFrame.State.EXCEPTIONAL_HALT);
        initialFrame.setExceptionalHaltReason(Optional.of(ExceptionalHaltReason.INVALID_CODE));
        validationResult =
            ValidationResult.invalid(
                TransactionInvalidReason.EOF_CODE_INVALID,
                ((CodeInvalid) initialFrame.getCode()).getInvalidReason());
      }

      if (initialFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
        worldUpdater.commit();
      } else {
        if (initialFrame.getExceptionalHaltReason().isPresent()
            && initialFrame.getCode().isValid()) {
          validationResult =
              ValidationResult.invalid(
                  TransactionInvalidReason.EXECUTION_HALTED,
                  initialFrame.getExceptionalHaltReason().get().toString());
        }
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Gas used by transaction: {}, by message call/contract creation: {}",
            opTransaction.getGasLimit() - initialFrame.getRemainingGas(),
            gasAvailable - initialFrame.getRemainingGas());
      }

      boolean isRegolith = this.genesisConfigOptions.isRegolith(blockHeader.getTimestamp());
      // if deposit: skip refunds, skip tipping coinbase
      // Regolith changes this behaviour to report the actual gasUsed instead of always reporting
      // all gas used.
      if (opTransaction.getType() == TransactionType.OPTIMISM_DEPOSIT && !isRegolith) {
        var gasUsed = opTransaction.getGasLimit();
        if (opTransaction.getIsSystemTx().orElse(false)) {
          gasUsed = 0L;
        }
        if (initialFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
          return TransactionProcessingResult.successful(
              initialFrame.getLogs(), gasUsed, 0L, initialFrame.getOutputData(), validationResult);
        } else {
          return TransactionProcessingResult.failed(
              gasUsed, 0L, validationResult, initialFrame.getRevertReason());
        }
      }

      // Refund the sender by what we should and pay the miner fee (note that we're doing them one
      // after the other so that if it is the same account somehow, we end up with the right result)
      final long selfDestructRefund =
          gasCalculator.getSelfDestructRefundAmount() * initialFrame.getSelfDestructs().size();
      final long baseRefundGas =
          initialFrame.getGasRefund() + selfDestructRefund + codeDelegationRefund;
      final long refundedGas =
          refunded(opTransaction, initialFrame.getRemainingGas(), baseRefundGas);
      final Wei refundedWei = transactionGasPrice.multiply(refundedGas);
      final Wei balancePriorToRefund = sender.getBalance();
      sender.incrementBalance(refundedWei);
      LOG.atTrace()
          .setMessage("refunded sender {}  {} wei ({} -> {})")
          .addArgument(senderAddress)
          .addArgument(refundedWei)
          .addArgument(balancePriorToRefund)
          .addArgument(sender.getBalance())
          .log();

      final long gasUsedByTransaction =
          opTransaction.getGasLimit() - initialFrame.getRemainingGas();
      final long usedGas = opTransaction.getGasLimit() - refundedGas;
      // depositTx has processed, will return
      if (opTransaction.getType() == TransactionType.OPTIMISM_DEPOSIT && isRegolith) {
        initialFrame.getSelfDestructs().forEach(evmWorldUpdater::deleteAccount);
        if (clearEmptyAccounts) {
          evmWorldUpdater.clearAccountsThatAreEmpty();
        }
        if (initialFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
          return TransactionProcessingResult.successful(
              initialFrame.getLogs(),
              gasUsedByTransaction,
              refundedGas,
              initialFrame.getOutputData(),
              validationResult);
        } else {
          return TransactionProcessingResult.failed(
              gasUsedByTransaction, refundedGas, validationResult, initialFrame.getRevertReason());
        }
      }
      // update the coinbase
      final CoinbaseFeePriceCalculator coinbaseCalculator;
      if (blockHeader.getBaseFee().isPresent()) {
        final Wei baseFee = blockHeader.getBaseFee().get();
        if (transactionGasPrice.compareTo(baseFee) < 0) {
          return TransactionProcessingResult.failed(
              gasUsedByTransaction,
              refundedGas,
              ValidationResult.invalid(
                  TransactionInvalidReason.TRANSACTION_PRICE_TOO_LOW,
                  "transaction price must be greater than base fee"),
              Optional.empty());
        }
        coinbaseCalculator = coinbaseFeePriceCalculator;
      } else {
        coinbaseCalculator = CoinbaseFeePriceCalculator.frontier();
      }

      final Wei coinbaseWeiDelta =
          coinbaseCalculator.price(usedGas, transactionGasPrice, blockHeader.getBaseFee());

      operationTracer.traceBeforeRewardTransaction(worldUpdater, opTransaction, coinbaseWeiDelta);
      if (!coinbaseWeiDelta.isZero() || !clearEmptyAccounts) {
        final var coinbase = evmWorldUpdater.getOrCreate(miningBeneficiary);
        coinbase.incrementBalance(coinbaseWeiDelta);

        if (genesisConfigOptions.isBedrockBlock(blockHeader.getNumber())
            && opTransaction.getType() != TransactionType.OPTIMISM_DEPOSIT) {
          var gasCost = blockHeader.getBaseFee().orElse(Wei.ZERO).multiply(usedGas);
          // todo check overflow?
          MutableAccount opBaseFeeRecipient =
              evmWorldUpdater.getOrCreate(
                  Address.fromHexString("0x4200000000000000000000000000000000000019"));
          opBaseFeeRecipient.incrementBalance(gasCost);
          if (!l1CostGasFee.equals(Wei.ZERO)) {
            MutableAccount opL1FeeRecipient =
                evmWorldUpdater.getOrCreate(
                    Address.fromHexString("0x420000000000000000000000000000000000001A"));
            opL1FeeRecipient.incrementBalance(l1CostGasFee);
          }
        }
      }

      operationTracer.traceEndTransaction(
          evmWorldUpdater.updater(),
          opTransaction,
          initialFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS,
          initialFrame.getOutputData(),
          initialFrame.getLogs(),
          gasUsedByTransaction,
          initialFrame.getSelfDestructs(),
          0L);

      initialFrame.getSelfDestructs().forEach(evmWorldUpdater::deleteAccount);

      if (clearEmptyAccounts) {
        evmWorldUpdater.clearAccountsThatAreEmpty();
      }

      if (initialFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
        return TransactionProcessingResult.successful(
            initialFrame.getLogs(),
            gasUsedByTransaction,
            refundedGas,
            initialFrame.getOutputData(),
            validationResult);
      } else {
        if (initialFrame.getExceptionalHaltReason().isPresent()) {
          LOG.debug(
              "Transaction {} processing halted: {}",
              opTransaction.getHash(),
              initialFrame.getExceptionalHaltReason().get());
        }
        if (initialFrame.getRevertReason().isPresent()) {
          LOG.debug(
              "Transaction {} reverted: {}",
              opTransaction.getHash(),
              initialFrame.getRevertReason().get());
        }
        return TransactionProcessingResult.failed(
            gasUsedByTransaction, refundedGas, validationResult, initialFrame.getRevertReason());
      }
    } catch (final MerkleTrieException re) {
      operationTracer.traceEndTransaction(
          evmWorldUpdater.updater(),
          opTransaction,
          false,
          Bytes.EMPTY,
          List.of(),
          0,
          EMPTY_ADDRESS_SET,
          0L);

      // need to throw to trigger the heal
      throw re;
    } catch (final RuntimeException re) {
      operationTracer.traceEndTransaction(
          evmWorldUpdater.updater(),
          opTransaction,
          false,
          Bytes.EMPTY,
          List.of(),
          0,
          EMPTY_ADDRESS_SET,
          0L);

      final var cause = re.getCause();
      if (cause != null && cause instanceof InterruptedException) {
        return TransactionProcessingResult.invalid(
            ValidationResult.invalid(TransactionInvalidReason.EXECUTION_INTERRUPTED));
      }

      LOG.error("Critical Exception Processing Transaction", re);
      return TransactionProcessingResult.invalid(
          ValidationResult.invalid(
              TransactionInvalidReason.INTERNAL_ERROR,
              "Internal Error in Besu - " + re + "\n" + printableStackTraceFromThrowable(re)));
    }
  }

  private TransactionProcessingResult opDepositTxFailed(
      final WorldUpdater worldState,
      final ProcessableBlockHeader blockHeader,
      final OptimismTransaction transaction,
      final String reason) {
    worldState.revert();
    worldState.getOrCreate(transaction.getSender()).incrementNonce();
    if (clearEmptyAccounts) {
      worldState.clearAccountsThatAreEmpty();
    }
    worldState.commit();
    var gasUsed = transaction.getGasLimit();
    if (transaction.getIsSystemTx().orElse(false)
        && genesisConfigOptions.isRegolith(blockHeader.getTimestamp())) {
      gasUsed = 0L;
    }
    final String msg = String.format("failed deposit: %s", reason);
    return TransactionProcessingResult.failed(
        gasUsed,
        0L,
        ValidationResult.valid(),
        Optional.of(Bytes.wrap(msg.getBytes(StandardCharsets.UTF_8))));
  }
}
