/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zetool.netflow.ds.network;

import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.UndirectedGraph;
import java.util.List;

/**
 *
 * @author kapman
 */
public class UndirectedNetwork extends GeneralNetwork implements UndirectedGraph {
  // TODO: replace constructors and change to a builder that maybe also automatically builds the network!

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( graph, capacities, sources, sinks );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( graph, capacities, source, sinks );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( graph, capacities, sources, sink );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( graph, capacities, source, sink );
  }

  @Override
  public UndirectedGraph getGraph() {
    return (UndirectedGraph)super.getGraph();
  }

  //******************************************************************************************************
  // Delegated methods of the graph of the network such that the network can be accessed as a graph itself

}