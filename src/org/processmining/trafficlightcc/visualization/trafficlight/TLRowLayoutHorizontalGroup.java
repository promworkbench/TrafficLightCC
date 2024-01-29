package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

public class TLRowLayoutHorizontalGroup extends InOutInLayoutableElement {

	private final float elementPadding;

	private final List<InOutInLayoutableElement> elements;

	private float maxWContent;

	private float maxHContent;

	public TLRowLayoutHorizontalGroup(float elementPadding, List<InOutInLayoutableElement> elements) {
		this.elementPadding = elementPadding;
		this.elements = elements;
	}

	public void initInternalLayout() {
		// Init child Layouts
		this.elements.forEach(InOutInLayoutableElement::initInternalLayout);
		
		// Init width and height considering children and padding
		this.layoutInfo.w = this.elements.stream()
				.map(e -> e.layoutInfo.w)
				.reduce(0f, (res, v) -> res + v);
		// Paddings
		this.layoutInfo.w += (this.elements.size() - 1) * elementPadding;
		
		// Height: Maximum among all
		this.layoutInfo.h = this.elements.stream()
				.map(e -> e.layoutInfo.h)
				.max(Float::compare)
				.orElse(0f);
		
		this.maxWContent = this.layoutInfo.w;
		this.maxHContent = this.layoutInfo.h;
	}

	@Override
	public void processExternalDimensions(Optional<Float> wDesiredExt, Optional<Float> hDesiredExt) {
		this.layoutInfo.w = Math.max(this.layoutInfo.w, wDesiredExt.orElse(0f));
		this.layoutInfo.h = Math.max(this.layoutInfo.h, wDesiredExt.orElse(0f));
	}

	@Override
	public void finalPositionUpdate() {
		this.elements.forEach(InOutInLayoutableElement::finalPositionUpdate);

		this.layoutInfo.x = (this.layoutInfo.w - this.maxWContent);
		this.layoutInfo.y = (this.layoutInfo.h - this.maxHContent);
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
		float xEl = 0;
		for (InOutInLayoutableElement e : this.elements) {
			// Center child element in y direction 
			float yEl = (this.maxHContent - e.layoutInfo.h) / 2;
			builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", xEl, yEl));
			e.writeSVG(builder);
			builder.append("</g>\n");
			xEl += e.layoutInfo.w + this.elementPadding;
		}
		builder.append("</g>\n");
	}
	
	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Group groupRow = new Group();
		groupRow.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
		svgEl.loaderAddChild(null, groupRow);

		float xEl = 0;
		for (InOutInLayoutableElement e : this.elements) {
			// Center child element in y direction 
			float yEl = (this.maxHContent - e.layoutInfo.h) / 2;
			Group rowElContainer = new Group();
			rowElContainer.addAttribute("transform", AnimationElement.AT_XML, 
						String.format(Locale.ROOT, "translate(%f, %f)", xEl, yEl));
			groupRow.loaderAddChild(null, rowElContainer);
			e.add2SVG(rowElContainer);
			xEl += e.layoutInfo.w + this.elementPadding;
		}
	}
}
