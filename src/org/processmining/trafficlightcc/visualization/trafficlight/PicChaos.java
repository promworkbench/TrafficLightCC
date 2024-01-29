package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;


/**
 * Symbol:
 * Circle
 */
public final class PicChaos extends PicDiagnosticCircle {

	public PicChaos(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric,
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
	}

	@Override
	public String getColor() {
		return "orange";
	}

}
