package org.hyperledger.besu.config;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class OpGenesisConfigFile extends GenesisConfigFile {

    OpGenesisConfigFile(GenesisReader loader) {
        super(loader);
    }

    /**
     * Mainnet genesis config file.
     *
     * @return the genesis config file
     */
    public static GenesisConfigFile mainnet() {
        return fromSource(GenesisConfigFile.class.getResource("/optimism-mainnet.json"));
    }

    /**
     * Genesis file from URL.
     *
     * @param jsonSource the URL
     * @return the genesis config file
     */
    public static GenesisConfigFile fromSource(final URL jsonSource) {
        return fromConfig(JsonUtil.objectNodeFromURL(jsonSource, false));
    }

    /**
     * Genesis file from resource.
     *
     * @param resourceName the resource name
     * @return the genesis config file
     */
    public static GenesisConfigFile fromResource(final String resourceName) {
        return fromConfig(GenesisConfigFile.class.getResource(resourceName));
    }

    /**
     * From config genesis config file.
     *
     * @param jsonSource the json string
     * @return the genesis config file
     */
    public static OpGenesisConfigFile fromConfig(final URL jsonSource) {
        return new OpGenesisConfigFile(new GenesisReader.FromURL(jsonSource));
    }

    /**
     * From config genesis config file.
     *
     * @param json the json string
     * @return the genesis config file
     */
    public static GenesisConfigFile fromConfig(final String json) {
        return fromConfig(JsonUtil.objectNodeFromString(json, false));
    }

    /**
     * From config genesis config file.
     *
     * @param config the config
     * @return the genesis config file
     */
    public static GenesisConfigFile fromConfig(final ObjectNode config) {
        return new GenesisConfigFile(new GenesisReader.FromObjectNode(config));
    }

    /**
     * Gets parent hash.
     *
     * @return the parent hash
     */
    public String getStateHash() {
        return JsonUtil.getString(genesisRoot, "statehash", "");
    }

    /**
     * Gets config options, including any overrides.
     *
     * @return the config options
     */
    @Override
    public JsonOptimismConfigOptions getConfigOptions() {
        final ObjectNode config = loader.getConfig();
        // are there any overrides to apply?
        if (this.overrides == null) {
            return JsonOptimismConfigOptions.fromJsonObject(config);
        }
        // otherwise apply overrides
        Map<String, String> overridesRef = this.overrides;

        // if baseFeePerGas has been explicitly configured, pass it as an override:
        final var optBaseFee = getBaseFeePerGas();
        if (optBaseFee.isPresent()) {
            // streams and maps cannot handle null values.
            overridesRef = new HashMap<>(this.overrides);
            overridesRef.put("baseFeePerGas", optBaseFee.get().toShortHexString());
        }

        return JsonOptimismConfigOptions.fromJsonObjectWithOverrides(config, overridesRef);
    }
}
