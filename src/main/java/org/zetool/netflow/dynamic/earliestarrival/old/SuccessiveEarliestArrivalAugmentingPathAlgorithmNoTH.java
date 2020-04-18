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
package org.zetool.netflow.dynamic.earliestarrival.old;

import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import org.zetool.netflow.dynamic.problems.DynamicTransshipmentProblem;
import org.zetool.netflow.dynamic.transshipment.TransshipmentFramework;

/**
 * This class calculates an earliest arrival transshipment
 * by using a successive earliest arrival augmenting path algorithm.
 * The optimal time horizon is found as specified in {@code TransshipmentFramework}. 
 */
public class SuccessiveEarliestArrivalAugmentingPathAlgorithmNoTH extends TransshipmentFramework<EarliestArrivalFlowProblem,SuccessiveEarliestArrivalAugmentingPathAlgorithmTH> {

	public SuccessiveEarliestArrivalAugmentingPathAlgorithmNoTH() {
		super( new SuccessiveEarliestArrivalAugmentingPathAlgorithmTH() );
	}
	
}
