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
package org.hyperledger.besu.ethereum.core.encoding;

import static com.google.common.base.Preconditions.checkNotNull;

import org.hyperledger.besu.datatypes.TransactionType;

import com.google.common.collect.ImmutableMap;

/** The transaction encoder provider for Mainnet. */
public class MainnetTransactionEncoderDecoderProvider
    implements TransactionEncoder.EncoderProvider, TransactionDecoder.DecoderProvider {

  private static final ImmutableMap<TransactionType, TransactionEncoder.Encoder>
      TYPED_TRANSACTION_ENCODERS =
          ImmutableMap.of(
              TransactionType.ACCESS_LIST,
              AccessListTransactionEncoder::encode,
              TransactionType.EIP1559,
              EIP1559TransactionEncoder::encode,
              TransactionType.BLOB,
              BlobTransactionEncoder::encode,
              TransactionType.DELEGATE_CODE,
              CodeDelegationTransactionEncoder::encode);

  private static final ImmutableMap<TransactionType, TransactionDecoder.Decoder>
      TYPED_TRANSACTION_DECODERS =
          ImmutableMap.of(
              TransactionType.ACCESS_LIST,
              AccessListTransactionDecoder::decode,
              TransactionType.EIP1559,
              EIP1559TransactionDecoder::decode,
              TransactionType.BLOB,
              BlobTransactionDecoder::decode,
              TransactionType.DELEGATE_CODE,
              CodeDelegationTransactionDecoder::decode);

  private static final ImmutableMap<TransactionType, TransactionEncoder.Encoder>
      POOLED_TRANSACTION_ENCODERS =
          ImmutableMap.of(TransactionType.BLOB, BlobPooledTransactionEncoder::encode);

  private static final ImmutableMap<TransactionType, TransactionDecoder.Decoder>
      POOLED_TRANSACTION_DECODERS =
          ImmutableMap.of(TransactionType.BLOB, BlobPooledTransactionDecoder::decode);

  @Override
  public TransactionEncoder.Encoder getEncoder(
      final TransactionType transactionType, final EncodingContext encodingContext) {

    if (encodingContext.equals(EncodingContext.POOLED_TRANSACTION)) {
      if (POOLED_TRANSACTION_ENCODERS.containsKey(transactionType)) {
        return POOLED_TRANSACTION_ENCODERS.get(transactionType);
      }
    }
    return checkNotNull(
        TYPED_TRANSACTION_ENCODERS.get(transactionType),
        "Developer Error. A supported transaction type %s has no associated encoding logic",
        transactionType);
  }

  @Override
  public TransactionDecoder.Decoder getDecoder(
      final TransactionType transactionType, final EncodingContext encodingContext) {
    if (encodingContext.equals(EncodingContext.POOLED_TRANSACTION)) {
      if (POOLED_TRANSACTION_DECODERS.containsKey(transactionType)) {
        return POOLED_TRANSACTION_DECODERS.get(transactionType);
      }
    }
    return checkNotNull(
        TYPED_TRANSACTION_DECODERS.get(transactionType),
        "Developer Error. A supported transaction type %s has no associated decoding logic",
        transactionType);
  }
}
