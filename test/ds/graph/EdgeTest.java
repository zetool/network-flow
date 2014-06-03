///* zet evacuation tool copyright (c) 2007-10 zet evacuation team
// *
// * This program is free software; you can redistribute it and/or
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// */
//package ds.graph;
//
//import static org.junit.Assert.*;
//import junit.framework.JUnit4TestAdapter;
//import org.junit.Before;
//import org.junit.Test;
//import static org.easymock.classextension.EasyMock.*;
//
//
///*
// * EdgeTest.java
// * JUnit based test
// *
// * Created on 29. November 2007, 09:47
// */
//
///**
// *
// * @author mouk
// */
////@RunWith(JMock.class)
//public class EdgeTest
//{
//    Edge instance;
//    Node start;
//    Node end;
//
//    @Before
//    public void setUp()
//    {
//        start = (Node) createMock(Node.class);
//        end = (Node) createMock(Node.class);
//
//        //expect(start.id()).andReturn(6).anyTimes();
//        //replay(start);
//        //end = (Node) mockControl.getMock(Node.class);
//
//        instance = new Edge(10,start, end);
//
//    }
//
//    @Test(expected=Exception.class)
//    public void cannotInitiateWithNullObjecs()
//    {
//        Edge edge= null;
//        try
//        {
//            edge = new Edge(10,start,null);
//        }
//        finally{}
//
//        assertNull("should not be able to constructe an edge with null object", edge);
//        edge = null;
//        try
//        {
//            edge = new Edge(10,null,end);
//        }
//        finally{}
//
//        assertNull("should not be able to constructe an edge with null object", edge);
//
//    }
//
//    @Test
//    public void canGetOpposite()
//    {
//        assertEquals(end,instance.opposite(start));
//        assertEquals(start,instance.opposite(end));
//    }
//
//    @Test
//    public void canDetectEquality()
//    {
//        Edge oppositeEdge = new Edge(2,end, start);
//        assertFalse(instance.equals(oppositeEdge));
//        assertTrue(instance.equals(instance));
//    }
//
//     public static junit.framework.Test suite() {
//		return new JUnit4TestAdapter(EdgeTest.class);
//     }
//}
