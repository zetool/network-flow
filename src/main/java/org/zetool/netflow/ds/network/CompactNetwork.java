/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zetool.netflow.ds.network;

import org.zetool.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.graph.Node;

/**
 *
 * @author Martin
 */
public class CompactNetwork {

    private int[] nodeStartIndices;
    private int[] edgeEndIDs;
    private int[] edgeCapacities;

    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdentifiableCollection<Edge> edges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdentifiableCollection<Node> nodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int numberOfEdges() {
        return edgeEndIDs.length;
    }

    public int numberOfNodes() {
        return nodeStartIndices.length;
    }

    public int outdegree(int node) {
        return ((node+1 == nodeStartIndices.length)? edgeEndIDs.length : nodeStartIndices[node+1]) - nodeStartIndices[node];
    }
}
