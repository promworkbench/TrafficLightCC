package org.processmining.trafficlightcc.util.stopwatch;

import java.util.EnumMap;
import java.util.stream.Collectors;

public class PerformanceLogger {
	
	private EnumMap<StopwatchMilestone, TimeMeasurement> measurements;
	
	public PerformanceLogger() {
		this.measurements = new EnumMap<>(StopwatchMilestone.class);
	}
	
	public void startMeasurement(StopwatchMilestone milestone) {
		TimeMeasurement measurement = measurements.get(milestone);
		if (measurement == null) {
			measurement = new TimeMeasurement();
			measurements.put(milestone, measurement);
		}
		measurement.start();
	}
	
	public void pauseMeasurement(StopwatchMilestone milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.pause();
	}
	
	public void resumeMeasurement(StopwatchMilestone milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.resume();
	}

	public void stopMeasurement(StopwatchMilestone milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.stop();
	}
	
	public String toString() {
		return measurements.entrySet().stream()
			.map(e -> (e.getKey() + ": " + e.getValue().getTime()))
			.sorted()
			.collect(Collectors.joining(", ", "{", "}"));
	}

	private TimeMeasurement getMeasurement(StopwatchMilestone milestone) {
		TimeMeasurement measurement = measurements.get(milestone);
		if (measurement == null) {
			throw new IllegalStateException("No measurement " + milestone);
		}
		else {
			return measurement;
		}
		
	}
	
	public EnumMap<StopwatchMilestone, TimeMeasurement> getMeasurements() {
		return this.measurements;
	}

}
