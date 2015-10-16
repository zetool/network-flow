/* copyright 2014-2015
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.zetool.netflow.io;

import org.zetool.graph.visualization.NodePositionMapping;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.zetool.container.collection.HidingSet;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableConstantMapping;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.math.geom.DiscretePoint;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestDatFileReaderWriter {

    @Test
    public void testWritefile() throws IOException {
        int timeHorizon = 3;
        Node a = new Node(0);
        Node b = new Node(1);
        Node c = new Node(2);
        Node d = new Node(3);
        Node e = new Node(4);
        HidingSet<Node> set = new HidingSet<>(Node.class, 5);
        set.add(a);
        set.add(b);
        set.add(c);
        set.add(d);
        set.add(e);
        List<Node> sources = new LinkedList<>();
        sources.add(a);
        sources.add(b);
        Node sink = d;
        IdentifiableCollection<Edge> edges = new HidingSet<>(Edge.class, 4);
        edges.add(new Edge(0, a, c));
        edges.add(new Edge(1, b, c));
        edges.add(new Edge(2, b, d));
        edges.add(new Edge(3, c, d));
        IdentifiableIntegerMapping<Node> currentAssignment = new IdentifiableConstantMapping<>(6);
        NodePositionMapping<DiscretePoint> nodePositions = new NodePositionMapping<>(2, 4);
        IdentifiableIntegerMapping<Edge> edgeCapacities = new IdentifiableConstantMapping<>(7);
        IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableConstantMapping<>(8);
        nodePositions.set(a, new DiscretePoint(0, 0));
        nodePositions.set(b, new DiscretePoint(0, 200));
        nodePositions.set(c, new DiscretePoint(200, 200));
        nodePositions.set(d, new DiscretePoint(400, 0));
        nodePositions.set(e, new DiscretePoint(-100, -100));

        // The expected output for the above example
        String[] output = {
            "N 5",
            "M 4",
            "TIME 3",
            "V 0 6 0 0",
            "V 1 6 0 200",
            "V 2 6 200 200",
            "V 3 6 400 0",
            "V 4 6 -100 -100",
            "E 0 2 7 8",
            "E 1 2 7 8",
            "E 1 3 7 8",
            "E 2 3 7 8"};

        StringWriter stringWriter = new StringWriter();
        DatFileReaderWriter.writeFile(stringWriter, timeHorizon, set, sources, sink, edges, edgeCapacities, transitTimes,
                currentAssignment, nodePositions);
        String expected = concatenate(output);
        assertEquals("Written output of example .dat is wrong.", expected, stringWriter.toString());
    }

    private String concatenate(String[] lines) {
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            sb.append(s).append('\n');
        }
        return sb.toString();
    }
}
