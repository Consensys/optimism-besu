/*
 * Copyright ConsenSys AG.
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
package org.hyperledger.besu.ethereum.core;

import static com.google.common.base.Preconditions.checkState;

import org.hyperledger.besu.crypto.KeyPair;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.BlobsWithCommitments;
import org.hyperledger.besu.datatypes.CodeDelegation;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.VersionedHash;
import org.hyperledger.besu.datatypes.Wei;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public class OptimismTransaction extends Transaction
    implements org.hyperledger.besu.datatypes.OptimismTransaction {

  private final Optional<Hash> sourceHash;

  private final Optional<Wei> mint;

  private final Optional<Boolean> isSystemTx;

  public static OptimismTransaction.Builder builder() {
    return new OptimismTransaction.Builder();
  }

  public OptimismTransaction(
      final boolean forCopy,
      final TransactionType transactionType,
      final long nonce,
      final Optional<Wei> gasPrice,
      final Optional<Wei> maxPriorityFeePerGas,
      final Optional<Wei> maxFeePerGas,
      final Optional<Wei> maxFeePerBlobGas,
      final long gasLimit,
      final Optional<Address> to,
      final Wei value,
      final SECPSignature signature,
      final Payload payload,
      final Optional<List<AccessListEntry>> maybeAccessList,
      final Address sender,
      final Optional<BigInteger> chainId,
      final Optional<List<VersionedHash>> versionedHashes,
      final Optional<BlobsWithCommitments> blobsWithCommitments,
      final Optional<List<CodeDelegation>> maybeCodeDelegationList,
      final Optional<Bytes> rawRlp,
      final Optional<Hash> sourceHash,
      final Optional<Wei> mint,
      final Optional<Boolean> isSystemTx) {
    super(
        forCopy,
        transactionType,
        nonce,
        gasPrice,
        maxPriorityFeePerGas,
        maxFeePerGas,
        maxFeePerBlobGas,
        gasLimit,
        to,
        value,
        signature,
        payload,
        maybeAccessList,
        sender,
        chainId,
        versionedHashes,
        blobsWithCommitments,
        maybeCodeDelegationList,
        rawRlp);
    this.sourceHash = sourceHash;
    this.mint = mint;
    this.isSystemTx = isSystemTx;
  }

  @Override
  public Optional<Hash> getSourceHash() {
    return sourceHash;
  }

  @Override
  public Optional<Wei> getMint() {
    return mint;
  }

  @Override
  public Optional<Boolean> getIsSystemTx() {
    return isSystemTx;
  }

  public static class Builder extends Transaction.Builder {

    protected Hash sourceHash;
    protected Wei mint;
    protected Boolean isSystemTx;

    @Override
    public OptimismTransaction.Builder copiedFrom(final Transaction toCopy) {
      super.copiedFrom(toCopy);
      if (toCopy instanceof OptimismTransaction opTransaction) {
        this.sourceHash = opTransaction.sourceHash.orElse(null);
        this.mint = opTransaction.mint.orElse(null);
        this.isSystemTx = opTransaction.isSystemTx.orElse(null);
      }
      return this;
    }

    @Override
    public Builder type(final TransactionType transactionType) {
      this.transactionType = transactionType;
      return this;
    }

    @Override
    public Builder sender(final Address sender) {
      this.sender = sender;
      return this;
    }

    @Override
    public Builder to(final Address to) {
      this.to = Optional.ofNullable(to);
      return this;
    }

    @Override
    public Builder value(final Wei value) {
      this.value = value;
      return this;
    }

    @Override
    public Builder gasLimit(final long gasLimit) {
      this.gasLimit = gasLimit;
      return this;
    }

    @Override
    public Builder payload(final Bytes payload) {
      this.payload = new Payload(payload);
      return this;
    }

    public Builder sourceHash(final Hash sourceHash) {
      this.sourceHash = sourceHash;
      return this;
    }

    public Builder mint(final Wei mint) {
      this.mint = mint;
      return this;
    }

    public Builder isSystemTx(final Boolean isSystemTx) {
      this.isSystemTx = isSystemTx;
      return this;
    }

    @Override
    public Builder guessType() {
      if (sourceHash != null || transactionType == TransactionType.OPTIMISM_DEPOSIT) {
        transactionType = TransactionType.OPTIMISM_DEPOSIT;
      }
      super.guessType();
      return this;
    }

    @Override
    public OptimismTransaction signAndBuild(final KeyPair keys) {
      if (transactionType == TransactionType.OPTIMISM_DEPOSIT) {
        return build();
      }
      checkState(
          signature == null, "The transaction signature has already been provided to this builder");
      signature(computeSignature(keys));
      sender(Address.extract(Hash.hash(keys.getPublicKey().getEncodedBytes())));
      return build();
    }

    @Override
    public OptimismTransaction build() {
      if (transactionType == null) guessType();

      return new OptimismTransaction(
          false,
          transactionType,
          nonce,
          Optional.ofNullable(gasPrice),
          Optional.ofNullable(maxPriorityFeePerGas),
          Optional.ofNullable(maxFeePerGas),
          Optional.ofNullable(maxFeePerBlobGas),
          gasLimit,
          to,
          value,
          signature,
          payload,
          accessList,
          sender,
          chainId,
          Optional.ofNullable(versionedHashes),
          Optional.ofNullable(blobsWithCommitments),
          codeDelegationAuthorizations,
          Optional.ofNullable(rawRlp),
          Optional.ofNullable(sourceHash),
          Optional.ofNullable(mint),
          Optional.ofNullable(isSystemTx));
    }
  }
}
