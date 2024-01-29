package org.processmining.trafficlightcc.visualization.graphviz;

import java.util.Map;
import java.util.Map.Entry;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

/**
 * Extends the {@link DotPanel} by functionality to add SVG elements to existing dot elements.
 * 
 * @author brockhoff
 *
 */
public class DotPanelNodePictures extends DotPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7741925432650089678L;
	
	private Map<DotElement, SVGElementManipulator<Group>> dotElementManipulations;

	public DotPanelNodePictures(Dot dot, Map<DotElement, SVGElementManipulator<Group>> dotElementManipulations) {
		super(dot);
		
		this.dotElementManipulations = dotElementManipulations;
		// Apply additional svg manipulations to the initial image
		// Required! Doing this in the changeDot method only is not sufficient.
		this.applySVGManipultions(image, dotElementManipulations);
		
		try {
			image.updateTime(0);
		} catch (SVGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void applySVGManipultions(SVGDiagram svgDiagram, 
			Map<DotElement, SVGElementManipulator<Group>> dotElementManipulations) {

		for (Entry<DotElement,SVGElementManipulator<Group>> entry : dotElementManipulations.entrySet()) {
			////////////////////
			// Retrieve Group element and manipulate
			////////////////////
			SVGElement svgEl = svgDiagram.getElement(entry.getKey().getId());
			if (svgEl == null) {
				//		entry.getKey().getId(), entry.getKey().getLabel());
			}
			else if (!(svgEl instanceof Group)) {
				//		entry.getKey().getId());
			}
			else {
				// Apply Manipulation
				entry.getValue().applySVGManipulation((Group) svgEl);
			}
		}
	}

	@Override
	public void changeDot(Dot dot, SVGDiagram diagram, boolean resetView) {
		// Apply additional svg manipulations
		this.applySVGManipultions(diagram, dotElementManipulations);
		super.changeDot(dot, diagram, resetView);
	}
	
	public void changeContent(Dot dot, Map<DotElement, SVGElementManipulator<Group>> dotElementManipulations) {
		this.dotElementManipulations = dotElementManipulations;
		this.changeDot(dot, true);
	}
	
}
