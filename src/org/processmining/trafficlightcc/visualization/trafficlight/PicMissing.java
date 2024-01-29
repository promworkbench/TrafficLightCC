package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Symbol:
 *      ============  
 *      |           |
 *      ============  
 */
public final class PicMissing extends PicDiagnostic {
	
	public static float factor = 0.25f;
	
	private float a;
	
	private float b;
	
	public PicMissing(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
		
		float A = getPicArea();
		
		a = (float) Math.sqrt((1 / factor) * A);
		b = factor * a;
	}


	@Override
	public void initInternalLayout() {
		this.layoutInfo.h = b;
		this.layoutInfo.w = a;
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
		builder.append(String.format(Locale.ROOT, "<rect fill=%s x=\"0\" y=\"0\" width=\"%f\" height=\"%f\"/>\n",
				getColor(),
				this.a, this.b));
		builder.append("</g>\n");
	}

	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Rect rect = new Rect();
		super.applyColoring(rect);

		rect.addAttribute("x", AnimationElement.AT_XML, Float.toString(0f));
		rect.addAttribute("y", AnimationElement.AT_XML, Float.toString(0f));
		rect.addAttribute("width", AnimationElement.AT_XML, Float.toString(this.a));
		rect.addAttribute("height", AnimationElement.AT_XML, Float.toString(this.b));
		
		super.applyIntraPicShift(svgEl, rect);
	}

	@Override
	public String getColor() {
		return "red";
	}
}
