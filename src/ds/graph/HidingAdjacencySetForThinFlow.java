/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ds.graph;

/**
 *
 * @author Sebastian Schenker
 */

import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import java.util.Iterator;
import java.util.LinkedList;

public class HidingAdjacencySetForThinFlow extends LinkedList<Edge> implements IdentifiableCollection<Edge> {
    
    private HidingSetForThinFlow<Edge> baseSet;
    private int visibleSize;

    public HidingAdjacencySetForThinFlow(HidingSetForThinFlow<Edge> baseSet) {
        this.baseSet = baseSet;
    }
    
    @Override
    public boolean contains(Edge edge) {
        //return super.contains(edge) && !baseSet.isHidden(edge);
        return super.contains(edge) && baseSet.isVisible(edge);
    }

    public boolean empty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return visibleSize;
    }

    @Override
    public Edge get(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(Edge e) {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    public Edge first() {
        return getFirst();
    }

    public Edge last() {
        return getLast();
    }

    public Edge random() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Edge predecessor(Edge edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Edge successor(Edge edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Edge> iterator() {
        return new SequentialHidingIterator();
    }
    
    @Override
    public boolean add(Edge edge) {
        boolean result = super.add(edge);
        increaseSize();
        return result;
    }
    
    public void decreaseSize() {
        visibleSize--;
    }
    
    public void increaseSize() {
        visibleSize++;
    }
    
    public class SequentialHidingIterator implements Iterator<Edge> {
        
        private Iterator<Edge> setIterator;
        private Edge next;
        
        public SequentialHidingIterator() {
            setIterator = HidingAdjacencySetForThinFlow.super.iterator();
        }

        public boolean hasNext() {
            while (setIterator.hasNext()) {
                Edge edge = setIterator.next();
                //if (!baseSet.isHidden(edge)) {
                  if(baseSet.isVisible(edge)) {
                    next = edge;
                    return true;
                }
            }
            return false;
        }

        public Edge next() {
            if (next == null) hasNext();
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }    

}
