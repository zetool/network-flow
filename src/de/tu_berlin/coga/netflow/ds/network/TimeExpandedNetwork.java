/* zet evacuation tool copyright (c) 2007-10 zet evacuation team
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

package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.container.collection.ListSequence;
import de.tu_berlin.coga.netflow.ds.network.Network;
import de.tu_berlin.coga.netflow.ds.network.NetworkInterface;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.structure.DynamicPath;
import ds.graph.GraphLocalization;
import java.util.LinkedList;

import java.util.List;

/**
 * The class {@code TimeExpandedNetwork} provides flow algorithms with the
 * methods to create and work with time expanded networks in order to transform
 * dynamic flow problems into static ones.
 * The construction of the time expanded network depends on whether the
 * base network has multiple sources / sinks and whether those
 * shall be connected with a super source / sink.
 *
 * The original network may not include loops.
 */
public class TimeExpandedNetwork extends Network {

    /**
     * The underlying base network.
     */
    protected NetworkInterface network = null;

    /**
     * The capacities belonging to the time expanded network.
     */
    protected IdentifiableIntegerMapping<Edge> capacities = null;

    /**
     * The transit times belonging to the time expanded network.
     */
    protected IdentifiableIntegerMapping<Edge> costs = null;

    /**
     * The supply function belonging to the time expanded network.
     */
    protected IdentifiableIntegerMapping<Node> supplies = null;

    /**
     * The sink of the time expanded network
     * if it has exactly one sink.
     */
    protected Node sink = null;

    /**
     * The sinks of the time expanded network,
     * can also contain only one sink.
     */
    protected ListSequence<Node> sinks = null;

    /**
     * The sinks of the original network.
     */
    protected ListSequence<Node> originalSinks = null;

    /**
     * The source of the time expanded network
     * if it has exactly one source.
     */
    protected Node source = null;

    /**
     * The sources of the time expanded network,
     * can also contain only one source.
     */
    protected ListSequence<Node> sources = null;

    /**
     * The sources of the original network.
     */
    protected ListSequence<Node> originalSources = null;

    /**
     * A grid to store references to all nodes of
     * the time expanded network.
     * The first dimension is addressed with the
     * IDs of the nodes in the base network,
     * the second dimension represents the
     * time layers of the time expanded network.
     */
    private Node[][] grid = null;

    /**
     * A mapping storing for each node the ID of the
     * node that it is a copy of.
     */
    IdentifiableIntegerMapping<Node> originalID = null;

    /**
     * The time horizon of the time expanded network.
     */
    protected int timeHorizon = -1;

    /**
     * A flag used during the creation of time expanded networks. If set to true
     * waiting is possible in every node. If set to false it is only possible
     * for source and sink.
     */
    protected boolean allowStorageInNodes = false;

    /**
     * The number of non-storage edges.
     */
    protected transient int realEdges = -1;

    /**
     * A constructor for clone and overriding classes.
     */
    protected TimeExpandedNetwork(int initialNodeCapacity, int initialEdgeCapacity) {
        super(initialNodeCapacity, initialEdgeCapacity);
    }

    /**
	 * Private method to extend the base network into time by constructing time
	 * layers.
	 * The  base network is included in the expanded version
	 * as the bottom layer.
	 * @param network The base network.
	 * @param capacities The capacity function of the base network.
	 * @param transitTimes The transit time function of the base network.
	 * @param timeHorizon The time horizon, i.e. the number of layers.
	 * @param sources The sources of the base network, may also be only one.
	 * @param sinks The sinks of the base network, may also be only one.
	 * @param allowStorageInNodes Says whether it shall be allowed to store flow in nodes, i.e.
	 *            whether there are hold-over arcs for all nodes or only for
	 *            sources and sinks.
	 */
	private void createTimeExpansion(NetworkInterface network,
			IdentifiableIntegerMapping<Edge> capacities,
			IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon,
			List<Node> sources, List<Node> sinks,
			boolean allowStorageInNodes) {

    	// get ID+1 of node with highest ID
    	int nodeCount = network.nodeCount();

    	// Initialize data structures:
    	// list newNodes is to set nodes in the network later,
    	// the grid saves the structure of the time expanded
    	// network and has one row for each time layer
    	// and at least one column for every node.
    	LinkedList<Node> newNodes = new LinkedList<Node>();
    	grid = new Node[nodeCount][timeHorizon];
    	originalID = new IdentifiableIntegerMapping<Node>(nodeCount*timeHorizon);

    	// Write the base network in the zero layer of
    	// the time expanded network.
    	for (Node node : network.nodes()){
    		grid[node.id()][0] = node;
    		if (originalID.getDomainSize()<=node.id())
    			originalID.setDomainSize(node.id()+1);
    		originalID.set(node, node.id());
    	}

    	// Create node objects for each time layer
    	// 1 .. T-1 and save the the references
    	// in the grid and the newNodes list.
    	// nodeCount needs to be actualized.
    	for (int t = 1; t <= timeHorizon-1; t++) {
        	for (Node node : network.nodes()){
        		Node newNode = new Node(nodeCount);
        		nodeCount++;
        		grid[node.id()][t] = newNode;
        		newNodes.add(newNode);
        		if (originalID.getDomainSize()<=newNode.id())
        			originalID.setDomainSize(newNode.id()+1);
        		originalID.set(newNode, node.id());
        	}
        }

    	// Store the original and the new nodes in the network
    	setNodeCapacity(nodeCount);
    	setNodes(network.nodes());
    	setNodes(newNodes);

    	// Calculate how many edges we will
    	// create by cloning the original edges:
        int edgeCount = 0;
        // Each edge e will be copied starting at each
        // time layer from 1 .. T-transitTime(e)+1,
        // edges in upper time layers would go outside
        // the time expanded network.
        for (Edge edge : network.edges()) {
            edgeCount += (int)Math.max(0, timeHorizon - (int) transitTimes.get(edge));
        }

        // set number of edges besides hold-over arcs.
        realEdges = edgeCount;

        // If storage in nodes is allowed, hold-over edges must be counted
        // for all nodes, else only for the sources and the sinks
        if (allowStorageInNodes) {
            edgeCount += (timeHorizon-1) * network.nodeCount();
        } else {
            edgeCount += (timeHorizon-1) * (sources.size() + sinks.size());
        }
        // Acquire enough space to store all edges.
        setEdgeCapacity(edgeCount);

        // Create mappings to store capacities and transit times for all edges.
        this.capacities = new IdentifiableIntegerMapping<Edge>(edgeCount);
        this.costs = new IdentifiableIntegerMapping<Edge>(edgeCount);

        // Create the edges for time layers 0 .. T-1.
        for (int t = 0; t <= timeHorizon-1; t++) {
            for (Edge edge : network.edges()) {
            	// Edges that would stick out of the time expanded network
            	// shall not be created.
                if (t + transitTimes.get(edge) > timeHorizon-1) continue;

                // Get the correct copies of the start end end node.
                Node start = grid[edge.start().id()][t];
                Node end = grid[edge.end().id()][t+transitTimes.get(edge)];

                //Node start = nodes.get(t * network.nodeCount() + edge.start().id());
                //Node end = nodes.get((t + (int) transitTimes.get(edge)) * network.nodeCount() + edge.end().id());

                // Insert edge into time expanded network
                // and copy transit time and capacities.
                Edge e = createAndSetEdge(start, end);
                this.capacities.set(e, capacities.get(edge));
                this.costs.set(e, transitTimes.get(edge));
            }
        }

        // Calculate hold-over arcs for all nodes
		// or only for sinks and sources.

		// create a hold-over arc for each node from
		// each time layer 0 .. T-1 to the time layer above.
		for (int t = 0; t < timeHorizon - 1; t++) {
			for (Node node : network.nodes()) {

				// check whether node is a sink or a source
				boolean isSink = sinks.contains(node);
				boolean isSinkOrSource = (isSink || sources.contains(node));

				// If we do only want hold-over arcs for sources and sinks
				// and this one is not a source or sink, go to the next node.
				if (!allowStorageInNodes && !isSinkOrSource)
					continue;

				// Get the correct copies of the node as
				// start and end of the new edge.

				Node start = grid[node.id()][t];
				Node end = grid[node.id()][t + 1];

				// Insert edge into time-expanded network
				// and set capacity

				Edge e = createAndSetEdge(start, end);
				this.capacities.set(e, Integer.MAX_VALUE);

				if (isSink)
					this.costs.set(e,0);
				else this.costs.set(e,1);
			}
		}
	}

    /**
     * Creates a new time expanded network for a dynamic flow problem.
     * @param network the network underlying the flow problem.
     * @param capacities the flow problem's capacities.
     * @param transitTimes the flow problem's transit times.
     * @param source the flow problem's source, may not be null.
     * @param sink the flow problem's sink, may not be null.
     * @param timeHorizon the flow problem's time horizon.
     * @param allowStorageInNodes {@code true} if flow can wait in all nodes, {@code false}
     * otherwise.
     */
   public TimeExpandedNetwork(NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, Node source, Node sink, int timeHorizon, boolean allowStorageInNodes) {
		this(allowStorageInNodes, network, timeHorizon);
		if (source ==null && sink==null)
			throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.SinkSourceNullException"));
		if (source ==null)
			throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.SourceIsNullException"));
		if (sink==null)
			throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.SinkIsNullException"));
		sources = new ListSequence<Node>();
		sources.add(source);
		originalSources = sources;
		originalSinks = new ListSequence<>();
		originalSinks.add(sink);
		createTimeExpansion(network, capacities, transitTimes, timeHorizon,
				sources, originalSinks, allowStorageInNodes);
		this.source = grid[source.id()][0];
		this.sink = grid[sink.id()][timeHorizon-1];
		sinks = new ListSequence<>();
		sinks.add(this.sink);
	}

    private void createSuperSource(int supersouceOutgoingCapacitiy){
		// Create super source
    	int nodeCount = this.getNodeCapacity();
		Node supersource = new Node(nodeCount);
		nodeCount++;
		this.setNodeCapacity(nodeCount);
		this.setNode(supersource);
		source = supersource;
		sources = new ListSequence<Node>();
		sources.add(source);

		int edgeCount = this.getEdgeCapacity();
		this.setEdgeCapacity(edgeCount+originalSources.size());

		// Add edges to the time expanded network:
		// The super source is connected to all sources
		// on layer zero with edges having maximal capacity
		// and no costs.

		for (Node source : originalSources){
			Edge newEdge = new Edge(edgeCount, supersource, grid[source.id()][0]); // only source should also work
			this.setEdge(newEdge);
			capacities.set(newEdge, supersouceOutgoingCapacitiy);
			costs.set(newEdge, 0);
			edgeCount++;
		}

    }

    private void createSuperSourceAndSink(int supersouceOutgoingCapacitiy){

    	if (originalSources.size() <= 0)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.OriginalSourcesException"));
    	if (originalSinks.size() <= 0)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.OriginalSinksException"));

    	if (originalSources.size()>1)
    		createSuperSource(supersouceOutgoingCapacitiy);
    	else {
    		this.source = originalSources.get(0);
    		sources = new ListSequence<Node>();
    		sources.add(source);
    	}
    	if (originalSinks.size()>1)
    		createSuperSink();
    	else {
    		this.sink = grid[originalSinks.get(0).id()][timeHorizon-1];
    		sinks = new ListSequence<Node>();
    		sinks.add(sink);
    	}

    }

    public TimeExpandedNetwork(NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon, List<Node> sources, List<Node> sinks, boolean allowStorageInNodes){
    	this(network, capacities, transitTimes, timeHorizon, sources, sinks, Integer.MAX_VALUE, allowStorageInNodes);
    }

    public TimeExpandedNetwork(NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon, List<Node> sources, List<Node> sinks, int supersouceOutgoingCapacitiy, boolean allowStorageInNodes ){
    	this(allowStorageInNodes,network,timeHorizon);
    	originalSources = new ListSequence<>(sources);
    	this.sources = originalSources;
    	originalSinks = new ListSequence<>(sinks);

		createTimeExpansion(network, capacities, transitTimes, timeHorizon, sources, originalSinks, allowStorageInNodes);

		createSuperSourceAndSink(supersouceOutgoingCapacitiy);
    }

	public TimeExpandedNetwork(NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon, IdentifiableIntegerMapping<Node> supplies, boolean allowStorageInNodes) {
		this( network, capacities, transitTimes, timeHorizon, supplies, allowStorageInNodes, true );
	}

		public TimeExpandedNetwork(NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon, IdentifiableIntegerMapping<Node> supplies, boolean allowStorageInNodes, boolean createSuperSouce ) {
		this(allowStorageInNodes, network, timeHorizon);
		sources = new ListSequence<>();
		originalSinks = new ListSequence<>();
		int overallSupply=0;
		for (Node node : network.nodes()) {
			if (!supplies.isDefinedFor(node))
				throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.NoSupplyNodeException"));
			else {
				overallSupply += supplies.get(node);
				if (supplies.get(node)>0)
					sources.add(node);
				if (supplies.get(node)<0)
					originalSinks.add(node);
			}
		}
		originalSources = sources;

		if (overallSupply != 0)
			throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.SumNotZeroException"));

		createTimeExpansion(network, capacities, transitTimes, timeHorizon, sources, originalSinks, allowStorageInNodes );

		if( createSuperSouce )
			createSuperSourceAndSink(supplies);
		else {
			// do not create a super source!
			// create time expanded supply-vector and copy source supplies

			sources = new ListSequence<>();

			int negativeSupplies = 0;

    	this.supplies = new IdentifiableIntegerMapping<>(this.nodes().size());
    	for (Node n : this.nodes()) {
    		if( supplies.isDefinedFor( n ) ) {
					if( supplies.get( n ) >= 0 )
						this.supplies.set( n, supplies.get( n ) );
					else {
						negativeSupplies += supplies.get( n );
						this.supplies.set( n, 0 ); // old sinks are no sinks any more
					}
					if( supplies.get( n ) > 0 )
						sources.add( n );
				} else
					this.supplies.set(n, 0);
    	}


			// THis only works for one single sink

    		this.sink = grid[originalSinks.get(0).id()][timeHorizon-1];
    		sinks = new ListSequence<>();
    		sinks.add(sink);


		//if (sink.id() >= this.supplies.getDomainSize())
		//	this.supplies.setDomainSize(sink.id()+1);
    	this.supplies.set(sink, negativeSupplies );
    //}
		}
	}

    private void createSuperSource(IdentifiableIntegerMapping<Node> supplies){
		// Create super source
    	int nodeCount = this.getNodeCapacity();
		Node supersource = new Node(nodeCount);
		nodeCount++;
		this.setNodeCapacity(nodeCount);
		this.setNode(supersource);
		source = supersource;
		sources = new ListSequence<>();
		sources.add(source);

		int edgeCount = this.getEdgeCapacity();
		this.setEdgeCapacity(edgeCount+originalSources.size());

		// Add edges to the time expanded network:
		// The super source is connected to all sources
		// on layer zero with edges having the capacity
		// equal to the former supply of the sources
		// and without costs.

		int superSourceSupply = 0;

		for (Node source : originalSources){
			Edge newEdge = new Edge(edgeCount, supersource, grid[source.id()][0]); // only source should also work
			this.setEdge(newEdge);
			capacities.set(newEdge, supplies.get(source));
			superSourceSupply += supplies.get(source);
			costs.set(newEdge, 0);
			edgeCount++;
		}

		if (supersource.id() >= this.supplies.getDomainSize())
			this.supplies.setDomainSize(supersource.id()+1);
		this.supplies.set(supersource, superSourceSupply);

    }

    private void createSuperSink(){
    	// Create super sink
    	int nodeCount = this.getNodeCapacity();
		Node supersink = new Node(nodeCount);
		nodeCount++;
		this.setNodeCapacity(nodeCount);
		this.setNode(supersink);
		sink = supersink;
		sinks = new ListSequence<>();
		sinks.add(sink);

		int edgeCount = this.getEdgeCapacity();
		this.setEdgeCapacity(edgeCount+originalSinks.size());

		// Add edges to the time expanded network:
		// The super sink is connected to all sinks
		// on layer timeHorizon-1 by an arc with
		// max capacity and no costs.

		for (Node sink: originalSinks){
			Edge newEdge = new Edge(edgeCount, grid[sink.id()][timeHorizon-1], supersink);
			this.setEdge(newEdge);
			capacities.set(newEdge,Integer.MAX_VALUE);
			costs.set(newEdge, 0);
			edgeCount++;
		}
    }

    private void createSuperSourceAndSink(IdentifiableIntegerMapping<Node> supplies){

    	this.supplies = new IdentifiableIntegerMapping<>(this.nodes().size());
    	for (Node n : this.nodes()){
    		if (n.id() >= this.supplies.getDomainSize())
    			this.supplies.setDomainSize(n.id()+1);
    		this.supplies.set(n, 0);
    	}

    	if (originalSources.size() <= 0)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.OriginalSourcesException"));
    	if (originalSinks.size() <= 0)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.OriginalSinksException"));

    	if (originalSources.size()>1)
    		createSuperSource(supplies);
    	else {
    		this.source = originalSources.get(0);
    		sources = new ListSequence<Node>();
    		sources.add(source);
    		this.supplies.set(source, supplies.get(source));
    	}
    	if (originalSinks.size()>1)
    		createSuperSink();
    	else {
    		this.sink = grid[originalSinks.get(0).id()][timeHorizon-1];
    		sinks = new ListSequence<Node>();
    		sinks.add(sink);
    	}

		if (sink.id() >= this.supplies.getDomainSize())
			this.supplies.setDomainSize(sink.id()+1);
    	this.supplies.set(sink, -this.supplies.get(source));

    }

  /*  public TimeExpandedNetwork(AbstractNetwork network,
			IdentifiableIntegerMapping<Edge> capacities,
			IdentifiableIntegerMapping<Edge> transitTimes,
			LinkedList<Node> sources, LinkedList<Node> sinks,
			int timeHorizon,
			boolean allowStorageInNodes) {
		this(allowStorageInNodes, network, timeHorizon);
		this.sinks = new ListSequence<Node>(sinks);
		this.sources = new ListSequence<Node>(sources);
		createTimeExpansion(network, capacities, transitTimes, timeHorizon,
				sources, sinks, allowStorageInNodes);
	}*/

    private TimeExpandedNetwork(boolean allowStorageInNodes, NetworkInterface network, int timeHorizon){
    	super(0,0);
        this.allowStorageInNodes = allowStorageInNodes;
        this.network = network;
        this.timeHorizon = timeHorizon;
    }

    /**
     * Translates a static path in this time expanded network into a string.
     * O(edges in the path).
     * @param path the static path to be translated.
     * @return a dynamic path in the underlying base network that is equivalent
     * to this path.
     */
    public DynamicPath translatePath(StaticPath path) {
        DynamicPath result = new DynamicPath();
        int delay = 0;
        for (Edge edge : path) {
        	if (!originalID.isDefinedFor(edge.start()) || !originalID.isDefinedFor(edge.end())) continue;
        	if (isHoldoverArc(edge))
        		delay++;
        	else {
        		Node from = network.getNode(originalID.get(edge.start()));
        		Node to = network.getNode(originalID.get(edge.end()));
        		Edge e = network.getEdge(from,to);
        		result.addLastEdge(e, delay);
        		delay = 0;
        	}
        }
        return result;
    }

    // This method uses that the original network does not include loops!
    private boolean isHoldoverArc(Edge edge){
    	if (edge==null)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.EdgeIsNullException"));
    	if (edge.start() == null)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.StartNodeIsNullException"));
    	if (edge.end() == null)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.EndNodeIsNullException"));
    	return (originalID.get(edge.start()) == originalID.get(edge.end()));
    }

    /**
     * Translates a static path in this time expanded network into a dynamic
     * path in the underlying base network. O(edges in the path).
     * @param path the static path to be translated.
     * @return a string representation of the dynamic path which takes the
     * structure of the time expanded network into account.
     */
    @Deprecated
    public String translatePathToString(StaticPath path) {
        StringBuilder result = new StringBuilder();
        int delay = 0;
        for (Edge edge : path) {
            if (edge.id() >= realEdges) {
                delay++;
            } else {
                if (delay > 0) {
                    result.append("[" + delay + "], ");
                }
                result.append(String.format("(%1$s [%2$s], %3$s [%4$s])", edge.start().id() % network.nodeCount(), edge.start().id() / network.nodeCount(), edge.end().id() % network.nodeCount(), edge.end().id() / network.nodeCount()));
                result.append(", ");
                delay = 0;
            }
        }
        return result.toString();
    }

    /**
     * Returns whether flow can wait in all nodes, or only in sources and sinks.
     * Runtime O(1).
     * @return {@code true} if flow can wait in all nodes, {@code false}
     * otherwise.
     */
    public boolean allowStorageInNodes() {
        return allowStorageInNodes;
    }

    /**
     * Returns the capacities belonging to this time expanded network.
     * Runtime O(1).
     * @return the capacities belonging to this time expanded network.
     */
    public IdentifiableIntegerMapping<Edge> capacities() {
        return capacities;
    }

    /**
     * Returns the network underlying the time expanded network. Runtime O(1).
     * @return the network underlying the time expanded network.
     */
    public NetworkInterface network() {
        return network;
    }

    /**
     * Returns the sinks of the time expanded network. Runtime O(1).
     * All original sinks will be returned,
     * even if a super sink exists, but the super sink will not
     * be returned.
     * @return the sinks of the time expanded network.
     */
    public LinkedList<Node> sinks() {
        return sinks;
    }

    /**
     * Returns the single sink of the time expanded network,
     * if it has one. Else an {@code AssertionError} will be thrown.
     * The network does for example not contain a single sink
     * if it has multiple sinks and does not contain a super sink.
     * @return the single sink of this time expanded network,
     *         if it has one.
     */
    public Node singleSink(){
    	if (sink == null)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.NoSingleSinkException"));
    	else
    		return sink;
    }

    /**
     * Returns the sources of the time expanded network. Runtime O(1).
     * All original sources will be returned,
     * even if a super source exists, but the super source will not
     * be returned.
     * @return the sources of the time expanded network.
     */
    public LinkedList<Node> sources() {
        return sources;
    }

    /**
     * Returns the single source of the time expanded network,
     * if it has one. Else an {@code AssertionError} will be thrown.
     * The network does for example not contain a single source
     * if it has multiple sources and does not contain a super source.
     * @return the single source of this time expanded network,
     *         if it has one.
     */
    public Node singleSource(){
    	if (source == null)
    		throw new AssertionError(GraphLocalization.loc.getString ("ds.graph.NoSingleSourceException"));
   		return source;
    }

    /**
     * Returns the time horizon of this time expanded network. Runtime O(1).
     * @return the time horizon of this time expanded network.
     */
    public int timeHorizon() {
        return timeHorizon;
    }

    /**
     * Returns the cost function belonging to this time expanded network.
     * Runtime O(1).
     * @return the cost function belonging to this time expanded network.
     */
    public IdentifiableIntegerMapping<Edge> costs() {
        return costs;
    }

    /**
     * Return the supply function belonging to this time expanded network.
     * Runtime O(1)
     * @return the supply function belonging to this time expanded network.
     */
    public IdentifiableIntegerMapping<Node> supplies(){
    	return supplies;
    }

    /**
     * Creates a copy of this time expanded network.
     * @return a copy of this time expanded network.
     */
    @Override
    public TimeExpandedNetwork clone() {
        TimeExpandedNetwork clone = new TimeExpandedNetwork(nodeCount(), edgeCount());
        clone.setNodes(nodes());
        clone.setEdges(edges());
        clone.network = network;
        clone.allowStorageInNodes = allowStorageInNodes;
        clone.capacities = capacities.clone();
        clone.realEdges = realEdges;
        clone.sink = sink.clone();
        clone.source = source.clone();
        clone.sinks = sinks.clone();
        clone.sources = sources.clone();
        clone.timeHorizon = timeHorizon;
        clone.costs = costs.clone();
        return clone;
    }

    /**
     * Compares this object with the specified object. If the specified object
     * is equivalent to this one {@code true} is returned, {@code false
     * } otherwise. A object is considered equivalent if and only if it is
     * a time expanded network with equals components (nodes, edges,
     * base network, capacities, ...). Runtime O(n + m).
     * @param o the object to compare this one to.
     * @return {@code true} if the specified object
     * is equivalent to this one, {@code false
     * } otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeExpandedNetwork) {
            TimeExpandedNetwork ten = (TimeExpandedNetwork) o;
            return network.equals(ten.network) && capacities.equals(ten.capacities)
                   && costs.equals(ten.costs) && super.equals(o);
//                   && (sink.equals(ten.sink) && (source.equals(ten.source))
//                   && (sinks.equals(ten.sinks)) && (sources.equals(ten.sources)));
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code for this time expanded network.
     * Runtime O(n + m).
     * @return a hash code computed by the sum of the hash codes of its
     * components.
     */
    @Override
    public int hashCode() {
        int hashCode = super.hashCode()+ capacities.hashCode() + costs.hashCode();
        return hashCode;
    }

    /**
     * Returns a string representation of this time expanded network.
     * Runtime O(n + m).
     * @return  a string representation of this residual network.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Base network: \n" + network.toString() + "\n");
        builder.append("Nodes of the time expanded network:\n");
        for (int t = this.timeHorizon-1; t >= 0; t--){
        	if (this.source != null)
        		if (t == 0)
        			builder.append(this.source+"\t");
        		else
        			builder.append("\t");
        	for (int i = 0; i < grid.length; i++)
        		builder.append(grid[i][t]+"\t");
        	if (this.sink != null)
        		if (t == timeHorizon-1)
        			builder.append(this.sink);
        	builder.append("\n");
        }
        builder.append("Edges of the time expanded network: \n");
        int counter = 0;
        for (Edge edge : edges()) {
            if (counter == 10) {
                counter = 0;
                builder.append("\n");
            }
            builder.append(edge.toString());
            builder.append(", ");
            counter++;
        }
        if (edgeCount() > 0) builder.delete(builder.length()-2, builder.length());

        builder.append("\n");
        builder.append("Time expanded capacities: \n" + capacities.toString() + "\n");
        builder.append("Time expanded costs: \n" + costs.toString() + "\n");
        builder.append("Storage in Nodes: \n" + allowStorageInNodes + "\n");
        builder.append("Time Horizon: " + timeHorizon + "\n");
        builder.append("Sources: ");
        if (sources!=null) builder.append(sources.toString());
        if (source!=null) builder.append(" SingleSource, "+source);
        builder.append(" Original sources: ");
        if (originalSources!=null) builder.append(originalSources); else builder.append("null");
        builder.append("\n");
        builder.append("Sinks: ");
        if (sinks!=null) builder.append(sinks.toString());
        if (sink!=null) builder.append(" SingleSink, "+sink);
        builder.append(" Original sinks: ");
        if (originalSinks!=null) builder.append(originalSinks); else builder.append("null");
        builder.append("\n");
        builder.append("Supply and demands: ");
        if (supplies == null) builder.append("null"); else builder.append(supplies);
        builder.append(".\n");
        return builder.toString();
    }

}
