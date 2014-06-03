/**
 * BinaryTreeTest.java
 * Created: 23.07.2012, 12:59:40
 */
package ds.graph;

import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.math.coga.datastructure.searchtree.BinaryTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class BinaryTreeTest {

	@Test
	public void testBinaryTree() {
//		assertEquals( "", 1, 1 );
//		fail( "hätte nicht sein dürfen" );
//		assertEquals( "Testbedingung", 2, 1 );
		
		BinaryTree bt = new BinaryTree( 7 );

		assertEquals( "Height of empty Tree (contains a root automatically)", 0, bt.getHeight() );

		Node root = bt.getRoot();
		
		assertEquals( "Root id is 0", root.id(), 0 );
		
		for( Node n : bt ) {
			assertNull( "Parent of node " + n.id() + " is not null.", bt.getParent( n ) );
			assertNull( "Left child of node " + n.id() + " is not null.", bt.getLeft( n ) );
			assertNull( "Right child of node " + n.id() + " is not null.", bt.getRight( n ) );
		}
		
		Node A = root;
		int i = 1;
		Node B = bt.getNode( i++ );
		Node C = bt.getNode( i++ );
		Node D = bt.getNode( i++ );
		Node E = bt.getNode( i++ );
		Node F = bt.getNode( i++ );
		
		assertEquals( "Children of A", 0, bt.numOfChildren( A ) );
		assertEquals( "Children of B", 0, bt.numOfChildren( B ) );
		assertEquals( "Children of C", 0, bt.numOfChildren( C ) );
		assertEquals( "Children of D", 0, bt.numOfChildren( D ) );
		assertEquals( "Children of E", 0, bt.numOfChildren( E ) );
		assertEquals( "Children of F", 0, bt.numOfChildren( F ) );

		Edge e1 = bt.createAndSetEdge( A, B );

		assertEquals( "Height after A-B inserted", 1, bt.getHeight() );

		Edge e2 = bt.createAndSetEdge( A, C );

		assertEquals( "Height after A-C inserted", 1, bt.getHeight() );
		
		Edge e3 = bt.createAndSetEdge( B, D );

		assertEquals( "Height after B-D inserted", 2, bt.getHeight() );

		Edge e4 = bt.createAndSetEdge( B, E );
		Edge e5 = bt.createAndSetEdge( C, F );
		
		assertEquals( "Parent of B not A", bt.getParent( B ), A );
		assertEquals( "Parent of C not A", bt.getParent( C ), A );
		assertEquals( "Parent of D not B", bt.getParent( D ), B );
		assertEquals( "Parent of E not B", bt.getParent( E ), B );
		assertEquals( "Parent of F not C", bt.getParent( F ), C );
		
		assertEquals( "Children of A", 2, bt.numOfChildren( A ) );
		assertEquals( "Children of B", 2, bt.numOfChildren( B ) );
		assertEquals( "Children of C", 1, bt.numOfChildren( C ) );
		assertEquals( "Children of D", 0, bt.numOfChildren( D ) );
		assertEquals( "Children of E", 0, bt.numOfChildren( E ) );
		assertEquals( "Children of F", 0, bt.numOfChildren( F ) );
		
		assertTrue( "Left child of A", bt.getLeft( A ).equals( B ) || bt.getLeft( A ).equals( C ) );
		assertFalse( "Left child of A unique", bt.getLeft( A ).equals( B ) && bt.getRight( A ).equals( B ) );

		assertTrue( "Only one child of C", bt.getLeft( C ) == null || bt.getRight( C ) == null );
		assertFalse( "Exactly one child of C", bt.getLeft( C ) == null &&  bt.getRight( C ) == null );
		
		
		Node G = bt.getNode( i++ );
		try {
			bt.createAndSetEdge( E,F );
			fail( "Exception not thrown. Would create cycle (two incoming edges at F)" );
		} catch( IllegalArgumentException ex ) {
			
		}

		try {
			bt.createAndSetEdge( A,C );
			fail( "Exception not thrown. Two edges between same pair." );
		} catch( IllegalArgumentException ex ) {
			
		}

		try {
			Node H = bt.getNode( i++ );
			fail( "Node limit exceeded." );
		} catch( IndexOutOfBoundsException ex ) {
			
		}
		
		try {
			bt.createAndSetEdge( A,G );
			fail( "Exception not thrown. More that two outgoing arcs" );
		} catch( IllegalArgumentException ex ) {
			
		}
		
		bt.createAndSetEdge( C, G );
		
		assertEquals( "Height of complete Tree", 2, bt.getHeight() );

		System.out.println( bt.toString2() );

		System.out.println( BinaryTreeToString.format( bt ) );
		
		System.out.println( "Done.");
	}
}
