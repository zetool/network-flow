
package de.tu_berlin.coga.netflow.ds.network;

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.UndirectedGraph;
import de.tu_berlin.coga.graph.structure.Path;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class UndirectedResidualNetwork extends UndirectedNetwork implements ResidualNetwork {
  /** The residual graph. */
  private ResidualGraph residualGraph;
  
  public UndirectedResidualNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( getResidualGraph( graph, capacities ), capacities, sources, sinks );
    init();
  }

  public UndirectedResidualNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( getResidualGraph( graph, capacities ), capacities, source, sinks );
    init();
  }

  public UndirectedResidualNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( getResidualGraph( graph, capacities ), capacities, sources, sink );
    init();
  }

  public UndirectedResidualNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( getResidualGraph( graph, capacities ), capacities, source, sink );
    init();
  }

  private static UndirectedResidualGraph getResidualGraph( UndirectedGraph digraph, IdentifiableIntegerMapping<Edge> capacities ) {
    return new UndirectedResidualGraph( digraph, capacities );
  }

  /**
   * Initializes the instance by setting up the residual graph.
   */
  private void init() {
    this.residualGraph = (ResidualGraph)super.getNetwork();

  }

  /****************************************************************************
   * Delegated methods of the graph of the network such that the network can be
   * accessed as a graph itself
   */

  @Override
  public Edge reverseEdge( Edge edge ) {
    return residualGraph.reverseEdge( edge );
  }

  @Override
  public boolean isReverseEdge( Edge edge ) {
    return residualGraph.isReverseEdge( edge );
  }

  
  /****************************************************************************
   * Delegated methods of the residual graph
   */
  
  @Override
  public void augmentFlow( Edge edge, int amount ) {
    residualGraph.augmentFlow( edge, amount );
  }

  @Override
  public void augmentFlow( Path path, int amount ) {
    residualGraph.augmentFlow( path, amount );
  }

  @Override
  public int residualCapacity( Edge edge ) {
    return residualGraph.residualCapacity( edge );
  }

  @Override
  public IdentifiableIntegerMapping<Edge> flow() {
    return residualGraph.flow();
  }
}
