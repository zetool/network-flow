package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.netflow.ds.flow.MaximumFlow;
import de.tu_berlin.coga.netflow.ds.network.ResidualNetwork;
import de.tu_berlin.coga.netflow.ds.network.ResidualNetworkExtended;
import de.tu_berlin.coga.container.mapping.IdentifiableBooleanMapping;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.traversal.BreadthFirstSearch;
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
  protected long pushes = 0;
  protected int flow = 0;
  protected int augmentations = 0;
  protected Node source;
  protected Node sink;
  boolean verbose = true;
  boolean useLower = true;
  private IdentifiableIntegerMapping<Edge> lowerCapacities;

  @Override
  protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
    if( residualNetwork == null ) // only initialize in the first run!
    {
      initializeDatastructures();
    } else {
      residualNetwork.update(); // second round
      cut = null;
    }

    int maxPossibleFlow = 0;
    for( Edge e : residualNetwork.outgoingEdges( source ) ) {
      maxPossibleFlow += residualNetwork.residualCapacities().get( e );
    }

    int maxPossibleFlow2 = 0;
    for( Edge e : residualNetwork.incomingEdges( sink ) ) {
      if( residualNetwork.residualCapacities().get( e ) == Integer.MAX_VALUE ) {
        maxPossibleFlow2 = Integer.MAX_VALUE;
        break;
      } else {
        maxPossibleFlow2 += residualNetwork.residualCapacities().get( e );
      }
    }

    if( maxPossibleFlow2 < maxPossibleFlow ) {
      maxPossibleFlow = maxPossibleFlow2;
    }

    int value = 0;
    do {
      StaticPath p = findPath();
      value = residualCapacity( p );
      augmentFlow( p, value );
      fireProgressEvent( value < Integer.MAX_VALUE ? (double)flow / maxPossibleFlow : 1 );
    } while( value > 0 && value < Integer.MAX_VALUE ); //while( augmentFlow() != 0 )

    return new MaximumFlow( getProblem(), residualNetwork.flow() );
  }

  private void initializeDatastructures() {
    if( useLower ) {
      residualNetwork = new ResidualNetworkExtended( getProblem().getNetwork(), getProblem().getCapacities() );
      ((ResidualNetworkExtended)residualNetwork).setLower( lowerCapacities );
    } else {
      residualNetwork = new ResidualNetwork( getProblem().getNetwork(), getProblem().getCapacities() );
    }
    source = getProblem().getSource();
    sink = getProblem().getSink();
  }

  protected StaticPath findPath() {
    BreadthFirstSearch bfs = new BreadthFirstSearch();
    bfs.setProblem( residualNetwork );
    bfs.setStart( source );
    bfs.setStop( sink );
    bfs.run();
		return new StaticPath( bfs );
  }

  private int residualCapacity( StaticPath path ) {
    if( path.length() == 0 ) {
      return 0;
    }
    int min = Integer.MAX_VALUE;
    for( Edge e : path ) {
      min = Math.min( min, residualNetwork.residualCapacities().get( e ) );
    }
    return min;
  }

  public void augmentFlow( StaticPath path, int value ) {
    Stack<Edge> s = new Stack<>();

    for( Edge e : path ) {
      residualNetwork.augmentFlow( e, value );
      pushes++;
      s.push( e );
    }

    System.out.println( "Augmented on " );
    while( !s.empty() ) {
      System.out.print( s.pop() );
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
    for( Node n : getProblem().getNetwork() ) {
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

    for( Node n : cut ) {
      for( Edge e : getProblem().getNetwork().outgoingEdges( n ) ) {
        // find outgoing edges
        if( !contained.get( e.end() ) && !cutOutgoing.contains( e ) ) {
          cutOutgoing.add( e );
        }
      }
      for( Edge e : getProblem().getNetwork().incomingEdges( n ) ) {
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
