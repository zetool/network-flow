/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.zetool.netflow.dynamic;

/**
 * Provides upper and lower bounds for the needed time horizon.
 * @author Martin Groß
 */
public class TimeHorizonBounds {
  /** The lower bound. */
  private int lowerBound;
  /** The upper bound. */
  private int upperBound;

  /**
   * Initializes an interval for the time horizon.
   * @param lowerBound the lower bound for the time horizon
   * @param upperBound the upper bound for the time horizon
   * @throws IllegalArgumentException if lower bound is larger than the upper bound
   */
  public TimeHorizonBounds( int lowerBound, int upperBound ) throws IllegalArgumentException {
    if( lowerBound > upperBound ) {
      throw new IllegalArgumentException( "Upper bound (" + upperBound
              + ") must be larger than lower bound (" + lowerBound + ")!" );
    }
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public int getLowerBound() {
    return lowerBound;
  }

  public int getUpperBound() {
    return upperBound;
  }

  @Override
  public String toString() {
    return "[" + lowerBound + "," + upperBound + "]";
  }
}
