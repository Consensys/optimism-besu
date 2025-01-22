package org.hyperledger.besu.ethereum.chain;

import org.hyperledger.besu.config.GenesisConfigFile;
import org.hyperledger.besu.config.OpGenesisConfigFile;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.worldstate.DataStorageConfiguration;

public class OptimismGenesisState extends GenesisState {

  OptimismGenesisState(final Block block, final GenesisConfigFile genesisConfigFile) {
      super(block, genesisConfigFile);
  }

  /**
   * Construct a {@link GenesisState} from a JSON object for Optimism.
   *
   * @param dataStorageConfiguration A {@link DataStorageConfiguration} describing the storage
   *     configuration
   * @param genesisConfigFile A {@link GenesisConfigFile} describing the genesis block.
   * @param protocolSchedule A protocol Schedule associated with
   * @return A new {@link GenesisState}.
   */
  public static OptimismGenesisState fromConfig(
      final DataStorageConfiguration dataStorageConfiguration,
      final OpGenesisConfigFile genesisConfigFile,
      final ProtocolSchedule protocolSchedule) {
    // for optimism mainnet, it will modify genesis state root.
    final Hash genesisStateRoot;
    if (!genesisConfigFile.getStateHash().isEmpty() && genesisConfigFile.streamAllocations().findAny().isEmpty()) {
      genesisStateRoot = Hash.fromHexStringLenient(genesisConfigFile.getStateHash());
    } else {
      // other optimism network, it will calculate genesis state root.
      genesisStateRoot = GenesisState.calculateGenesisStateRoot(dataStorageConfiguration, genesisConfigFile);
    }
    final Block block =
        new Block(
            GenesisState.buildHeader(genesisConfigFile, genesisStateRoot, protocolSchedule),
            GenesisState.buildBody(genesisConfigFile));
    return new OptimismGenesisState(block, genesisConfigFile);
  }

}
