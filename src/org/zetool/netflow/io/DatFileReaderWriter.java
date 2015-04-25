/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
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

package org.zetool.netflow.io;

import org.zetool.graph.visualization.NodePositionMapping;
import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import org.zetool.netflow.dynamic.LongestShortestPathTimeHorizonEstimator;
import org.zetool.netflow.dynamic.earliestarrival.SEAAPAlgorithm;
//import de.tu_berlin.math.coga.zet.converter.graph.ZToGraphMapping;
import org.zetool.common.algorithm.Algorithm;
import org.zetool.common.algorithm.AlgorithmEvent;
import org.zetool.common.algorithm.AlgorithmListener;
import org.zetool.common.algorithm.AlgorithmStartedEvent;
import org.zetool.common.algorithm.AlgorithmTerminatedEvent;
import org.zetool.common.algorithm.AlgorithmProgressEvent;
import org.zetool.common.util.Formatter;
import org.zetool.common.util.units.TimeUnits;
import org.zetool.math.vectormath.Vector3;
import org.zetool.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import org.zetool.graph.Node;
//import ds.graph.NodeRectangle;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.graph.DefaultDirectedGraph;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.zetool.math.geom.NDimensional;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class DatFileReaderWriter implements AlgorithmListener {

	public static EarliestArrivalFlowProblem read( String filename ) throws FileNotFoundException, IOException {
		return read( filename, null, null );
	}

	public static boolean zeroNodeCapacities = false;
	static int factor = 2;
	static boolean verbose = false;

	public static EarliestArrivalFlowProblem read( String filename, IdentifiableObjectMapping<Node, Vector3> nodePositionMapping ) throws FileNotFoundException, IOException {
		IdentifiableIntegerMapping<Node> xPos = new IdentifiableIntegerMapping<>( 0 );
		IdentifiableIntegerMapping<Node> yPos = new IdentifiableIntegerMapping<>( 0 );
		EarliestArrivalFlowProblem eafp = read( filename, xPos, yPos );

		// convert positions
		for( Node node : eafp.getNetwork().nodes() )
			nodePositionMapping.set( node, new Vector3( xPos.get( node ), yPos.get( node ), 0 ) );

		return eafp;
	}

	public static EarliestArrivalFlowProblem read( String filename, IdentifiableIntegerMapping<Node> x, IdentifiableIntegerMapping<Node> y ) throws FileNotFoundException, IOException {
		BufferedReader read = new BufferedReader( new FileReader( new File( filename ) ) );
		String s;

		int nodeCount = 0;
		int edgeCount = -1;
		int estimatedTimeHorizon = -1;

//		ArrayList<Long> source_id = new ArrayList<Long>();
//		ArrayList<Integer> source_sup = new ArrayList<Integer>();


		ArrayList<Long> node_id = new ArrayList<>();
		ArrayList<Integer> node_sup = new ArrayList<>();

		ArrayList<Integer> node_x = new ArrayList<>();
		ArrayList<Integer> node_y = new ArrayList<>();

		ArrayList<Long> edge_start = new ArrayList<>();
		ArrayList<Long> edge_end = new ArrayList<>();
		ArrayList<Integer> edge_cap = new ArrayList<>();
		ArrayList<Integer> edge_len = new ArrayList<>();

		HashMap<Long, Integer> nodeMap = new HashMap<>();

		int currentNodeID = 0;
		int xmax = Integer.MIN_VALUE;
		int xmin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int sinks = 0;

		while( (s = read.readLine() ) != null) {
			if( s.length() == 0 )
				continue;
			if( s.startsWith( "SCALE" ) ) {
				factor = Integer.parseInt( s.substring( 6 ) );
				continue;
			}
			if( s.charAt( 0 ) == 'S' )
				continue;
			if( s.charAt( 0 ) == '%' )	// comment
				continue;
			if( s.charAt( 0 ) == 'N' ) {
				nodeCount = Integer.parseInt( s.substring( 2 ) );
				continue;
			}
			if( s.charAt( 0 ) == 'M' ) {
				edgeCount = Integer.parseInt( s.substring( 2 ) );
				continue;
			}
			if( s.startsWith( "TIME" ) ) {
				estimatedTimeHorizon = Integer.parseInt( s.substring( 5 ) );
				continue;
			}
			if( s.charAt( 0 ) == 'T' )
				continue;
			if( s.startsWith( "HOLDOVER" ) )
				continue;
			if( s.charAt( 0 ) == 'V' ) {
				final String[] split = s.split( " " );
				final long nodeID = Long.parseLong( split[1] );
				nodeMap.put( nodeID, currentNodeID++ );
				final int nodeSupply = Integer.parseInt( split[2] );
				node_id.add( nodeID );
				node_sup.add( nodeSupply );
				if( nodeSupply < 0 )
					sinks++;


				if( x != null && y != null ) {
					final int readX = (int)( factor * Double.parseDouble( split[3] ) );
					if( readX > xmax )
						xmax = readX;
					if( readX < xmin )
						xmin = readX;
					final int readY = (int)( factor * Double.parseDouble( split[4] ) );
					if( readY > ymax )
						ymax = readY;
					if( readY < ymin )
						ymin = readY;
					node_x.add( readX );
					node_y.add( readY );
				}
				continue;
			}
			if( s.charAt( 0 ) == 'E' ) {
				final String[] split = s.split( " " );
				final long start = Long.parseLong( split[1] );
				final long end = Long.parseLong( split[2] );
				if( !nodeMap.containsKey( start ) )
					nodeMap.put( start, currentNodeID++ );
				if( !nodeMap.containsKey( end ) )
					nodeMap.put( end, currentNodeID++ );
				edge_start.add( start );
				edge_end.add( end );
				edge_cap.add( Integer.parseInt( split[3] ) );
				edge_len.add( Integer.parseInt( split[4] ) );
				continue;
			}
			throw new IllegalStateException( "Unbekannte Zeile" );
		}

		// create missing nodes
		for( int i = node_id.size(); i <= nodeCount; ++i ) {
				//final String[] split = s.split( " " );
//				final long nodeID = Long.parseLong( split[1] );
//				nodeMap.put( nodeID, currentNodeID++ );
//				final int nodeSupply = Integer.parseInt( split[2] );
//				node_id.add( nodeID );
//				node_sup.add( nodeSupply );
//				if( nodeSupply < 0 )
//					sinks++;
//
//
//				if( x != null && y != null ) {
//					final int readX = (int)( factor * Double.parseDouble( split[3] ) );
//					if( readX > xmax )
//						xmax = readX;
//					if( readX < xmin )
//						xmin = readX;
//					final int readY = (int)( factor * Double.parseDouble( split[4] ) );
//					if( readY > ymax )
//						ymax = readY;
//					if( readY < ymin )
//						ymin = readY;
//					node_x.add( readX );
//					node_y.add( readY );
		}

		if( sinks > 1 )
			nodeCount++;

		int xDiff = xmax - xmin;
		int yDiff = ymax - ymin;

		int xadd = xDiff/2;
		int yadd = xDiff/2;

		int xoffset = 0;
		int yoffset = 0;

		if( xmax < 0 )
			xoffset = xmax + xadd;
		else if( xmin > 0 )
			xoffset = -xmin - xadd;
		else if( xmin < 0 )
			xoffset = -xmin - xadd;
		else
			xoffset = -xmax + xadd;

		if( ymax < 0 )
			yoffset = ymax + yadd;
		else if( ymin > 0 )
			yoffset = -ymin - yadd;
		else if( ymin < 0 )
			yoffset = -ymin - yadd;
		else
			yoffset = -ymax + yadd;

		if( x == null || y == null ) {
			System.out.println( "Ignoring coordinates.");
		} else {
			System.out.println( "x: " + xmin + " - " + xmax + "; offset " + xoffset );
			System.out.println( "y: " + ymin + " - " + ymax + "; offset " + yoffset );
		}

		//System.exit( 0 );

		if( x != null && y != null) {
			x.setDomainSize( nodeCount );
			y.setDomainSize( nodeCount );
		}

		//int edgeCount = edge_start.size();
		if( edgeCount < 0 )
			edgeCount = edge_start.size();

		if( sinks > 1 )
			edgeCount += sinks;

		System.out.println( "Node count: " + nodeCount );
		if( verbose )
			System.out.println( "Hasheinträge: " + nodeMap.size() );
		System.out.println( "Edge count: " + edge_start.size() );
		System.out.println( "Time horizon (read form file): " + estimatedTimeHorizon );
		if( verbose ) {
			System.out.println( "Knoten: " + node_id.toString() );
			System.out.println( "mit Angeboten " + node_sup.toString() );
			System.out.println( "Kanten von " + edge_start.toString() );
			System.out.println( "nach " + edge_end.toString() );
			System.out.println( "Mit Kapazitäten " + edge_cap.toString() );
			System.out.println( "Mit Länge " + edge_len.toString() );
			System.out.println( "Nächste Knotennummer: " + currentNodeID );
		}

		System.out.println();
		System.out.print( "Create network..." );


		DefaultDirectedGraph network = new DefaultDirectedGraph( nodeCount, edgeCount );
		for( int i = 0; i < (sinks > 1 ? edgeCount - sinks : edgeCount); ++i )
			network.createAndSetEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) );
		Long t;
		IdentifiableIntegerMapping<Edge> edgeCapacities = new IdentifiableIntegerMapping<>( network.edges() );
		IdentifiableIntegerMapping<Node> nodeCapacities = new IdentifiableIntegerMapping<>( network.nodes() );
		IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableIntegerMapping<>( network.edges() );
		IdentifiableIntegerMapping<Node> currentAssignment = new IdentifiableIntegerMapping<>( network.nodes() );

		for( int i = 0; i < (sinks > 1 ? edgeCount - sinks : edgeCount); ++i ) {
			edgeCapacities.set( network.getEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) ), edge_cap.get( i ) );
			transitTimes.set( network.getEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) ), edge_len.get( i ) );
		}


		//ArrayList<Node> sinks = new ArrayList<>();
		Node sink = sinks > 1 ? network.getNode( nodeCount-1 ) : null;
		int totalSupply = 0;
		ArrayList<Node> sources = new ArrayList<>();
		for( int i = 0; i < node_id.size(); ++i ) {
			currentAssignment.set( network.getNode( nodeMap.get( node_id.get( i ) ) ), node_sup.get( i ) );
			if( node_sup.get( i ) < 0  ) {
				if( sinks > 1) {
					// we have several sinks
					// create the edge
					Node sinkNode = network.getNode( nodeMap.get( node_id.get( i ) ) );
					Edge e = network.createAndSetEdge( sinkNode, sink );
					edgeCapacities.set( e, Integer.MAX_VALUE );
					transitTimes.set( e, 0 );
				} else // the one node with negative capacity is the sink
					sink = network.getNode( nodeMap.get( node_id.get( i ) ) );
				totalSupply = Math.max( Integer.MAX_VALUE, totalSupply + node_sup.get( i ) );
			}
			if( node_sup.get( i ) > 0 )
				sources.add( network.getNode( nodeMap.get( node_id.get( i ) ) ) );
			if( x!= null )
				x.set( network.getNode( nodeMap.get( node_id.get( i ) ) ), node_x.get( i ) + xoffset );
			if( y != null)
				y.set( network.getNode( nodeMap.get( node_id.get( i ) ) ), node_y.get( i ) + yoffset );

		}

		//int totalSupply = -node_sup.get( sink.id() );
		for( int i = 0; i < nodeCount; ++i )
			if( sources.contains( network.getNode( i ) ) )
				nodeCapacities.set( network.getNode( i ), node_sup.get( i ) );
			else
				nodeCapacities.set( network.getNode( i ), zeroNodeCapacities ? 0 : totalSupply );
		nodeCapacities.set( sink, -totalSupply );
//		for( int i = 0; i < nodeCount; ++i )
//			if( sources.contains( network.getNode( i ) ) )
//				nodeCapacities.set( network.getNode( i ), 1000 );



		System.out.println( " done." );

		if( verbose )
			System.out.println( network.toString() );

		return new EarliestArrivalFlowProblem( edgeCapacities, network, nodeCapacities, sink, sources, estimatedTimeHorizon, transitTimes, currentAssignment );
	}

	public static EarliestArrivalFlowProblem readOld( String filename ) throws FileNotFoundException, IOException {
		BufferedReader read = new BufferedReader( new FileReader( new File( filename ) ) );
		String s;

		int nodeCount = 0;
		int edgeCount = -1;
		int estimatedTimeHorizon = -1;

		ArrayList<Long> source_id = new ArrayList<Long>();
		ArrayList<Integer> source_sup = new ArrayList<Integer>();

		ArrayList<Long> sink_id = new ArrayList<Long>();
		ArrayList<Integer> sink_sup = new ArrayList<Integer>();

		ArrayList<Long> edge_start = new ArrayList<Long>();
		ArrayList<Long> edge_end = new ArrayList<Long>();
		ArrayList<Integer> edge_cap = new ArrayList<Integer>();
		ArrayList<Integer> edge_len = new ArrayList<Integer>();

		HashMap<Long, Integer> nodeMap = new HashMap<Long, Integer>();

		int currentNodeID = 0;

		while((s = read.readLine()) != null) {
			if( s.length() == 0 )
				continue;
			if( s.charAt( 0) == 'V' )
				continue;
			if( s.charAt( 0 ) == '%' )	// comment
				continue;
			if( s.charAt( 0 ) == 'N' ) {
				nodeCount = Integer.parseInt( s.substring( 2 ) );
				continue;
			}
			if( s.charAt( 0 ) == 'M' ) {
				edgeCount = Integer.parseInt( s.substring( 2 ) );
				continue;
			}
			if( s.startsWith( "TIME" ) ) {
				estimatedTimeHorizon = Integer.parseInt( s.substring( 5 ) );
				continue;
			}
			if( s.startsWith( "HOLDOVER" ) )
				continue;
			if( s.startsWith( "SCALE" ) )
				continue;
			if( s.charAt( 0 ) == 'S' && !s.startsWith( "SCALE" ) ) {
				final String[] split = s.split( " " );
				final long nodeID = Long.parseLong( split[1] );
				nodeMap.put( nodeID, currentNodeID++ );
				final int nodeSupply = Integer.parseInt( split[2] );
				source_id.add( nodeID );
				source_sup.add( nodeSupply );
				continue;
			}
			if( s.charAt( 0 ) == 'T' ) {
				final String[] split = s.split( " " );
				final long nodeID = Long.parseLong( split[1] );
				nodeMap.put( nodeID, currentNodeID++ );
				final int nodeSupply = Integer.parseInt( split[2] );
				sink_id.add( nodeID );
				sink_sup.add( -nodeSupply );
				continue;
			}
			if( s.charAt( 0 ) == 'E' ) {
				final String[] split = s.split( " " );
				final long start = Long.parseLong( split[1] );
				final long end = Long.parseLong( split[2] );
				if( !nodeMap.containsKey( start ) )
					nodeMap.put( start, currentNodeID++ );
				if( !nodeMap.containsKey( end ) )
					nodeMap.put( end, currentNodeID++ );
				edge_start.add( start );
				edge_end.add( end );
				edge_cap.add( Integer.parseInt( split[3] ) );
				edge_len.add( Integer.parseInt( split[4] ) );
				continue;
			}
			throw new IllegalStateException( "Unbekannte Zeile: '" + s + "'" );
		}

		//int edgeCount = edge_start.size();
		if( edgeCount < 0 )
			edgeCount = edge_start.size();

		System.out.println( "Knotenzahl: " + nodeCount );
		System.out.println( "Hasheinträge: " + nodeMap.size() );
		System.out.println( "Kantenzahl: " + edge_start.size() );
		System.out.println( "Zeithorizont (geschätzt): " + estimatedTimeHorizon );
		System.out.println( "Quellen: " + source_id.toString() );
		System.out.println( "mit Angeboten " + source_sup.toString() );
		System.out.println( "Senken: " + sink_id.toString() );
		System.out.println( "mit Bedarfen" + sink_sup.toString() );
		System.out.println( "Kanten von " + edge_start.toString() );
		System.out.println( "nach " + edge_end.toString() );
		System.out.println( "Mit Kapazitäten " + edge_cap.toString() );
		System.out.println( "Mit Länge " + edge_len.toString() );
		System.out.println( "Nächste Knotennummer: " + currentNodeID );

		System.out.println();
		System.out.println( "Erzeuge Netzwerk..." );


		DefaultDirectedGraph network = new DefaultDirectedGraph( nodeCount, edgeCount );
		for( int i = 0; i < edgeCount; ++i )
			network.createAndSetEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) );
		Long t;
		IdentifiableIntegerMapping<Edge> edgeCapacities = new IdentifiableIntegerMapping<>( network.edges() );
		IdentifiableIntegerMapping<Node> nodeCapacities = new IdentifiableIntegerMapping<>( network.nodes() );
		IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableIntegerMapping<>( network.edges() );
		IdentifiableIntegerMapping<Node> currentAssignment = new IdentifiableIntegerMapping<>( network.nodes() );

		for( int i = 0; i < edgeCount; ++i ) {
			edgeCapacities.set( network.getEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) ), edge_cap.get( i ) );
			transitTimes.set( network.getEdge( network.getNode( nodeMap.get( edge_start.get( i ) ) ), network.getNode( nodeMap.get( edge_end.get( i ) ) ) ), edge_len.get( i ) );
		}

		for( int i = 0; i < nodeCount; ++i )
			nodeCapacities.set( network.getNode( i ), 1000 );

		for( int i = 0; i < source_id.size(); ++i )
			currentAssignment.set( network.getNode( nodeMap.get( source_id.get( i ) ) ), source_sup.get( i ) );
		for( int i = 0; i < sink_id.size(); ++i )
			currentAssignment.set( network.getNode( nodeMap.get( sink_id.get( i ) ) ), sink_sup.get( i ) );

		System.out.println( network.toString() );


		Node sink = network.getNode( nodeMap.get( sink_id.get( 0 ) ) );
		ArrayList<Node> sources = new ArrayList<>();
		for( int i = 0; i < source_id.size(); ++i )
			sources.add( network.getNode( nodeMap.get( source_id.get( i ) ) ) );

		return new EarliestArrivalFlowProblem( edgeCapacities, network, nodeCapacities, sink, sources, estimatedTimeHorizon, transitTimes, currentAssignment );
	}

	public Algorithm computeFlow( EarliestArrivalFlowProblem eafp ) throws FileNotFoundException, IOException {

		if( eafp.getTimeHorizon() <= 0 ) {
			System.out.println( "Schätze Zeithorizont" );
			LongestShortestPathTimeHorizonEstimator estimator = new LongestShortestPathTimeHorizonEstimator();
			estimator.setProblem( eafp );
			estimator.run();
			eafp.setTimeHorizon( estimator.getSolution().getUpperBound() );
			System.out.println( "Geschätzter Zeithorizont: " + estimator.getSolution().getUpperBound() );
		}

		SEAAPAlgorithm algo = new SEAAPAlgorithm();
		algo.setProblem( eafp );
		algo.addAlgorithmListener( this );
		algo.run();
		//System.out.println( Formatter.formatTimeMilliseconds( algo.getRuntime() ) );
		long start = System.nanoTime();
		PathBasedFlowOverTime df = algo.getSolution().getPathBased();
		String result = String.format( "Sent %1$s of %2$s flow units in %3$s time units successfully.", algo.getSolution().getFlowAmount(), eafp.getTotalSupplies(), algo.getSolution().getTimeHorizon() );
		System.out.println( result );
		//AlgorithmTask.getInstance().publish( 100, result, "" );
		long end = System.nanoTime();
		System.err.println( Formatter.formatUnit( end - start, TimeUnits.NanoSeconds ) );
		return algo;
	}

	public void eventOccurred( AlgorithmEvent event ) {
		if( event instanceof AlgorithmProgressEvent )
			//System.out.println( ((AlgorithmProgressEvent)event).getProgress() );
			System.out.println( Formatter.formatPercent( ((AlgorithmProgressEvent)event).getProgress() ));
		else if( event instanceof AlgorithmStartedEvent )
			System.out.println( "Algorithmus startet." );
		else if( event instanceof AlgorithmTerminatedEvent )
			System.out.println( "Laufzeit Flussalgorithmus: " + TimeUnits.MilliSeconds );
		else
			System.out.println( event.toString() );
	}

	public static void main( String[] arguments ) throws FileNotFoundException, IOException {
		// MÖgliche Dateien:
		// problem.dat
		// siouxfalls_5_10s.dat
		// siouxfalls_50_10s.dat
		// siouxfalls_500_10s.dat
		// padang_10p_10s_flow01.dat
		// swiss_500_10s.dat

		System.out.println( "Version 1.0" );
		System.out.println( "Reads a file with an earliest arrival flow problem and solves the problem." );
		System.out.println( "Param #1: filename/path");
		System.out.println();

		String filename0 = "./testinstanz/problem.dat";
		String filename1 = "./testinstanz/siouxfalls_500_10s.dat";
		String filename2 = "./testinstanz/4 rooms demo.dat";

		try {
			DatFileReaderWriter ff = new DatFileReaderWriter();
//			EarliestArrivalFlowProblem eat = read( arguments[0] );
			EarliestArrivalFlowProblem eat = read( filename2 );
			ff.computeFlow( eat );
			//DatFileReaderWriter.writeFile( "problem.dat", eat, "./testinstanz/output.dat" );
		} catch( FileNotFoundException e ) {
			System.out.println( "Datei nicht gefunden." );
			printFiles();
		} catch( ArrayIndexOutOfBoundsException e ) {
			System.out.println( "Kein Dateiname angegeben." );
			printFiles();
		}
	}

	public static void printFiles() {
			System.out.println( "Mögliche Dateien:" );
			System.out.println( "problem.dat" );
			System.out.println( "siouxfalls_5_10s.dat" );
			System.out.println( "siouxfalls_5_10s.dat" );
			System.out.println( "siouxfalls_50_10s.dat" );
			System.out.println( "padang_10p_10s_flow01.dat" );
			System.out.println( "swiss_500_10s.dat" );
	}

	public static void writeFile( String original, EarliestArrivalFlowProblem eafp, String filename ) throws FileNotFoundException, IOException {
		//writeFile( filename, eafp.getNetwork().nodeCount(), eafp.getTimeHorizon(), eafp.getSources(), eafp.getSink(), eafp.getNetwork().edges(), eafp.getEdgeCapacities(), eafp.getTransitTimes(), eafp.getSupplies() );
    Writer writer = new BufferedWriter( new FileWriter( new File( filename ) ) );
    writer.write( "% Written by ZET FlowWriter\n" );
    writer.write( "% original file: " + original + '\n' );
		writeFile( writer, -1, eafp.getNetwork().nodes(), eafp.getSources(), eafp.getSink(), eafp.getNetwork().edges(), eafp.getEdgeCapacities(), eafp.getTransitTimes(), eafp.getSupplies(), null );
	}

//	public static void writeFile( String original, EarliestArrivalFlowProblem eafp, String filename, ZToGraphMapping mapping ) throws FileNotFoundException, IOException {
//		writeFile( original, filename, -1, eafp.getNetwork().nodes(), eafp.getSources(), eafp.getSink(), eafp.getNetwork().edges(), eafp.getEdgeCapacities(), eafp.getTransitTimes(), eafp.getSupplies(), mapping.getNodeRectangles() );
//	}

  /**
   * Call this for example using a BufferedWriter writer = new BufferedWriter( new FileWriter( new File( filename ) ) );
   * If using it for conversion, start the file with
   *   - writer.write( "% Written by ZET FlowWriter\n" );
   *   - writer.write( "% original file: " + original + '\n' );
   * @param <D>
   * @param <E>
   * @param writer
   * @param timeHorizon
   * @param nodes
   * @param sources
   * @param sink
   * @param edges
   * @param edgeCapacities
   * @param transitTimes
   * @param currentAssignment
   * @param nodePositions
   * @throws FileNotFoundException
   * @throws IOException 
   */
	public static <D extends Number,E extends NDimensional<D>> void writeFile( Writer writer, int timeHorizon, IdentifiableCollection<Node>nodes, List<Node> sources, Node sink, IdentifiableCollection<Edge> edges, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> currentAssignment, NodePositionMapping<E> nodePositions ) throws IOException {
		writer.write( "N " + nodes.size() + '\n' );
		writer.write( "M " + edges.size() + '\n' );
		writer.write( "TIME " + timeHorizon + '\n' );

		for( Node node : nodes ) {
			if( nodePositions == null ) {
				//writer.write( "V " + node.id() + ' ' + currentAssignment.get( node ) + '\n' ); // without coordinates
				//writer.write( "V " + node.id() + ' ' + currentAssignment.get( node ) + ' ' + 0 + ' ' + 0 + '\n'  ); // with temp coordinates
				if( currentAssignment.get( node ) != 0 )
					writer.write( "V " + node.id() + ' ' + currentAssignment.get( node ) + ' ' + 0 + ' ' + 0 + '\n'  ); // with temp coordinates
		} else {
        E point = nodePositions.get( node );
				writer.write( "V " + node.id() + ' ' + currentAssignment.get( node ) + ' ' + point.get( 1 ) + ' ' + point.get( 2 ) + '\n' );
			}
		}

		for( Edge edge : edges ) {
			writer.write( "E " + edge.start().id() + ' ' + edge.end().id() + ' ' + edgeCapacities.get( edge ) + ' ' + transitTimes.get( edge ) + '\n' );
		}
	}

	public static void writeFile( String original, String filename, int timeHorizon, IdentifiableCollection<Node> nodes, List<Node> sources, Node sink, IdentifiableCollection<Edge> edges, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> currentAssignment, IdentifiableIntegerMapping<Node> x, IdentifiableIntegerMapping<Node> y ) throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter( new FileWriter( new File( filename ) ) );
		String s;

		writer.write( "% Written by ZET FlowWriter\n" );
		writer.write( "% original file: " + original + '\n' );
		writer.write( "N " + nodes.size() + '\n' );
		writer.write( "M " + edges.size() + '\n' );
		writer.write( "TIME " + timeHorizon + '\n' );

		for( Node node : nodes ) {
			int xPosition = x.get( node );
			int yPosition = y.get( node );
				writer.write( "V " + node.id() + ' ' + currentAssignment.get( node ) + ' ' + xPosition + ' ' + yPosition + '\n' );
		}

		for( Edge edge : edges ) {
			writer.write( "E " + edge.start().id() + ' ' + edge.end().id() + ' ' + edgeCapacities.get( edge ) + ' ' + transitTimes.get( edge ) + '\n' );
		}

		writer.close();
	}


	public static void writeFileOld( String original, String filename, int nodeCount, int timeHorizon, List<Node> sources, Node sink, IdentifiableCollection<Edge> edges, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> currentAssignment ) throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter( new FileWriter( new File( filename ) ) );
		String s;

		writer.write( "% Written by ZET FlowWriter\n" );
		writer.write( "% original file: " + original + '\n' );
		writer.write( "N " + nodeCount + '\n' );
		writer.write( "TIME " + timeHorizon + '\n' );

		for( Node source : sources ) {
			writer.write( "S " + source.id() + ' ' + currentAssignment.get( source ) + '\n' );
		}

		writer.write( "T " + sink.id() + ' ' + (-currentAssignment.get( sink )) + '\n' );

		for( Edge edge : edges ) {
			writer.write( "E " + edge.start().id() + ' ' + edge.end().id() + ' ' + edgeCapacities.get( edge ) + ' ' + transitTimes.get( edge ) + '\n' );
		}

		writer.close();
	}
}
