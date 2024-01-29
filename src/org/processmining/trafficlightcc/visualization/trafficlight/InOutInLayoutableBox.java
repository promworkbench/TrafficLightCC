package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Locale;
import java.util.Optional;


import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

public class InOutInLayoutableBox extends InOutInLayoutableElement {
	
	protected ContentAlignment contentAlignment;
	
	protected Optional<InOutInLayoutableElement> contentEl;

	public InOutInLayoutableBox(Optional<InOutInLayoutableElement> contentEl) {
		this.contentAlignment = ContentAlignment.CENTER;
		this.contentEl = contentEl;
		
	}

	@Override
	public void initInternalLayout() {
		// Only Update if dimension is not fixed yet
		if (!fixDim) {
			if (contentEl.isPresent()) {
				this.contentEl.get().initInternalLayout();
				this.layoutInfo.w = contentEl.get().layoutInfo.w;
				this.layoutInfo.h = contentEl.get().layoutInfo.h;
			}
			else {
				this.layoutInfo.w = 0f;
				this.layoutInfo.h = 0f;
			}
		}
	}

	@Override
	public void processExternalDimensions(Optional<Float> wDesiredExt, Optional<Float> hDesiredExt) {
		if (contentEl.isPresent()) {
			this.layoutInfo.w = Math.max(contentEl.get().layoutInfo.w, wDesiredExt.orElse(0f));
			this.layoutInfo.h = Math.max(contentEl.get().layoutInfo.h, hDesiredExt.orElse(0f));
		}
		else {
			this.layoutInfo.w = wDesiredExt.orElse(0f);
			this.layoutInfo.h = hDesiredExt.orElse(0f);
		}
	}

	@Override
	public void finalPositionUpdate() {
		if (this.contentEl.isPresent()) {
			float wContent = this.contentEl.get().layoutInfo.w;
			float hContent = this.contentEl.get().layoutInfo.h;
			switch (this.contentAlignment) {
			case BOTTOM:
				// Center x direction
				// Left upper corner of content + height will be bottom
				this.layoutInfo.x = (this.layoutInfo.w - wContent) / 2;
				this.layoutInfo.y = this.layoutInfo.h - hContent;
				break;
			case CENTER:
				// Center x direction
				this.layoutInfo.x = (this.layoutInfo.w - wContent) / 2;
				// Center y direction
				this.layoutInfo.y = (this.layoutInfo.h - hContent) / 2;
				break;
			case LEFT:
				this.layoutInfo.x = 0;
				// Center y direction
				this.layoutInfo.y = (this.layoutInfo.h - hContent) / 2;
				break;
			case RIGHT:
				this.layoutInfo.x = this.layoutInfo.w - wContent;
				// Center y direction
				this.layoutInfo.y = (this.layoutInfo.h - hContent) / 2;
				break;
			case TOP:
				this.layoutInfo.x = (this.layoutInfo.w - wContent) / 2;
				// Center y direction
				this.layoutInfo.y = 0f;
				break;
			default:
				this.layoutInfo.x = 0f;
				this.layoutInfo.y = 0f;
				break;
			}
		}
		else {
			this.layoutInfo.x = 0;
			this.layoutInfo.y = 0;
		}
	}

	@Override
	public void writeSVG(StringBuilder builder) {
		// Empty boxes will not be rendered
		if (contentEl.isPresent()) {
			builder.append(String.format(Locale.ROOT, "<g transform=\"translate(%f, %f)\">\n", this.layoutInfo.x, this.layoutInfo.y));
			contentEl.get().writeSVG(builder);
			builder.append("</g>\n");
		}
	}

	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		if (contentEl.isPresent()) {
			// Create child tree
			
			// Add transformation
			Group g = new Group();
			g.addAttribute("transform", AnimationElement.AT_XML, 
					String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
			svgEl.loaderAddChild(null, g);

			contentEl.get().add2SVG(g);
		}
	}

}
