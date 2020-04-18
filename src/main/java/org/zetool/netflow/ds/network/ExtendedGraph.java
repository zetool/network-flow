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

import org.zetool.container.collection.ShiftedArraySet;
import org.zetool.container.collection.CombinedCollection;
import org.zetool.container.collection.ArraySet;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.collection.ListSequence;
import org.zetool.container.util.IteratorIterator;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import java.util.HashMap;
import java.util.Iterator;
import org.zetool.graph.MutableDirectedGraph;

/**
 * A special {@link MutableDirectedGraph} extending an static network, i.e. a network that cannot be changed, such that
 * it can be modified by adding some sources and edges. Slow implementation!
 *
 * @author Jan-Philipp Kappmeier
 */
public class ExtendedGraph implements MutableDirectedGraph {

    private final int originalEdgeCount;
    private final int originalNodeCount;
    private final DirectedGraph graph;
    private final ArraySet<Node> newNodes;
    private final ArraySet<Edge> newEdges;

    /**
     * Caches the edges incident to a node for all nodes in the graph. * Must not be null.
     */
    protected HashMap<Node, ListSequence<Edge>> incidentEdges;
    /**
     * Caches the edges ending at a node for all nodes in the graph. Must not be null.
     */
    protected HashMap<Node, ListSequence<Edge>> incomingEdges;
    /**
     * Caches the edges starting at a node for all nodes in the graph. Must not be null.
     */
    protected HashMap<Node, ListSequence<Edge>> outgoingEdges;
    /**
     * Caches the number of edges incident to a node for all nodes in the graph. Must not be null.
     */
    protected HashMap<Node, Integer> degree;
    /**
     * Caches the number of edges ending at a node for all nodes in the graph. Must not be null.
     */
    protected HashMap<Node, Integer> indegree;
    /**
     * Caches the number of edges starting at a node for all nodes in the graph. Must not be null.
     */
    protected HashMap<Node, Integer> outdegree;

    public ExtendedGraph(DirectedGraph graph, int newNodes, int newEdges) {
        originalNodeCount = graph.nodeCount();
        originalEdgeCount = graph.edgeCount();
        this.graph = graph;
        this.newNodes = new ShiftedArraySet<>(Node.class, newNodes, originalNodeCount);
        for (int i = 0; i < newNodes; ++i) {
            this.newNodes.add(new Node(originalNodeCount + i));
        }
        this.newEdges = new ShiftedArraySet<>(Edge.class, newEdges, originalEdgeCount);

        //outgoingEdges = new IdentifiableObjectMapping<>( (originalNodeCount + newNodes) );
        outgoingEdges = new HashMap<>();
        incomingEdges = new HashMap<>();
        for (Node node : this) {
            outgoingEdges.put(node, new ListSequence<>());
            incomingEdges.put(node, new ListSequence<>());
        }
    }

    public Node getFirstNewNode() {
        return newNodes.get(0);
    }

    public int getFirstNewEdgeIndex() {
        return originalEdgeCount;
    }

    @Override
    public IdentifiableCollection<Edge> incomingEdges(Node node) {
        if (node.id() < originalNodeCount) {
            return graph.incomingEdges(node);
        } else {
            return incomingEdges.get(node);
        }
    }

    @Override
    public IdentifiableCollection<Edge> outgoingEdges(Node node) {
        if (isOriginalNode(node.id())) {
            return new CombinedCollection<>(graph.outgoingEdges(node), outgoingEdges.get(node));
        } else {
            return outgoingEdges.get(node);
        }
    }

    @Override
    public IdentifiableCollection<Node> predecessorNodes(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IdentifiableCollection<Node> successorNodes(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int inDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int outDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IdentifiableCollection<Edge> edges() {
        return new CombinedCollection<>(graph.edges(), this.newEdges);
    }

    @Override
    public IdentifiableCollection<Node> nodes() {
        
        return new CombinedCollection<>(graph.nodes(), this.newNodes);
    }

    @Override
    public int edgeCount() {
        return graph.edgeCount() + newEdges.size();
    }

    @Override
    public int nodeCount() {
        return graph.nodeCount() + newNodes.size();
    }

    @Override
    public IdentifiableCollection<Edge> incidentEdges(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IdentifiableCollection<Node> adjacentNodes(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int degree(Node node) {
        int degree = 0;
        if (isOriginalNode(node.id())) {
            degree += graph.degree(node);
        }
        if (this.degree.containsKey(node)) {
            degree += this.degree.get(node);
        }
        return degree;
    }

    @Override
    public boolean contains(Edge edge) {
        return graph.contains(edge) || newEdges.contains(edge);
    }

    @Override
    public boolean contains(Node node) {
        return graph.contains(node) || newNodes.contains(node);
    }

    @Override
    public Edge getEdge(int id) {
        if (isOriginalEdge(id)) {
            return graph.getEdge(id);
        } else {
            return newEdges.get(id - originalEdgeCount);
        }
    }

    @Override
    public Edge getEdge(Node start, Node end) {
        Edge e = null;
        if (isOriginalNode(start.id()) && isOriginalNode(end.id())) {
            e = graph.getEdge(start, end);
        }
        if (e != null) {
            return e;
        }
        if (outgoingEdges.containsKey(start)) {
            for (Edge edge : outgoingEdges.get(start)) {
                if (edge.end().equals(end)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public IdentifiableCollection<Edge> getEdges(Node start, Node end) {
        IdentifiableCollection<Edge> result = new ListSequence<>();
        if (isOriginalNode(start.id()) && isOriginalNode(end.id())) {
            result = graph.getEdges(start, end);
        }
        if (outgoingEdges.containsKey(start)) {
            for (Edge edge : outgoingEdges.get(start)) {
                if (edge.end().equals(end)) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    @Override
    public Node getNode(int id) {
        if (isOriginalNode(id)) {
            return graph.getNode(id);
        } else {
            return newNodes.get(id - originalNodeCount);
        }
    }

    private boolean isOriginalNode(int id) {
        return id < originalNodeCount;
    }

    private boolean isOriginalEdge(int id) {
        return id < originalEdgeCount;
    }

    @Override
    public Iterator<Node> iterator() {
        return new IteratorIterator<>(graph.iterator(), newNodes.iterator());
    }

    @Override
    public Edge createAndSetEdge(Node start, Node end) {
        if (newEdges.size() >= newEdges.getCapacity()) {
            throw new IllegalArgumentException("Cannot add more edges to extended graph!");
        }
        Edge edge = new Edge(originalEdgeCount + newEdges.size(), start, end);
        newEdges.add(edge);
        outgoingEdges.get(start).add(edge);
        incomingEdges.get(end).add(edge);
        return edge;
    }

    @Override
    public String toString() {
        return DirectedGraph.stringRepresentation(this);
    }

    @Override
    public void setEdges(Iterable<Edge> edges) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setNodes(Iterable<Node> nodes) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getNodeCapacity() {
        return originalNodeCount + newNodes.getCapacity();
    }

    @Override
    public void setNodeCapacity(int newCapacity) {
        int currentCapacity = getNodeCapacity();

        int newNewNodes = newCapacity - currentCapacity;

        if (newCapacity < currentCapacity) {
            for (int i = Math.abs(newNewNodes); i >= newNodes.getCapacity() + newNewNodes; --i) {
                Node deletedNode = newNodes.get(i);
                for (Edge e: outgoingEdges(deletedNode)) {
                    newEdges.remove(e);
                }
                outgoingEdges.remove(deletedNode);
                for (Edge e: incomingEdges(deletedNode)) {
                    newEdges.remove(e);
                }
                incomingEdges.remove(deletedNode);
                newNodes.remove(deletedNode);
            }
        }
        if (newCapacity == currentCapacity) {
            throw new IllegalArgumentException("Return without modification");
        }
        newNodes.setCapacity(newNodes.getCapacity() + newNewNodes);
        for (int i = 0; i < newNewNodes; ++i) {
            Node newNode = new Node(currentCapacity + i);
            this.newNodes.add(newNode);
            outgoingEdges.put(newNode, new ListSequence<>());
            incomingEdges.put(newNode, new ListSequence<>());
        }
    }

    @Override
    public int getEdgeCapacity() {
        return originalEdgeCount + newEdges.getCapacity();
    }

    @Override
    public void setEdgeCapacity(int newCapacity) {
        int currentCapacity = getEdgeCapacity();
        if (newCapacity < currentCapacity) {
            throw new IllegalStateException("Extended network cannot decrease capacity");
        }
        int newNewEdges = newCapacity - currentCapacity;
        newEdges.setCapacity(newEdges.getCapacity() + newNewEdges);
    }
}
