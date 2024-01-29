package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Circle;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

public abstract class PicDiagnosticCircle extends PicDiagnostic {

	/**
	 * Radius
	 */
	protected float r;

	public PicDiagnosticCircle(float maxArea, float defaultAreaAbsentMetric, 
			float defaultAreaZeroMetric, Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
		
		initCircle();
	}

	@Override
	public void initInternalLayout() {
		// Radius of the circle so that area is (metric/default) * maxArea
		
		this.layoutInfo.h = 2 * r;
		this.layoutInfo.w = 2 * r;
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		String color = this.getColorPlot();
		builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
		builder.append(String.format(Locale.ROOT, "<circle cx=\"%f\" cy=\"%f\" r=\"%f\" fill=\"%s\"/>\n", 
				this.r, // center x
				this.r, // center y
				this.r,  // radius
				color)); 
		builder.append("</g>\n");
	}
	
	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Circle circle = new Circle();
		super.applyColoring(circle);

		circle.addAttribute("cx", AnimationElement.AT_XML, Float.toString(this.r));
		circle.addAttribute("cy", AnimationElement.AT_XML, Float.toString(this.r));
		circle.addAttribute("r", AnimationElement.AT_XML, Float.toString(this.r));

		super.applyIntraPicShift(svgEl, circle);
		
	}

	/**
	 * The color for this triangle if the metric is present and non-zero.
	 * @return Color string
	 */
	public abstract String getColor();

	public void initCircle() {
		// Height of the triangle so that area is (metric/default) * maxArea
		if (!this.metricValue.isPresent()) {
			this.r =  (float) Math.sqrt((defaultAreaAbsentMetric * this.maxArea) / Math.PI);
		}
		else if (this.metricValue.get().compareTo(0f) == 0) {
			this.r =  (float) Math.sqrt((defaultAreaZeroMetric * this.maxArea) / Math.PI);
		}
		else {
			this.r = (float) Math.sqrt((this.metricValue.get() * this.maxArea) / Math.PI);
		}
	}
	
}
