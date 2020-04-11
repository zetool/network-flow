/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
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
package org.zetool.netflow.dynamic.earliestarrival;

import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import org.zetool.common.algorithm.AlgorithmProgressEvent;
import org.zetool.common.algorithm.AlgorithmListener;
import org.zetool.common.util.Formatter;
import org.zetool.common.util.units.TimeUnits;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.graph.DefaultDirectedGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import junit.framework.TestCase;
import org.zetool.common.algorithm.AbstractAlgorithmEvent;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EarliestArrivalFlowInstanceTest extends TestCase implements AlgorithmListener {

    @Test
    public void testInstance() throws FileNotFoundException, IOException, URISyntaxException {
        //File batchFile = new File( "./testinstanz/siouxfalls_500_10s.dat" );

        URL networkInput = EarliestArrivalFlowInstanceTest.class.getResource("/networks/padang_10p_10s_flow01.dat");
        File batchFile = new File(networkInput.toURI());
        
        BufferedReader read = new BufferedReader(new FileReader(batchFile));
        String s;

        int nodeCount = 0;
        int estimatedTimeHorizon = 0;

        ArrayList<Long> source_id = new ArrayList<>();
        ArrayList<Integer> source_sup = new ArrayList<>();

        ArrayList<Long> sink_id = new ArrayList<>();
        ArrayList<Integer> sink_sup = new ArrayList<>();

        ArrayList<Long> edge_start = new ArrayList<>();
        ArrayList<Long> edge_end = new ArrayList<>();
        ArrayList<Integer> edge_cap = new ArrayList<>();
        ArrayList<Integer> edge_len = new ArrayList<>();

        HashMap<Long, Integer> nodeMap = new HashMap<>();

        int currentNodeID = 0;

        while ((s = read.readLine()) != null) {
            if (s.charAt(0) == '%') // comment
            {
                continue;
            }
            if (s.charAt(0) == 'N') {
                nodeCount = Integer.parseInt(s.substring(2));
                continue;
            }
            if (s.charAt(0) == 'V') {
                // Old format. Skip vertices
                continue;
            }
            if (s.startsWith("TIME")) {
                estimatedTimeHorizon = Integer.parseInt(s.substring(5));
                continue;
            }
            if (s.startsWith("HOLDOVER")) {
                continue;
            }
            if (s.charAt(0) == 'S') {
                final String[] split = s.split(" ");
                final long nodeID = Long.parseLong(split[1]);
                System.out.println(" Key: " + nodeID + " value: " + currentNodeID);
                nodeMap.put(nodeID, currentNodeID++);
                final int nodeSupply = Integer.parseInt(split[2]);
                source_id.add(nodeID);
                source_sup.add(nodeSupply);
                continue;
            }
            if (s.charAt(0) == 'T') {
                final String[] split = s.split(" ");
                final long nodeID = Long.parseLong(split[1]);
                System.out.println(" Key: " + nodeID + " value: " + currentNodeID);
                nodeMap.put(nodeID, currentNodeID++);
                final int nodeSupply = Integer.parseInt(split[2]);
                sink_id.add(nodeID);
                sink_sup.add(-nodeSupply);
                continue;
            }
            if (s.charAt(0) == 'E') {
                final String[] split = s.split(" ");
                final long start = Long.parseLong(split[1]);
                final long end = Long.parseLong(split[2]);
                if (!nodeMap.containsKey(start)) {
                    nodeMap.put(start, currentNodeID++);
                }
                if (!nodeMap.containsKey(end)) {
                    nodeMap.put(end, currentNodeID++);
                }
                edge_start.add(start);
                edge_end.add(end);
                edge_cap.add(Integer.parseInt(split[3]));
                edge_len.add(Integer.parseInt(split[4]));
                continue;
            }
            throw new IllegalStateException("Unbekannte Zeile: " + s);
        }

        int edgeCount = edge_start.size();

        System.out.println("Knotenzahl: " + nodeCount);
        System.out.println("Hasheinträge: " + nodeMap.size());
        assertEquals(nodeCount, nodeMap.size());
        System.out.println("Kantenzahl: " + edge_start.size());
        System.out.println("Zeithorizont (geschätzt): " + estimatedTimeHorizon);
        System.out.println("Quellen: " + source_id.toString());
        System.out.println("mit Angeboten " + source_sup.toString());
        System.out.println("Senken: " + sink_id.toString());
        System.out.println("mit Bedarfen" + sink_sup.toString());
        System.out.println("Kanten von " + edge_start.toString());
        System.out.println("nach " + edge_end.toString());
        System.out.println("Mit Kapazitäten " + edge_cap.toString());
        System.out.println("Mit Länge " + edge_len.toString());
        System.out.println("Nächste Knotennummer: " + currentNodeID);

        System.out.println();
        System.out.println("Erzeuge Netzwerk...");

        DefaultDirectedGraph network = new DefaultDirectedGraph(nodeCount, edgeCount);
        for (int i = 0; i < edgeCount; ++i) {
            network.createAndSetEdge(network.getNode(nodeMap.get(edge_start.get(i))), network.getNode(nodeMap.get(edge_end.get(i))));
        }
        Long t;
        IdentifiableIntegerMapping<Edge> edgeCapacities = new IdentifiableIntegerMapping<>(network.edges());
        IdentifiableIntegerMapping<Node> nodeCapacities = new IdentifiableIntegerMapping<>(network.nodes());
        IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableIntegerMapping<>(network.edges());
        IdentifiableIntegerMapping<Node> currentAssignment = new IdentifiableIntegerMapping<>(network.nodes());

        for (int i = 0; i < edgeCount; ++i) {
            edgeCapacities.set(network.getEdge(network.getNode(nodeMap.get(edge_start.get(i))), network.getNode(nodeMap.get(edge_end.get(i)))), edge_cap.get(i));
            transitTimes.set(network.getEdge(network.getNode(nodeMap.get(edge_start.get(i))), network.getNode(nodeMap.get(edge_end.get(i)))), edge_len.get(i));
        }

        for (int i = 0; i < nodeCount; ++i) {
            nodeCapacities.set(network.getNode(i), 1000);
        }

        for (int i = 0; i < source_id.size(); ++i) {
            currentAssignment.set(network.getNode(nodeMap.get(source_id.get(i))), source_sup.get(i));
        }
        for (int i = 0; i < sink_id.size(); ++i) {
            currentAssignment.set(network.getNode(nodeMap.get(sink_id.get(i))), sink_sup.get(i));
        }

        System.out.println(network.toString());

        Node sink = network.getNode(nodeMap.get(sink_id.get(0)));
        ArrayList<Node> sources = new ArrayList<>();
        for (int i = 0; i < source_id.size(); ++i) {
            sources.add(network.getNode(nodeMap.get(source_id.get(i))));
        }

        EarliestArrivalFlowProblem eat = new EarliestArrivalFlowProblem(edgeCapacities, network, nodeCapacities, sink, sources, estimatedTimeHorizon, transitTimes, currentAssignment);

        //SuccessiveEarliestArrivalAugmentingPathAlgorithm algo = new SuccessiveEarliestArrivalAugmentingPathAlgorithm();
        SEAAPAlgorithm algo = new SEAAPAlgorithm();
        algo.setProblem(eat);
        algo.addAlgorithmListener(this);
        algo.run();
        System.err.println(algo.getRuntime());
        long start = System.nanoTime();
        PathBasedFlowOverTime df = algo.getSolution().getPathBased();
        String result = String.format("Sent %1$s of %2$s flow units in %3$s time units successfully.", algo.getSolution().getFlowAmount(), eat.getTotalSupplies(), algo.getSolution().getTimeHorizon());
        System.out.println(result);
        //AlgorithmTask.getInstance().publish( 100, result, "" );
        long end = System.nanoTime();
        System.out.println("Sending the flow units required " + algo.getRuntime() + ".");
//        System.out.println( df.toString() );
        System.err.println(Formatter.formatUnit(end - start, TimeUnits.NANO_SECOND));

    }

    @Override
    public void eventOccurred(AbstractAlgorithmEvent event) {
        if (event instanceof AlgorithmProgressEvent) {
            System.out.println(((AlgorithmProgressEvent) event).getProgress());
        } else {
            System.out.println(event.toString());
        }
    }
}
