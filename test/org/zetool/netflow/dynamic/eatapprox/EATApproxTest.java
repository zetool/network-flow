package org.zetool.netflow.dynamic.eatapprox;

//package de.tu_berlin.coga.netflow.dynamic.eatapprox;
//
//import de.tu_berlin.coga.netflow.dynamic.earliestarrival.old.EATApprox;
//import de.tu_berlin.coga.netflow.dynamic.earliestarrival.old.LimitedMaxFlowOverTime;
//import de.tu_berlin.coga.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
//import de.tu_berlin.math.coga.graph.generator.RMFGEN;
//import de.tu_berlin.math.coga.rndutils.distribution.discrete.UniformDistribution;
//import de.tu_berlin.coga.graph.Edge;
//import de.tu_berlin.coga.graph.Node;
//import de.tu_berlin.coga.graph.DefaultDirectedGraph;
//import org.zetool.container.mapping.IdentifiableIntegerMapping;
//import de.tu_berlin.coga.graph.DirectedGraph;
//import java.util.ArrayList;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// *
// * @author Jan-Philipp Kappmeier
// */
//public class EATApproxTest {
//  UniformDistribution dist;
//
//  public EATApproxTest() {
//  }
//
//  private double computeBeta( ArrayList<Integer> fMax, ArrayList<Integer> eafApprox ) {
//    int maxEAT = eafApprox.size() - 1;
//    int maxFMax = fMax.size() - 1;
//    double beta = Double.MIN_VALUE;
//    for( int i = 0; i <= Math.max( maxEAT, maxFMax ); ++i ) {
//      double betaTemp = fMax.get( Math.min( i, maxFMax ) ) / (double) eafApprox.get( Math.min( i, maxEAT ) );
//      if( betaTemp > beta ) {
//        beta = betaTemp;
//      }
//    }
//    return beta;
//  }
//
//  @Before
//  public void initRandomGenerator() {
//    dist = new UniformDistribution( 1, 20 );
//  }
//
//  /**
//   * Solves a simple scenario that does not give the optimal solution.
//   */
//  @Test
//  public void easy() {
//    System.out.println( "RUN TEST easy" );
//    DefaultDirectedGraph n = new DefaultDirectedGraph( 4, 3 );
//
//    int timeHorizon = 2;
//
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 2 ) );
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 3 ) );
//    n.createAndSetEdge( n.getNode( 1 ), n.getNode( 3 ) );
//
//    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 3 );
//    capacities.set( n.getEdge( 0 ), 1 );
//    capacities.set( n.getEdge( 1 ), 1 );
//    capacities.set( n.getEdge( 2 ), 1 );
//
//    IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( 3 );
//    transitTime.set( n.getEdge( 0 ), 0 );
//    transitTime.set( n.getEdge( 1 ), 0 );
//    transitTime.set( n.getEdge( 2 ), 0 );
//
//    IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( 4 );
//    supplies.set( n.getNode( 0 ), 4 );
//    supplies.set( n.getNode( 1 ), 2 );
//    supplies.set( n.getNode( 2 ), -4 );
//    supplies.set( n.getNode( 3 ), -2 );
//
//    ArrayList<Node> sources = new ArrayList<>( 2 );
//    sources.add( n.getNode( 0 ) );
//    sources.add( n.getNode( 1 ) );
//
//    ArrayList<Node> sinks = new ArrayList<>( 2 );
//    sinks.add( n.getNode( 2 ) );
//    sinks.add( n.getNode( 3 ) );
//
//    MaximumFlowOverTimeProblem p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, timeHorizon );
//    EATApprox eata = new EATApprox( p, supplies );
//    eata.runAlgorithm();
//  }
//
//  @Test
//  public void boundTest() {
//    System.out.println( "RUN TEST general boundTest" );
//    DefaultDirectedGraph n = new DefaultDirectedGraph( 4, 4 );
//    //int M = 75;
//    int k = 32;
//
//    int timeHorizon = 1;
//
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 2 ) );
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 3 ) );
//    n.createAndSetEdge( n.getNode( 1 ), n.getNode( 2 ) );
//    n.createAndSetEdge( n.getNode( 1 ), n.getNode( 3 ) );
//
//    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 4 );
//    capacities.set( n.getEdge( 0 ), 1 );
//    capacities.set( n.getEdge( 1 ), k - 1 );
//    capacities.set( n.getEdge( 2 ), k - 1 );
//    capacities.set( n.getEdge( 3 ), (k - 1) * (k - 1) );
//
//    IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( 4 );
//    transitTime.set( n.getEdge( 0 ), 0 );
//    transitTime.set( n.getEdge( 1 ), 0 );
//    transitTime.set( n.getEdge( 2 ), 0 );
//    transitTime.set( n.getEdge( 3 ), 0 );
//
//    IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( 4 );
//    supplies.set( n.getNode( 0 ), k * k );
//    supplies.set( n.getNode( 1 ), k * (k - 1) );
//    supplies.set( n.getNode( 2 ), -k * k );
//    supplies.set( n.getNode( 3 ), -k * (k - 1) );
//
//    ArrayList<Node> sources = new ArrayList<>( 2 );
//    sources.add( n.getNode( 0 ) );
//    sources.add( n.getNode( 1 ) );
//
//    ArrayList<Node> sinks = new ArrayList<>( 2 );
//    sinks.add( n.getNode( 2 ) );
//    sinks.add( n.getNode( 3 ) );
//
//    ArrayList<Integer> flowValues = new ArrayList<>();
//    MaximumFlowOverTimeProblem p;
//
//    LimitedMaxFlowOverTime lmfot;
//    int T = 1;
//    int totalSupply = 2 * k * k - k;
//    do {
//      timeHorizon = T++;
//
//      p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, timeHorizon );
//      lmfot = new LimitedMaxFlowOverTime( p, supplies );
//      lmfot.runAlgorithm();
//      flowValues.add( (int) lmfot.getHiprf() );
//
//    } while( lmfot.getFlow() != totalSupply );
//
//    p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, T );
//    EATApprox eata = new EATApprox( p, supplies );
//    eata.runAlgorithm();
//
//    System.out.println( flowValues.toString() );
//    System.out.println( eata.getFlowCurve().toString() );
//    System.out.println( "beta = " + computeBeta( flowValues, eata.getFlowCurve() ) );
//
////		Assert.assertEquals( "Size of the arrival curve", eata.flowCurve.size(), 1 );
////		Assert.assertEquals( "Flow", eata.flowCurve.get(0).intValue(), k+1 );
////		Assert.assertEquals( "Optimal solution for time T", flowValues.get( flowValues.size()-1 ).longValue(), 2*k );
////		Assert.assertEquals( "Optimal flow for time 1", flowValues.get( 0 ).longValue(), k+1 );
////		Assert.assertEquals( "Linear increase of arrival curve", flowValues.size(), k );
//  }
//
//  @Test
//  public void boundTest2() {
//    System.out.println( "RUN TEST boundTest for Algorithm" );
//    DefaultDirectedGraph n = new DefaultDirectedGraph( 4, 3 );
//    int M = 75;
//
//    int timeHorizon = 1;
//
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 2 ) );
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 3 ) );
//    n.createAndSetEdge( n.getNode( 1 ), n.getNode( 3 ) );
//
//    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 3 );
//    capacities.set( n.getEdge( 0 ), 1 );
//    capacities.set( n.getEdge( 1 ), M - 1 );
//    capacities.set( n.getEdge( 2 ), 1 );
//
//    IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( 3 );
//    transitTime.set( n.getEdge( 0 ), 0 );
//    transitTime.set( n.getEdge( 1 ), 0 );
//    transitTime.set( n.getEdge( 2 ), 0 );
//
//    IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( 4 );
//    supplies.set( n.getNode( 0 ), M );
//    supplies.set( n.getNode( 1 ), M );
//    supplies.set( n.getNode( 2 ), -M );
//    supplies.set( n.getNode( 3 ), -M );
//
//    ArrayList<Node> sources = new ArrayList<>( 2 );
//    sources.add( n.getNode( 0 ) );
//    sources.add( n.getNode( 1 ) );
//
//    ArrayList<Node> sinks = new ArrayList<>( 2 );
//    sinks.add( n.getNode( 2 ) );
//    sinks.add( n.getNode( 3 ) );
//
//    ArrayList<Integer> flowValues = new ArrayList<>();
//    MaximumFlowOverTimeProblem p;
//
//    LimitedMaxFlowOverTime lmfot;
//    int T = 1;
//    int totalSupply = 2 * M;
//    do {
//      timeHorizon = T++;
//
//      p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, timeHorizon );
//      lmfot = new LimitedMaxFlowOverTime( p, supplies );
//      lmfot.runAlgorithm();
//      flowValues.add( (int) lmfot.getFlow() );
//
//    } while( lmfot.getFlow() != totalSupply );
//
//    p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, T );
//    EATApprox eata = new EATApprox( p, supplies );
//    eata.runAlgorithm();
//
//    System.out.println( flowValues.toString() );
//    System.out.println( eata.getFlowCurve().toString() );
//    System.out.println( "beta = " + computeBeta( flowValues, eata.getFlowCurve() ) );
//
//    Assert.assertEquals( "Size of the arrival curve", eata.getFlowCurve().size(), 1 );
//    Assert.assertEquals( "Flow", eata.getFlowCurve().get( 0 ).intValue(), M + 1 );
//    Assert.assertEquals( "Optimal solution for time T", flowValues.get( flowValues.size() - 1 ).longValue(), 2 * M );
//    Assert.assertEquals( "Optimal flow for time 1", flowValues.get( 0 ).longValue(), M + 1 );
//    Assert.assertEquals( "Linear increase of arrival curve", flowValues.size(), M );
//  }
//
//  @Test
//  public void RMFGENInstances() {
//    System.out.println( "RUN TEST RMFGEN" );
//
//    RMFGEN gen = new RMFGEN();
//    gen.setDistribution( dist );
//    int a = 2;
//    int b = 2;
//    gen.generateCompleteGraph( a, b );
//
//    DirectedGraph network = gen.getGraph();
//    IdentifiableIntegerMapping<Edge> capacities = gen.getCapacities();
//    IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( network.edgeCount());
//    for( int i = 0; i < network.edgeCount(); ++i ) {
//      transitTime.add( network.getEdge( i ), 0 );
//    }
//    IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( network.nodeCount());
//    for( int i = 0; i < network.nodeCount(); ++i ) {
//      supplies.set( network.getNode( i ), 0 );
//    }
//    ArrayList<Node> sources = new ArrayList<>( a * a );
//    ArrayList<Node> sinks = new ArrayList<>( a * a );
//    int totalSupply = 0;
//    for( int i = 0; i < a * a; ++i ) {
//      int supply = dist.getNextRandom() * 10;
//      totalSupply += supply;
//      supplies.set( network.getNode( i ), supply );
//      supplies.set( network.getNode( a * a * b - (i + 1) ), -supply );
//      sources.add( network.getNode( i ) );
//      sinks.add( network.getNode( a * a * b - (i + 1) ) );
//    }
//    System.out.println( "Total supply: " + totalSupply );
//    int timeHorizon = 4;
//
//    ArrayList<Integer> flowValues = new ArrayList<>();
//    LimitedMaxFlowOverTime lmfot;
//    int T = 1;
//    do {
//      timeHorizon = T++;
//
//      MaximumFlowOverTimeProblem p = new MaximumFlowOverTimeProblem( network, capacities, transitTime, sources, sinks, timeHorizon );
//      lmfot = new LimitedMaxFlowOverTime( p, supplies );
//      lmfot.runAlgorithm();
//      flowValues.add( (int) lmfot.getFlow() );
//    } while( lmfot.getFlow() != totalSupply );
//
//    timeHorizon = T;
//    MaximumFlowOverTimeProblem p = new MaximumFlowOverTimeProblem( network, capacities, transitTime, sources, sinks, timeHorizon );
//    EATApprox eata = new EATApprox( p, supplies );
//
//    eata.runAlgorithm();
//
//    System.out.println( flowValues.toString() );
//    System.out.println( eata.getFlowCurve().toString() );
//    System.out.println( "beta = " + computeBeta( flowValues, eata.getFlowCurve() ) );
//  }
//
//  @Test
//  public void test() {
//    System.out.println( "Linear" );
//    DefaultDirectedGraph n = new DefaultDirectedGraph( 6, 5 );
//    //int M = 75;
//
//    int timeHorizon = 1;
//
//    n.createAndSetEdge( n.getNode( 0 ), n.getNode( 1 ) );
//    n.createAndSetEdge( n.getNode( 1 ), n.getNode( 2 ) );
//    n.createAndSetEdge( n.getNode( 2 ), n.getNode( 3 ) );
//    n.createAndSetEdge( n.getNode( 3 ), n.getNode( 4 ) );
//    n.createAndSetEdge( n.getNode( 4 ), n.getNode( 5 ) );
//
//    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 5 );
//    capacities.set( n.getEdge( 0 ), 4 );
//    capacities.set( n.getEdge( 1 ), 4 );
//    capacities.set( n.getEdge( 2 ), 2 );
//    capacities.set( n.getEdge( 3 ), 1 );
//    capacities.set( n.getEdge( 4 ), 2 );
//
//    IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( 5 );
//    transitTime.set( n.getEdge( 0 ), 0 );
//    transitTime.set( n.getEdge( 1 ), 0 );
//    transitTime.set( n.getEdge( 2 ), 0 );
//    transitTime.set( n.getEdge( 3 ), 0 );
//    transitTime.set( n.getEdge( 4 ), 0 );
//
//    IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( 6 );
//    supplies.set( n.getNode( 0 ), 16 );
//    supplies.set( n.getNode( 1 ), -8 );
//    supplies.set( n.getNode( 2 ), 8 );
//    supplies.set( n.getNode( 3 ), -4 );
//    supplies.set( n.getNode( 4 ), 4 );
//    supplies.set( n.getNode( 5 ), -16 );
//
//    ArrayList<Node> sources = new ArrayList<>( 3 );
//    sources.add( n.getNode( 0 ) );
//    sources.add( n.getNode( 2 ) );
//    sources.add( n.getNode( 4 ) );
//
//    ArrayList<Node> sinks = new ArrayList<>( 3 );
//    sinks.add( n.getNode( 1 ) );
//    sinks.add( n.getNode( 3 ) );
//    sinks.add( n.getNode( 5 ) );
//
//    ArrayList<Integer> flowValues = new ArrayList<>();
//    MaximumFlowOverTimeProblem p;
//
//    LimitedMaxFlowOverTime lmfot;
//    int T = 1;
//    int totalSupply = 28;
//    do {
//      timeHorizon = T++;
//
//      p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, timeHorizon );
//      lmfot = new LimitedMaxFlowOverTime( p, supplies );
//      lmfot.runAlgorithm();
//      flowValues.add( (int) lmfot.getFlow() );
//
//    } while( lmfot.getFlow() != totalSupply );
//
//    p = new MaximumFlowOverTimeProblem( n, capacities, transitTime, sources, sinks, T );
//
//    System.out.println( flowValues.toString() );
//
//		//Assert.assertEquals( "Optimal solution for time T", flowValues.get( flowValues.size()-1 ).longValue(), 12 );
//    //Assert.assertEquals( "Optimal flow for time 1", flowValues.get( 0 ).longValue(), 4 );
//    //Assert.assertEquals( "Linear increase of arrival curve", flowValues.size(), 4 );
//  }
//
//}
