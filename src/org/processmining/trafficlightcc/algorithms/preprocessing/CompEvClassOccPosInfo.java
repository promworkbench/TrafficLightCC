package org.processmining.trafficlightcc.algorithms.preprocessing;

import java.util.Comparator;

public class CompEvClassOccPosInfo implements Comparator<EvClassOccPosTieBreakInfo> {

	@Override
	public int compare(EvClassOccPosTieBreakInfo o1, EvClassOccPosTieBreakInfo o2) {
		
		// 1. Sort on number of occurrences
		// Higher importance if more cases with an occurrence
		int res = Integer.compare(o1.nbrOcc(), o2.nbrOcc());
		
		// 2. Sort on average first occurrence position in trace
		// Higher importance if early in trace
		if (res == 0) {
			res = -1 * Float.compare(o1.avgFirstOccInTrace(), o2.avgFirstOccInTrace());
		}

		// 3. Sort on lexicographic activity name
		// Higher importance if lexicographically smaller
		if (res == 0) {
			res = -1 * o1.eventClass().toString().compareTo(o2.eventClass().toString());
		}
		return res;
	}

}
