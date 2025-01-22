package org.hyperledger.besu.evm.precompile;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AltBN128PairingGranitePrecompiledContract
    extends AltBN128PairingPrecompiledContract {
  private static final int MAX_INPUT_SIZE_GRANITE = 112687;

  AltBN128PairingGranitePrecompiledContract(
      final GasCalculator gasCalculator, final long pairingGasCost, final long baseGasCost) {
    super(gasCalculator, pairingGasCost, baseGasCost);
  }

  @Override
  public PrecompileContractResult computePrecompile(
      final Bytes input, @Nonnull final MessageFrame messageFrame) {
    if (input.size() > MAX_INPUT_SIZE_GRANITE) {
      return PrecompileContractResult.halt(
          null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
    }
    return super.computePrecompile(input, messageFrame);
  }
}
