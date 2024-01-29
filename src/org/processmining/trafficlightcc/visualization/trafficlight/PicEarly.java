package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;


/**
 * Symbol:
 *     -|
 *   -  |
 * -    |
 *   -  |
 *     -|
 * 
 */
public final class PicEarly extends PicDiagnosticBlockTriangle {
	
	public PicEarly(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue, PicOrientation.LEFT);
	}

	@Override
	public String getColor() {
		return "yellow";
	}
}
