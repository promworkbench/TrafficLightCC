package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.Optional;

import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;

public abstract class InOutInLayoutableElement {

	/**
	 * Shared layout information.
	 */
	protected BaseLayoutInfo layoutInfo;

	/**
	 * Fix dimension (w, h).
	 */
	protected boolean fixDim;

	public InOutInLayoutableElement() {
		this.layoutInfo = new BaseLayoutInfo();
		this.fixDim = false;
	}

	/**
	 * Init the internal layout (in particular, width and height of the element)
	 */
	public abstract void initInternalLayout();
	
	public void processExternalDimensions(Optional<Float> wDesiredExt, Optional<Float> hDesiredExt) {
		// Ignore
	}
	
	public void finalPositionUpdate() {
		this.layoutInfo.x = 0;
		this.layoutInfo.y = 0;
	}
	
	
	public abstract void writeSVG(StringBuilder builder);
	
	public BaseLayoutInfo getLayoutInfo() {
		return this.layoutInfo;
	}
	
	public abstract void add2SVG(SVGElement svgEl) throws SVGElementException;
	
}
