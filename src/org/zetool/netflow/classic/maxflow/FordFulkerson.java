package org.zetool.netflow.classic.maxflow;

import org.zetool.common.algorithm.Algorithm;
import org.zetool.container.mapping.IdentifiableBooleanMapping;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.structure.Path;
import org.zetool.graph.traversal.BreadthFirstSearch;
import org.zetool.graph.util.GraphUtil;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.netflow.ds.network.ResidualNetwork;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FordFulkerson extends Algorithm<MaximumFlowProblem, MaximumFlow> {
  protected ResidualNetwork residualNetwork;
  protected long pushes;
  protected long flow;
  protected int augmentations;
  protected Node source;
  protected Node sink;
  boolean verbose = true;
  boolean useLower = true;
  private IdentifiableIntegerMapping<Edge> lowerCapacities;
  IdentifiableBooleanMapping<Node> contained;
  Set<Node> cut;
  List<Edge> cutOutgoing = new LinkedList<>();
  List<Edge> cutIncoming = new LinkedList<>();


  @Override
  protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
    if( residualNetwork == null ) { // only initialize in the first run!    
      initializeDatastructures();
    } else {
      throw new IllegalStateException( "Whatever." );
    }
    cut = null;

    final long maxPossibleFlow = getUpperBound();
    
    int value = 0;
    do {
      Path p = findPath();
      value = residualCapacity( p );
      augmentFlow( p, value );
      fireProgressEvent( value < Integer.MAX_VALUE ? (double)flow / maxPossibleFlow : 1 );
    } while( value > 0 && value < Integer.MAX_VALUE );

    if( getProblem().isSingleSourceSink() ) {
      return new MaximumFlow( getProblem(), residualNetwork.flow() );      
    } else {
      residualNetwork.flow().setDomainSize( getProblem().getNetwork().edgeCount() );
      return new MaximumFlow( getProblem(), residualNetwork.flow() );
    }
  }
  
  /**
   * Computes an upper bound for the flow value.
   * @return an upper bound for the flow value
   */
  private long getUpperBound() {
    long maxPossibleFlow = 0;
    for( Edge e :  GraphUtil.outgoingIterator( residualNetwork, source ) ) {
      maxPossibleFlow += residualNetwork.residualCapacity( e );
    }

    long maxPossibleFlow2 = 0;
    for( Edge e :  GraphUtil.incomingIterator( residualNetwork, sink ) ) {
      maxPossibleFlow2 += residualNetwork.residualCapacity( e );
    }

    return Math.min( maxPossibleFlow, maxPossibleFlow2 );
  }

  private MaximumFlowProblem getProblemSingleSource() {
    return getProblem().isSingleSourceSink() ? getProblem() : getProblem().asSingleSourceProblem();
  }
  
  private void initializeDatastructures() {
    final MaximumFlowProblem problem = getProblemSingleSource();

    if( useLower ) {
      residualNetwork = ResidualNetwork.getResidualNetwork( problem.getNetwork(), problem.getCapacities(), problem.getSource(), problem.getSink() );
      //((ResidualNetworkExtended)residualNetwork).setLower( lowerCapacities );
    } else {
      residualNetwork = ResidualNetwork.getResidualNetwork( problem.getNetwork(), problem.getCapacities(), problem.getSource(), problem.getSink() );
    }
    source = problem.getSource();
    sink = problem.getSink();
  }

  protected Path findPath() {
    return GraphUtil.getPath( residualNetwork, source, sink );
  }

  private int residualCapacity( Path path ) {
    if( path.length() == 0 ) {
      return 0;
    }
    int min = Integer.MAX_VALUE;
    for( Edge e : path ) {
      min = Math.min( min, residualNetwork.residualCapacity( e ) );
    }
    return min;
  }

  /**
   * Augments flow along a path.
   * @param path the path
   * @param value the value by what the flow is augmented
   */
  public void augmentFlow( Path path, int value ) {
    residualNetwork.augmentFlow( path, value );
    pushes += path.length();
    flow += value;
    augmentations++;
  }

  public long getFlow() {
    return flow;
  }

  /**
   * Returns the number of augmentation steps performed during the execution of the algorithm.
   * @return the number of augmentation steps
   */
  public int getAugmentations() {
    return augmentations;
  }

  public long getPushes() {
    return pushes;
  }

  /**
   * Computes the cut nodes.
   * @return a set containing all nodes in the cut.
   */
  public Set<Node> computeCutNodes() {
    if( contained == null ) {
      contained = new IdentifiableBooleanMapping<>( residualNetwork.nodeCount() );
    }
    for( Node n : getProblemSingleSource().getNetwork() ) {
      contained.set( n, false );
    }
    BreadthFirstSearch bfs = new BreadthFirstSearch();
    bfs.setProblem( residualNetwork );
    Objects.requireNonNull( source );
    bfs.setStart( source );
    bfs.run();
    Set<Node> reachable = bfs.getReachableNodes();
    for( Node n : reachable ) {
      contained.set( n, true );
    }
    cut = reachable;
    return reachable;
  }

  public void computeCutEdges() {
    if( cut == null ) {
      cut = computeCutNodes();
      cutOutgoing.clear();
      cutIncoming.clear();
    }

    for( Node n : cut ) {
      for( Edge e : GraphUtil.outgoingIterator( residualNetwork, n ) ) {
       // find outgoing edges
        if( !contained.get( e.end() ) && !cutOutgoing.contains( e ) ) {
          cutOutgoing.add( e );
        }
      }
      for( Edge e :  GraphUtil.incomingIterator( residualNetwork, n ) ) {
        if( !contained.get( e.start() ) && !cutIncoming.contains( e ) ) {
          cutIncoming.add( e );
        }
      }
    }
  }

  public boolean isInCut( Node n ) {
    return contained.get( n );
  }

  public Set<Node> getCut() {
    return Collections.unmodifiableSet( cut );
  }

  public List<Edge> getOutgoingCut() {
    return Collections.unmodifiableList( cutOutgoing );
  }

  public List<Edge> getIncomingCut() {
    return Collections.unmodifiableList( cutIncoming );
  }

  public void setLowerCapacities( IdentifiableIntegerMapping<Edge> lowerCapacities ) {
    this.lowerCapacities = lowerCapacities;
  }
}
