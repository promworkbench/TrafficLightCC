package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Group;
import com.kitfox.svg.Path;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Symbol 
 * 
 * |==========|-
 * |          |  -
 * |          |    -
 * |          |  -
 * |==========|-
 * @author brockhoff
 *
 */
public abstract class PicDiagnosticBlockTriangle extends PicDiagnostic {
	
	/**
	 * Factor between base line length and length of short of the rectangle
	 */
	public static float f1 = 0.3f;

	/**
	 * Factor between short of the rectangle and height of the triangle
	 */
	public static float f2 = 0.3f;
	
	/**
	 * Baseline length
	 */
	private float a;
	
	/**
	 * Length short side of rectangle
	 */
	private float b;
	
	/**
	 * Height of triangle
	 */
	private float h;
	
	private PicOrientation orient;

	public PicDiagnosticBlockTriangle(float maxArea, float defaultAreaAbsentMetric, float defaultAreaZeroMetric,
			Optional<Float> metricValue, PicOrientation orient) {
		super(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, metricValue);
		
		this.orient = orient;

		float A = getPicArea();
		
		this.a = (float) Math.sqrt(1 / (f1 + 0.5 * f1 * f2) * A);
		this.b = f1 * a;
		this.h = f2 * b;
	}	@Override

	public void initInternalLayout() {

		switch(this.orient) {
		case LEFT:
		case RIGHT:
			this.layoutInfo.h = a;
			this.layoutInfo.w = b + h;
			break;
		case BOTTOM:
		case TOP:
			this.layoutInfo.h = b + h;
			this.layoutInfo.w = a;
			break;
		default:
			break;
		
		}
	}	
	
	@Override
	public void writeSVG(StringBuilder builder) {
		builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
		builder.append(String.format(Locale.ROOT, "<path fill=\"%s\" d=\"m 0 0 h %f l %f %f l %f %f h %f z\">\n",
					getColor(),
					this.b,
					this.h, 0.5 * this.a,
					-1 * this.h, 0.5 * this.a,
					-1* this.b
				)
			);
		builder.append("</g>\n");
	}
	
	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Path path = new Path();
		super.applyColoring(path);

		path.addAttribute("d", AnimationElement.AT_XML, 
				String.format(Locale.ROOT, "m 0 0 h %f l %f %f l %f %f h %f z",
					this.b,
					this.h, 0.5 * this.a,
					-1 * this.h, 0.5 * this.a,
					-1* this.b
				)
			);
		

		if (this.orient != PicOrientation.RIGHT) {
			float angle = 0f;
			switch (this.orient) {
				case RIGHT:
					// That's the default; unreachable because of if
					break;
				case BOTTOM:
					angle = 90;
					break;
				case LEFT:
					angle = 180;
					break;
				case TOP:
					angle = 270;
					break;
				default:
					break;
			}
			Group g = new Group();
			g.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "rotate(%f, %f, %f)", angle, (this.b + this.h) / 2 , this.a / 2));
			super.applyIntraPicShift(svgEl, path, g);
		}
		else {
			super.applyIntraPicShift(svgEl, path);
		}
		
	}



}
