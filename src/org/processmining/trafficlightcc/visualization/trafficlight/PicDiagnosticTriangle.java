package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;


import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;

public final class PicDiagnosticTriangle extends PicDiagnostic {

	/**
	 * Length of the triangle baseline.
	 */
	protected float lenBaseline;
	
	/**
	 * Height of the triangle.
	 */
	protected float h;
	
	public PicDiagnosticTriangle(float maxArea, 
			float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
	
		initTriangle();
	}
	
	public void initTriangle() {
		// For metric value 1, we want an equilateral triangle.
		this.lenBaseline = (float) Math.sqrt(maxArea * 4 / Math.sqrt(2)) ;

		// Height of the triangle so that area is (metric/default) * maxArea
		if (!this.metricValue.isPresent()) {
			this.h = (2 * (this.maxArea * defaultAreaAbsentMetric)) / this.lenBaseline;
		}
		else if (this.metricValue.get().compareTo(0f) == 0) {
			this.h = (2 * (this.maxArea * defaultAreaZeroMetric)) / this.lenBaseline;
		}
		else {
			this.h = (2 * (this.maxArea * this.metricValue.get())) / this.lenBaseline;
		}
		
		if (Float.isNaN(this.h)) {
		}
	}

	@Override
	public String getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initInternalLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		// TODO Auto-generated method stub
		
	}
	
	
}
