package org.processmining.trafficlightcc.util.datastat;

public class EventClassAggStat {
	
	/**
	 * Count of aggregated occurrences.
	 * (Must not be equal to total number of occurrences (e.g., if only one occurrence 
	 * per case is kept))
	 */
	protected int count;
	
	/**
	 * Sum of all positions
	 */
	protected int sumPos;
	
	/**
	 * Sum of all relative positions
	 */
	protected float sumPosRel;
	
	public EventClassAggStat() {
		this.count = 0;
		this.sumPos = 0;
		this.sumPosRel = 0;
	}
	
	public synchronized void consume(EventOccurrenceInfo evOcc) {
		this.count++;
		this.sumPos += evOcc.occPosition();
		this.sumPosRel += evOcc.occPositionRel();
	}

	public int getCount() {
		return count;
	}

	public int getSumPos() {
		return sumPos;
	}

	public float getSumPosRel() {
		return sumPosRel;
	}
	
	public float getAvgPos() {
		return ((float) sumPos) / count;
	}

	public float getAvgPosRel() {
		return ((float) sumPosRel) / count;
	}

	@Override
	public String toString() {
		return "EventClassAggStat [count=" + count + ", sumPos=" + sumPos + ", sumPosRel=" + sumPosRel +
				", avgPos=" + this.getAvgPos() + ", avgPosRel=" + this.getAvgPosRel()
				+ "]";
	}

}
