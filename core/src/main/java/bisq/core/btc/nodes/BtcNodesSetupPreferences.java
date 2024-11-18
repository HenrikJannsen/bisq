/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.btc.nodes;

import bisq.core.btc.nodes.BtcNodes.BtcNode;
import bisq.core.user.Preferences;

import bisq.common.config.Config;
import bisq.common.util.Utilities;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BtcNodesSetupPreferences {
    private static final Logger log = LoggerFactory.getLogger(BtcNodesSetupPreferences.class);

    private final Preferences preferences;
    private final int numConnectionsForBtc;
    private final Config config;

    public BtcNodesSetupPreferences(Preferences preferences,
                                    int numConnectionsForBtc,
                                    Config config) {
        this.preferences = preferences;
        this.numConnectionsForBtc = numConnectionsForBtc;
        this.config = config;
    }

    public List<BtcNode> selectPreferredNodes(BtcNodes btcNodes) {
        List<BtcNode> result;

        BtcNodes.BitcoinNodesOption nodesOption = BtcNodes.BitcoinNodesOption.values()[preferences.getBitcoinNodesOptionOrdinal()];
        switch (nodesOption) {
            case CUSTOM:
                String bitcoinNodes = preferences.getBitcoinNodes();
                Set<String> distinctNodes = Utilities.commaSeparatedListToSet(bitcoinNodes, false);
                result = BtcNodes.toBtcNodesList(distinctNodes);
                if (result.isEmpty()) {
                    log.warn("Custom btcNodes is set but no valid btcNodes are provided. " +
                            "We fall back to provided btcNodes option.");
                    preferences.setBitcoinNodesOptionOrdinal(BtcNodes.BitcoinNodesOption.PROVIDED.ordinal());
                    result = btcNodes.getProvidedBtcNodes();
                }
                break;
            case PUBLIC:
                result = Collections.emptyList();
                break;
            case PROVIDED:
            default:
                List<BtcNode> hardcodedBtcNodes = btcNodes.getProvidedBtcNodes();
                result = FederatedBtcNodeProvider.getNodes(hardcodedBtcNodes, config);
                break;
        }

        return result;
    }

    public boolean isUseCustomNodes() {
        return BtcNodes.BitcoinNodesOption.CUSTOM.ordinal() == preferences.getBitcoinNodesOptionOrdinal();
    }

    public int calculateMinBroadcastConnections(List<BtcNode> nodes) {
        BtcNodes.BitcoinNodesOption nodesOption = BtcNodes.BitcoinNodesOption.values()[preferences.getBitcoinNodesOptionOrdinal()];
        int result;
        switch (nodesOption) {
            case CUSTOM:
                // We have set the nodes already above
                result = (int) Math.ceil(nodes.size() * 0.5);
                // If Tor is set we usually only use onion nodes,
                // but if user provides mixed clear net and onion nodes we want to use both
                break;
            case PUBLIC:
                // We keep the empty nodes
                result = (int) Math.floor(numConnectionsForBtc * 0.8);
                break;
            case PROVIDED:
            default:
                // We require only 4 nodes instead of 7 (for 9 max connections) because our provided nodes
                // are more reliable than random public nodes.
                result = 4;
                break;
        }
        return result;
    }
}
