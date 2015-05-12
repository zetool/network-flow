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
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FordFulkerson extends Algorithm<MaximumFlowProblem, MaximumFlow> {
  protected ResidualNetwork residualNetwork;
  protected long pushes;
  protected int flow;
  protected int augmentations;
  protected Node source;
  protected Node sink;
  boolean verbose = true;
  boolean useLower = true;
  private IdentifiableIntegerMapping<Edge> lowerCapacities;
  private MaximumFlowProblem problem;

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

  private void initializeDatastructures() {
    // Set up network
    problem = getProblem().isSingleSourceSink() ? getProblem() : getProblem().asSingleSourceProblem();

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

  public void augmentFlow( Path path, int value ) {
    residualNetwork.augmentFlow( path, value );
    
    pushes += path.length();
    
    Stack<Edge> s = new Stack<>();
    System.out.print( "Augmented on " );
    for( Edge e : path ) {
      System.out.print( e );
    }
    System.out.println( " by " + value );

    flow += value;
    augmentations++;
  }

  public int getFlow() {
    return flow;
  }

  public int getAugmentations() {
    return augmentations;
  }

  public long getPushes() {
    return pushes;
  }

  IdentifiableBooleanMapping<Node> contained;
  Set<Node> cut;

  public Set<Node> computeCutNodes() {
    if( contained == null ) {
      contained = new IdentifiableBooleanMapping<>( residualNetwork.nodeCount() );
    }
    for( Node n : problem.getNetwork() ) {
      contained.set( n, false );
    }
    BreadthFirstSearch bfs = new BreadthFirstSearch();
    bfs.setProblem( residualNetwork );
    bfs.setStart( source );
    bfs.run();
    Set<Node> reachable = bfs.getReachableNodes();
    for( Node n : reachable ) {
      contained.set( n, true );
    }
    cut = reachable;
    return reachable;
  }

  LinkedList<Edge> cutOutgoing = new LinkedList<>();
  LinkedList<Edge> cutIncoming = new LinkedList<>();

  public void computeCutEdges() {
    if( cut == null ) {
      cut = computeCutNodes();
      cutOutgoing.clear();
      cutIncoming.clear();
    }

    //UnifiedGraphAccess g;
    for( Node n : cut ) {
      for( Edge e :  GraphUtil.outgoingIterator( residualNetwork, n ) ) {
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
