package org.hyperledger.besu.datatypes;

/** List of all Optimism hard forks. */
public enum OptimismHardforkId implements HardforkId {
  /** Bedrock fork. */
  BEDROCK(true, "Bedrock"),
  /** Regolith fork. */
  REGOLITH(true, "Regolith"),
  /** Bedrock fork. */
  CANYON(true, "Canyon"),
  /** DELTA fork. */
  DELTA(true, "Delta"),
  /** Ecotone fork. */
  ECOTONE(true, "Ecotone"),
  /** Fjord fork. */
  FJORD(true, "Fjord"),
  /** Granite fork. */
  GRANITE(true, "Granite"),
  /** Interop fork. */
  INTEROP(false, "Interop");

  final boolean finalized;
  final String description;

  OptimismHardforkId(final boolean finalized, final String description) {
    this.finalized = finalized;
    this.description = description;
  }

  @Override
  public boolean finalized() {
    return finalized;
  }

  @Override
  public String description() {
    return description;
  }
}
