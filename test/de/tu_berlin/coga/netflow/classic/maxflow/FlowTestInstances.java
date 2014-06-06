/**
 * FlowTestInstances.java
 * Created: 06.06.2014, 15:59:34
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.netflow.ds.network.DefaultNetwork;
import de.tu_berlin.coga.netflow.ds.network.Network;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FlowTestInstances {
  public static Network getDiamondExample() {
    DefaultDirectedGraph graph = new DefaultDirectedGraph( 4, 5 );
    graph.createAndSetEdge( graph.getNode( 0 ), graph.getNode( 1 ) );
    graph.createAndSetEdge( graph.getNode( 0 ), graph.getNode( 2 ) );
    graph.createAndSetEdge( graph.getNode( 1 ), graph.getNode( 2 ) );
    graph.createAndSetEdge( graph.getNode( 1 ), graph.getNode( 3 ) );
    graph.createAndSetEdge( graph.getNode( 2 ), graph.getNode( 3 ) );

    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 5 );
    capacities.add( graph.getEdge( 0 ), 2 );
    capacities.add( graph.getEdge( 1 ), 1 );
    capacities.add( graph.getEdge( 2 ), 1 );
    capacities.add( graph.getEdge( 3 ), 1 );
    capacities.add( graph.getEdge( 4 ), 2 );

    int[][] array = new int[][] {
      {0,1,2},
      {0,2,1},
      {1,2,1},
      {1,3,1},
      {2,3,2}
    };

    //MaximumFlowProblem mfp = new MaximumFlowProblem( graph, capacities, graph.getNode( 0 ), graph.getNode( 3 ) );
    //Network network = new DefaultNetwork( graph, capacities, graph.getNode( 0 ), graph.getNode( 3 ) );
    return fromArray( 4, array, 0, 3 );
  }

  public static Network getHarrisRoss() {
    int t = 35; // sink
    int EG = 32;
    int M = 33;
    int B = 34;

    int R = 15;
    int H = 16;
    int S = 18;
    int E4 = 27;
    int E3 = 28;
    int W4 = 29;
    int W3 = 30;
    int t2= 31;

    int s1 = 19;
    int s2 = 20;
    int s3 = 21;
    int s6 = 22;
    int s7 = 23;
    int s8 = 24;
    int s5 = 26;


    // frei: 15,16, 18, ..., 44, 47,

    int N13 = 13;

    int[][] array = new int[][] {
      {1,2,14},
      {1,3,10},
      {2,t,191},
      {3,t,10},
      {4,s2,17},
      {4,5,36},
      {4,7,20},
      {4,8,16},
      {5,6,60},
      {6,EG,49},
      {6,9,19},
      {7,8,24},
      {7,S,6},
      {8,9,53}, // oder 33?
      {8,M,27},
      {9,t,32}, // sollte +11 sein!
      //{9,t,11}, // otherwise, the result is not correct!
      {9,B,10},
      {10,s7,10},
      {12,10,10},
      {N13,14,38},
      {14,s6,24},
      {17,s1,36},
      {45,46,8},
      {46,17,20},
      {48,s1,32},
      {49,4,36},
      {49,s1,2},
      {49,50,17},
      {50,4,17},
      {50,51,28},
      {51,7,29}, // vielleicht 24
      {51,H,5},
      {51,S,10},
      {52,51,2},
      {52,s7,4},
      {52,s8,16},
      {R,52,14},
      {R,51,8},
      {R,H,19},
      {H,1,16},
      {H,S,20},
      {S,M,24},
      {M,B,23},
      {M,9,3},
      {M,1,13},
      {B,2,2},
      {B,t,10},
      {B,EG,19},
      {s1,s2,30},
      {s1,4,33},
      {s2,s3,23},
      {s2,6,29},
      {s3,6,16},
      {s3,EG,16},
      {s5,s7,28},
      {s5,49,12},
      {s6,s5,52},
      {s7,50,34},
      {s7,51,28},
      {s8,s7,50},
      {25,s6,22},
      {25,E4,30},
      {E4,W4,34},
      {W4,48,30},
      {W4,49,30},
      {W3,46,2},
      {W3,W4,34},
      {E3,E4,4},
      {t2,45,6},
      {0,t2,100},
      {0,W3,100},
      {0,E3,100},
      {0,25,100},
      {0,N13,100},
      {0,52,100},
      {0,R,100},
      //{0,}, left out
      //{0,}, left out
      {EG,t,100},
    };

    //MaximumFlowProblem mfp = new MaximumFlowProblem( graph, capacities, graph.getNode( 0 ), graph.getNode( 3 ) );
    //Network network = new DefaultNetwork( graph, capacities, graph.getNode( 0 ), graph.getNode( 3 ) );
    return fromArray( 53, array, 0, t );
  }

  private static Network fromArray( int nodeCount, int[][] array, int source, int sink ) {
    if( array[0].length != 3 ) {
      throw new IllegalArgumentException( "Illegal Format!" );
    }

    DefaultDirectedGraph graph = new DefaultDirectedGraph( nodeCount, array.length );
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( array.length );

    for( int i = 0; i < array.length; ++i ) {
      Edge e = graph.createAndSetEdge( graph.getNode( array[i][0] ), graph.getNode( array[i][1] ) );
      capacities.add( e, array[i][2] );
    }
    return new DefaultNetwork( graph, capacities, graph.getNode( source ), graph.getNode( sink ) );
  }
}
