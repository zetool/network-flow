/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
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

package ds.graph;

import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.Identifiable;
import java.util.Iterator;
import java.util.Random;
import java.util.RandomAccess;

/**
 *
 * @author Martin Groß / Sebastian Schenker
 */
public class HidingSetForThinFlow<E extends Identifiable> implements IdentifiableCollection<E> {

	private boolean[] visible;
	private int numberOfVisibleElements;
	private IdentifiableCollection<E> set;

	/* public HidingSetForThinFlow(IdentifiableSet<E> set) {
	 this(set, new boolean[set.size()], set.size());
	 } */
	public HidingSetForThinFlow( IdentifiableCollection<E> set, int capacity ) {
		this( set, new boolean[capacity], 0 );
	}

	public HidingSetForThinFlow( IdentifiableCollection<E> set, boolean[] visible ) {
		this.visible = visible;
		this.set = set;
		for( int i = 0; i < visible.length; i++ ) {
			if( visible[i] ) {
				numberOfVisibleElements++;
			}
		}
	}

	public HidingSetForThinFlow( IdentifiableCollection<E> set, boolean[] visible, int numberOfVisibleElements ) {
		this.visible = visible;
		this.numberOfVisibleElements = numberOfVisibleElements;
		this.set = set;
	}

	public void printVisible() {
		for( int i = 0; i < visible.length; i++ ) {
			System.out.print( " " + visible[i] + " " );
		}
	}

	public boolean contains( E element ) {
		return set.contains( element ) && visible[element.id()];
	}

	public boolean empty() {
		return numberOfVisibleElements() == 0;
	}

	public int size() {
		return set.size();
	}

	public E get( int id ) {
		return (!visible[id]) ? null : set.get( id );
	}

	public E removeLast() {
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	public void remove( E elem ) {
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	public boolean add( E elem ) {
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	public E first() {
		int index = 0;
		while( !visible[index] ) {
			index++;
			if( index == visible.length ) {
				return null;
			}
		}
		return get( index );
	}

	public E last() {
		int index = size() - 1;
		while( !visible[index] ) {
			index--;
			if( index == -1 ) {
				return null;
			}
		}
		return get( index );
	}

	public E random() {
		int r = new Random().nextInt( size() );
		for( int i = 0; i < visible.length; i++ ) {
			if( visible[i] ) {
				continue;
			}
			if( r == 0 ) {
				return get( i );
			}
			r--;
		}
		return null;
	}

	public E predecessor( E element ) {
		int index = element.id();
		do {
			index--;
			if( index == -1 ) {
				return null;
			}
		} while( !visible[index] );
		return get( index );
	}

	public E successor( E element ) {
		int index = element.id();
		do {
			index++;
			//System.out.println(index + " " + hidden.length);
			if( index == visible.length ) {
				return null;
			}
		} while( !visible[index] );
		return get( index );
	}

	public Iterator<E> iterator() {
		if( set instanceof RandomAccess ) {
			return new HidingIterator();
		} else {
			return new SequentialHidingIterator();
		}
	}

	public int numberOfVisibleElements() {
		return numberOfVisibleElements;
	}

	public boolean isVisible( E element ) {
		return visible[element.id()];
	}

	public void changeVisibility( E element, boolean visible ) {
		if( isVisible( element ) != visible ) {
			this.visible[element.id()] = visible;
			if( visible ) {
				numberOfVisibleElements++;
			} else {
				numberOfVisibleElements--;
			}
		}
	}

	/* public void setHidden(E element, boolean hidden) {
	 if (isHidden(element) != hidden) {
	 this.hidden[element.id()] = hidden;
	 if (hidden) {
	 numberOfHiddenElements++;
	 } else {
	 numberOfHiddenElements--;
	 }
	 }
	 }*/
	public int getCapacity() {
		return visible.length;
	}

	public void setCapacity( int capacity ) {
		boolean[] newHidden = new boolean[capacity];
		System.arraycopy( visible, 0, newHidden, 0, Math.min( visible.length, capacity ) );
		visible = newHidden;
	}

	public class HidingIterator implements Iterator<E> {

		private E current;
		private E next;

		public HidingIterator() {
		}

		public boolean hasNext() {
			if( next != null ) {
				return true;
			} else if( current == null && size() > 0 ) {
				next = first();
				return next != null;
			} else {
				next = successor( current );
				return next != null;
			}
		}

		public E next() {
			if( next == null ) {
				hasNext();
			}
			current = next;
			next = null;
			return current;
		}

		public void remove() {
			throw new UnsupportedOperationException( "Not supported." );
		}

	}

	public class SequentialHidingIterator implements Iterator<E> {

		private Iterator<E> setIterator;
		private E current;
		private E next;

		public SequentialHidingIterator() {
			setIterator = set.iterator();
		}

		public boolean hasNext() {
			while( setIterator.hasNext() ) {
				E element = setIterator.next();
				if( visible[element.id()] ) {
					next = element;
					return true;
				}
			}
			return false;
		}

		public E next() {
			if( next == null ) {
				hasNext();
			}
			current = next;
			next = null;
			return current;
		}

		public void remove() {
			throw new UnsupportedOperationException( "Not supported." );
		}

	}

}
