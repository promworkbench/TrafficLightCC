package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

public class TLRowLayoutVerticalGroup extends InOutInLayoutableElement {

	private final float elementPadding;

	private final List<InOutInLayoutableElement> elements;
	
	private float maxWContent;

	private float maxHContent;

	public TLRowLayoutVerticalGroup(float elementPadding, List<InOutInLayoutableElement> elements) {
		this.elementPadding = elementPadding;
		this.elements = elements;
	}

	public void initInternalLayout() {
		// Init child Layouts
		this.elements.forEach(InOutInLayoutableElement::initInternalLayout);
		
		// Width: Maximum among all
		this.layoutInfo.w = this.elements.stream()
				.map(e -> e.layoutInfo.w)
				.max(Float::compare)
				.orElse(0f);
		
		// Height: Sum of children's height + padding
		this.layoutInfo.h = this.elements.stream()
				.map(e -> e.layoutInfo.h)
				.reduce(0f, (res, v) -> res + v); 
		// Paddings
		this.layoutInfo.h += (this.elements.size() - 1) * elementPadding;
		
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
		float yEl = this.layoutInfo.y;
		for (InOutInLayoutableElement e : this.elements) {
			// Center child element in x direction 
			float xEl = (this.maxWContent - e.layoutInfo.w) / 2;
			builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", xEl, yEl));
			e.writeSVG(builder);
			builder.append("</g>\n");
			yEl += e.layoutInfo.h + this.elementPadding;
		}
		builder.append("</g>\n");
	}

	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Group groupRow = new Group();
		groupRow.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
		svgEl.loaderAddChild(null, groupRow);

		float yEl = this.layoutInfo.y;
		for (InOutInLayoutableElement e : this.elements) {
			// Center child element in x direction 
			float xEl = (this.maxWContent - e.layoutInfo.w) / 2;
			Group rowElContainer = new Group();
			rowElContainer.addAttribute("transform", AnimationElement.AT_XML, 
						String.format(Locale.ROOT, "translate(%f, %f)", xEl, yEl));
			groupRow.loaderAddChild(null, rowElContainer);
			e.add2SVG(rowElContainer);
			yEl += e.layoutInfo.h + this.elementPadding;
		}
	}


}
