/**
 * FordFulkersonTest.java Created: 06.06.2014, 15:59:15
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.netflow.ds.network.Network;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FordFulkersonTest {
  @Test
  public void testInstance() {
    Network network = FlowTestInstances.getDiamondExample();

    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    assertEquals( "Flow value", 3, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }


  @Test
  public void testHarrisRoss() {
    Network network = FlowTestInstances.getHarrisRoss();

    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    assertEquals( "Flow value", 152, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
}
