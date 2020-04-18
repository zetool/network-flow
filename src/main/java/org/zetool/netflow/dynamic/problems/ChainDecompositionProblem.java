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
package org.zetool.netflow.dynamic.problems;

import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.netflow.ds.structure.FlowOverTimeEdgeSequence;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class ChainDecompositionProblem {

    private List<FlowOverTimeEdgeSequence> edgeSequences;
    private ImplicitTimeExpandedResidualNetwork network;

    public ChainDecompositionProblem(List<FlowOverTimeEdgeSequence> edgeSequences, ImplicitTimeExpandedResidualNetwork network) {
        this.edgeSequences = edgeSequences;
        this.network = network;
    }

    public List<FlowOverTimeEdgeSequence> getEdgeSequences() {
        return edgeSequences;
    }

    public void setEdgeSequences(List<FlowOverTimeEdgeSequence> edgeSequences) {
        this.edgeSequences = edgeSequences;
    }

    public ImplicitTimeExpandedResidualNetwork getNetwork() {
        return network;
    }

    public void setNetwork(ImplicitTimeExpandedResidualNetwork network) {
        this.network = network;
    }
}
