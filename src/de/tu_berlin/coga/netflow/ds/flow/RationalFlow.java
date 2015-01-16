/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*
 * RationalFlow.java
 *
 */

package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.IdentifiableDoubleMapping;

/**
 *
 * @author Sebastian Schenker
 */

public class RationalFlow extends IdentifiableDoubleMapping<Edge> {

    public RationalFlow(IdentifiableDoubleMapping<Edge> flow) {
        super(flow);
    }

}
