package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Path;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Symbol:
 * 
 *			|--| 
 *			|  | 
 *			|  | 
 *      ====    ====  
 *      |           |
 *      ====    ====  
 *			|  | 
 *			|  | 
 *			|--| 
 */
public final class PicAdditional extends PicDiagnostic {
	
	public static float factor = 0.5f;
	
	private float a;
	
	private float aShort;

	public PicAdditional(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			Optional<Float> metricValue) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);

		float A = getPicArea();
		
		a = (float) Math.sqrt(1 / (4 * factor + factor * factor) * A);
		aShort = factor * a;
	}


	@Override
	public void initInternalLayout() {
		this.layoutInfo.h = 2 * a + factor * a;
		this.layoutInfo.w = this.layoutInfo.h;
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
		builder.append(String.format(Locale.ROOT, 
				"<path fill=\"%s\" d=\"m %f 0 h %f v %f h %f v %f h %f v %f h %f v %f h %f v %f h %f z\">\n",
				getColorPlot(),
				this.a,
				this.aShort, this.a,
				this.a, this.aShort,
				-1* this.a, this.a,
				-1 * this.aShort, -1 * this.a,
				-1 * this.a, -1 * this.aShort,
				this.a
				));
		builder.append("</g>\n");
	}


	@Override
	public String getColor() {
		return "orange";
	}


	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Path path = new Path();

		path.addAttribute("d", AnimationElement.AT_XML, 
				String.format(Locale.ROOT, "m %f 0 h %f v %f h %f v %f h %f v %f h %f v %f h %f v %f h %f z",
					this.a,
					this.aShort, this.a,
					this.a, this.aShort,
					-1* this.a, this.a,
					-1 * this.aShort, -1 * this.a,
					-1 * this.a, -1 * this.aShort,
					this.a
				)
			);
		
		super.applyColoring(path);
		
		super.applyIntraPicShift(svgEl, path);
		
	}

}
