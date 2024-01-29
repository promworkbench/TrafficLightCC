package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;


/**
 * Symbol
 * |-
 * |  -
 * |    -
 * |  -
 * |-
 * 
 * @author brockhoff
 *
 */
public final class PicLate extends PicDiagnosticBlockTriangle {

	public PicLate(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue, PicOrientation.RIGHT);
	}

	@Override
	public String getColor() {
		return "yellow";
	}
}
