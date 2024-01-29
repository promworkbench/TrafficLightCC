package org.processmining.trafficlightcc.visualization.graphviz;

import com.kitfox.svg.SVGElement;

public interface SVGElementManipulator <T extends SVGElement> {
	
	public void applySVGManipulation(T svgDiagram);

}
