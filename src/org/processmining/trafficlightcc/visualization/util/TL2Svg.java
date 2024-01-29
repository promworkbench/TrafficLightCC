package org.processmining.trafficlightcc.visualization.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

import org.processmining.trafficlightcc.visualization.trafficlight.TrafficLightPic;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

public class TL2Svg {
	
	public static SVGDiagram instantiateSVGDiagram(TrafficLightPic pic, float svgHeight) throws IOException, SVGException {
		SVGUniverse svgUniverse = new SVGUniverse();
		StringBuilder svgBuilder = new StringBuilder();
		
		float svgAspect = pic.getLayoutInfo().getW() / pic.getLayoutInfo().getH();
		float svgWidth = svgAspect * svgHeight;
		
		//////////////////////////////
		// Create Baseline SVG String
		//////////////////////////////
		svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		svgBuilder.append(String.format(Locale.ROOT, "<svg version=\"1.1\" width=\"%fpt\" height=\"%fpt\" "
				+ "viewBox=\"0 0 %f %f\" "
				+ "xmlns=\"http://www.w3.org/2000/svg\">\n", 
				svgWidth, svgHeight,
				pic.getLayoutInfo().getW(), pic.getLayoutInfo().getH()));
		svgBuilder.append("</svg>");
		String svgBaseString = svgBuilder.toString();
		
		// String -> input stream -> SVG Universe load
		InputStream svgStream = new ByteArrayInputStream(svgBaseString.getBytes());
		URI uriPic = svgUniverse.loadSVG(svgStream, "Traffic Light Pic");
		
		// Add traffic light pic to SVG picture
		SVGDiagram svgDiag = svgUniverse.getDiagram(uriPic);
		pic.add2SVG(svgDiag.getRoot());
		
		svgDiag.updateTime(0);
				
		return svgDiag;
	}

}
