
package org.zetool.netflow.classic.maxflow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.zetool.netflow.ds.network.Network;
import static org.zetool.netflow.ds.network.NetworkUtil.generateDirected;
import static org.zetool.netflow.ds.network.NetworkUtil.generateUndirected;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FlowTestInstances {
  
  @Test
  public void testDirectedGraphGenerator() {
    Network n = generateDirected( 4, DIAMOND_EXAMPLE, 0, 3 );
    assertThat( n.nodeCount(), is( equalTo( 4 ) ) );
    assertThat( n.edgeCount(), is( equalTo( 5 ) ) );
    assertThat( n.sourceCount(), is( equalTo( 1 ) ) );
    assertThat( n.sinkCount(), is( equalTo( 1 ) ) );
    assertThat( n.sources().iterator().next(), is( equalTo( n.getNode( 0 ) ) ) );
    assertThat( n.sinks().iterator().next(), is( equalTo( n.getNode( 3 ) ) ) );
  }
  
  @Test
  public void testUndirectedGraphGenerator() {
    Network n = generateUndirected( 4, DIAMOND_EXAMPLE, 0, 3 );
    assertThat( n.nodeCount(), is( equalTo( 4 ) ) );
    assertThat( n.edgeCount(), is( equalTo( 5 ) ) );
    assertThat( n.sourceCount(), is( equalTo( 1 ) ) );
    assertThat( n.sinkCount(), is( equalTo( 1 ) ) );
    assertThat( n.sources().iterator().next(), is( equalTo( n.getNode( 0 ) ) ) );
    assertThat( n.sinks().iterator().next(), is( equalTo( n.getNode( 3 ) ) ) );
  }
  
  private final static int[][] SIMPLE = new int[][] {
    {0,1,1}
  };
  
  private final static int[][] DIAMOND_EXAMPLE = new int[][] {
      {0,1,2}, {0,2,1}, {1,2,1}, {1,3,1}, {2,3,2}
    };
  
  // used nodes from the map as numbers:
  // t = 35; // sink
  // EG = 32; M = 33; B = 34; R = 15; H = 16; S = 18;
  // E4 = 27; E3 = 28; W4 = 29; W3 = 30; N13 = 13; S13 = 36;
  // s1 = 19; s2 = 20; s3 = 21; s6 = 22; s7 = 23; s8 = 24; s5 = 26; t2= 31; s9 = 37;
  private final static int[][] HARRID_ROSS_EXAMPLE  = new int[][] {
      {1,2,14}, {1,3,10}, {2,35,191}, {3,35,10}, {4,20,17}, {4,5,36}, {4,7,20},
      {4,8,16}, {5,6,60}, {6,32,49}, {6,9,19}, {7,8,24}, {7,18,6}, {8,9,53},
      {8,33,27}, {9,35,32}, // Here 11 flow units are arriving to much!
      {9,35,11}, // We add a shortcut path to the sink to send enough flow.
      {9,34,10}, {10,23,10}, {12,10,10}, {13,14,38}, {14,22,24}, {17,19,36},
      {45,46,8}, {46,17,20}, {48,19,32}, {49,4,36}, {49,19,2}, {49,50,17},
      {50,4,17}, {50,51,28}, {51,7,29}, {51,16,5}, {51,18,10}, {52,51,2},
      {52,23,4}, {52,24,16}, {15,52,14}, {15,51,8}, {15,16,19}, {16,1,16},
      {16,18,20}, {18,33,24}, {33,34,23}, {33,9,3}, {33,1,13}, {34,2,2},
      {34,35,10}, {34,32,19}, {19,20,30}, {19,4,33}, {20,21,23}, {20,6,29},
      {21,6,16}, {21,32,16}, {26,23,28}, {26,49,12}, {22,26,52}, {23,50,34},
      {23,51,28}, {24,23,50}, {25,22,22}, {25,27,30}, {27,29,34}, {29,48,30},
      {29,49,30}, {30,46,2}, {30,29,34}, {28,27,4}, {31,45,6}, {31,46,12},
      // zero flow arcs
      {1,34,10}, {5,9,25}, {20,5,20}, {2,3,8}, {31,30,20}, {25,31,26},
      {25,28,6}, {25,14,20}, {25,13,24}, {28,30,6}, {46,29,24}, {26,29,41},
      {22,27,16}, {22,23,34}, {10,14,30}, {10,22,34}, {10,24,10}, {13,36,20},
      {36,12,44}, {12,11,48}, {11,10,30}, {11,37,28}, {10,37,40}, {37,24,44},
      //  connections to sources and sinks
      {0,12,100}, {0,36,100}, {0,31,100}, {0,30,100}, {0,28,100}, {0,25,100},
      {0,13,100}, {0,52,100}, {0,15,100}, {32,35,100},
    };

  public static Network getSimpleExample() {
    return generateDirected( 2, SIMPLE, 0, 1 );
  }
  
  public static Network getDiamondExample() {
    return generateDirected( 4, DIAMOND_EXAMPLE, 0, 3 );
  }
  
  public static Network getDiamondExampleUndirected() {
    return generateUndirected( 4, DIAMOND_EXAMPLE, 0, 3 );
  }

  public static Network getHarrisRossOriginal() {
    return generateDirected( 53, HARRID_ROSS_EXAMPLE, 0, 35 );
  }

}
