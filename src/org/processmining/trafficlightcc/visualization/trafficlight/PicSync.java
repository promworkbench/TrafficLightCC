package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;


/**
 * Symbol:
 * Circle
 */
public final class PicSync extends PicDiagnosticCircle {

	public PicSync(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric,
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getColor() {
		return "green";
	}
	
}
