package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

public abstract class PicDiagnostic extends InOutInLayoutableElement {
	
	
	/**
	 * The associated ("normalized" metric value)
	 * Normalization is assumed to be one for one occurrence per trace.
	 * Can be greater than one if element is used more frequently.
	 * 
	 */
	protected Optional<Float> metricValue;
	
	/**
	 * Area unit metric.
	 */
	protected float maxArea;
	
	/**
	 *  Default factor for the pic's area if there is no metric value.
	 */
	protected float defaultAreaAbsentMetric;
	
	/**
	 *  Default factor for the pic's area if the metric is zero.
	 */
	protected float defaultAreaZeroMetric;
	
	public PicDiagnostic(float maxArea, float defaultAreaAbsentMetric,  float defaultAreaZeroMetric,
			Optional<Float> metricValue) {
		this.maxArea = maxArea;
		this.defaultAreaAbsentMetric = defaultAreaAbsentMetric;
		this.defaultAreaZeroMetric = defaultAreaZeroMetric;
		this.metricValue = metricValue;
		this.layoutInfo = new BaseLayoutInfo();
	}
	
	public String getColorPlot() {
		if (!this.metricValue.isPresent()) {
			//return "red";
			return "gray";
		}
		else if (this.metricValue.get().compareTo(0f) == 0) {
			return "gray";
		}
		else {
			return this.getColor();
		}
	}
	
	public float getFillOpacity() {
		if (!this.metricValue.isPresent()) {
			//return 1f;
			return 0f;
		}
		else if (this.metricValue.get().compareTo(0f) == 0) {
			return 0f;
		}
		else {
			return  0.5f + 0.5f * ((float) Math.min(this.metricValue.get(), 1.0));
		}
	}
	
	/**
	 * The color for this triangle if the metric is present and non-zero.
	 * @return Color string
	 */
	public abstract String getColor();
	
	protected void applyIntraPicShift(SVGElement root, SVGElement pic) throws SVGElementException {
		// Shift required
		if((Float.compare(this.layoutInfo.x, 0) == 0) || (Float.compare(this.layoutInfo.y, 0) == 0)) {
			root.loaderAddChild(null, pic);
		}
		else {
			Group g = new Group();
			g.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
			root.loaderAddChild(null, g);
			g.loaderAddChild(null, pic);
		}
	}
	
	protected void applyIntraPicShift(SVGElement root, SVGElement pic, Group addPicTransform) throws SVGElementException {
		// Shift required
		if((Float.compare(this.layoutInfo.x, 0) == 0) || (Float.compare(this.layoutInfo.y, 0) == 0)) {
			root.loaderAddChild(null, addPicTransform);
			addPicTransform.loaderAddChild(null, pic);
		}
		else {
			Group g = new Group();
			g.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
			root.loaderAddChild(null, g);
			g.loaderAddChild(null, addPicTransform);
			addPicTransform.loaderAddChild(null, pic);
		}
	}
	
	protected float getPicArea() {
		float A;
		// Height of the triangle so that area is (metric/default) * maxArea
		if (!this.metricValue.isPresent()) {
			A = this.maxArea * defaultAreaAbsentMetric;
		}
		else if (this.metricValue.get().compareTo(0f) == 0) {
			A = this.maxArea * defaultAreaZeroMetric;
		}
		else {
			A = this.maxArea * this.metricValue.get();
		}
		
		return A;
	}
	
	protected void applyColoring(SVGElement svgEl) throws SVGElementException {

		String color = this.getColorPlot();
		float fillOpacity = this.getFillOpacity();

		svgEl.addAttribute("fill", AnimationElement.AT_XML, color);
		svgEl.addAttribute("fill-opacity", AnimationElement.AT_XML, Float.toString(fillOpacity));
	}

}
