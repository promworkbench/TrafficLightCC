package org.processmining.trafficlightcc.visualization.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.visualization.eventhandling.EventClassClickListener;
import org.processmining.trafficlightcc.visualization.eventhandling.EventClassSelectedEvent;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;
import org.processmining.trafficlightcc.visualization.trafficlight.TLMetricNormalization;
import org.processmining.trafficlightcc.visualization.trafficlight.TrafficLightPic;
import org.processmining.trafficlightcc.visualization.util.TL2Svg;
import org.processmining.trafficlightcc.visualization.util.VisualizationUtility;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGDisplayPanel;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;

public class TLCDiagnosticNonPNTLView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -650637205644258895L;


	/**
	 * Area of the pictogram for a unit metric.
	 */
	public static final float unitArea = 2500;

	/**
	 * If the metric is absent, scale down the unit Area by this factor and display. 
	 */
	public static final float defaultAbsentMetricFactor = 0.1f;

	/**
	 * If the metric is zero, scale down the unit Area by this factor and display. 
	 */
	public static final float defaultZeroMetricFactor = 0.1f;
	
	/**
	 * Inner separator for the traffic light (between the rectangle and the "lights")
	 */
	public static final float innerTLSep = 10; 

	/**
	 * Distance between the lights.
	 */
	public static final float lightPadding = 5; 

	/**
	 * Model providing the diagnostics data
	 */
	private final TLCDiagnosticsModel tlcDiagModel;

	/**
	 * Mapping transitions -> traffic light picture 
	 */
	private final Map<XEventClass, TrafficLightPic> pics4EventClasses;


	public TLCDiagnosticNonPNTLView(TLCDiagnosticsModel tlcDiagModel) {
		super();
		this.tlcDiagModel = tlcDiagModel;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		this.pics4EventClasses = new HashMap<>();
		this.initTLPics();
		
		////////////////////////////////////////
		// Init View
		////////////////////////////////////////
		// Layout
		this.setLayout(new BorderLayout());
		JLabel labelTitle = new JLabel("Additional Event Classes");
		// TODO explanation
		this.add(labelTitle, BorderLayout.PAGE_START);
		
		if(pics4EventClasses.isEmpty()) {
			JTextArea textNoAdd = new JTextArea(
					"All diagnostics can be mapped onto the model");
			textNoAdd.setFont(new Font("Serif", Font.ITALIC, 16));
			textNoAdd.setLineWrap(true);
			textNoAdd.setWrapStyleWord(true);
			textNoAdd.setOpaque(false);
			textNoAdd.setEditable(false);
			textNoAdd.setForeground(Color.WHITE);

			// We must wrap the text; otherwise we cannot shrink the split panel
			// TextArea can just extend
			JScrollPane wrapText = VisualizationUtility.wrapInProMStyleScrollPane(textNoAdd);
			wrapText.setOpaque(false);
			wrapText.setPreferredSize(new Dimension(50, 10));

			this.add(wrapText, BorderLayout.CENTER);
		}
		else {
			JPanel eventClassTLList = new JPanel();
			eventClassTLList.setBackground(Color.DARK_GRAY);
			eventClassTLList.setForeground(Color.DARK_GRAY);
			eventClassTLList.setLayout(new BoxLayout(eventClassTLList, BoxLayout.Y_AXIS));
			
			List<Entry<XEventClass, TrafficLightPic>> eventClassTLs = pics4EventClasses.entrySet()
				.stream()
				.sorted((e1, e2) -> e1.getKey().getId().compareTo(e2.getKey().getId()))
				.collect(Collectors.toList());
			
			for (Entry<XEventClass, TrafficLightPic> e : eventClassTLs) {
				TrafficLightPic pic = e.getValue();
				
				//////////////////////////////
				// Create SVG Pic Panel
				//////////////////////////////
				SVGDiagram svgDiagram;
				try {
					svgDiagram = TL2Svg.instantiateSVGDiagram(pic, 100f);
				} catch (SVGElementException | IOException e3) {
					e3.printStackTrace();
					continue;
				} catch (SVGException e3) {
					e3.printStackTrace();
					continue;
				}
				SVGDisplayPanel panelPic = new SVGDisplayPanel();
				panelPic.setDiagram(svgDiagram);
				panelPic.setPreferredSize(new Dimension((int) Math.ceil(pic.getLayoutInfo().getW()), 
						(int) Math.ceil(pic.getLayoutInfo().getH())));
				panelPic.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
					}
					
					@Override
					public void mouseClicked(MouseEvent event) {
						fireEventClassSelected(e.getKey());
						
					}
				});
				
				//////////////////////////////
				// Row Text
				//////////////////////////////
				JTextArea textEventClass = new JTextArea(
						e.getKey().getId());
				textEventClass.setFont(new Font("Serif", Font.ITALIC, 16));
				textEventClass.setLineWrap(true);
				textEventClass.setWrapStyleWord(true);
				textEventClass.setOpaque(false);
				textEventClass.setEditable(false);
				textEventClass.setForeground(Color.WHITE);

				// We must wrap the text; otherwise we cannot shrink the split panel
				// TextArea can just extend
				JScrollPane wrapText = VisualizationUtility.wrapInProMStyleScrollPane(textEventClass);
				wrapText.setOpaque(false);
				wrapText.setPreferredSize(new Dimension(100, 10));
				
				//////////////////////////////
				// Create Row
				//////////////////////////////
				JPanel panelRow = new JPanel();
				panelRow.setBackground(eventClassTLList.getBackground());
				panelRow.setLayout(new BoxLayout(panelRow, BoxLayout.X_AXIS));
				
				panelRow.add(panelPic);
				panelRow.add(wrapText);
				
				eventClassTLList.add(panelRow);
			}

			// Outer Scroll pane:
			JScrollPane wrapList = VisualizationUtility.wrapInProMStyleScrollPane(eventClassTLList);
			this.add(wrapList, BorderLayout.CENTER);
		}
	}	
	
	private void initTLPics() {
		for (Entry<XEventClass, TLCElementDiagnostic> entryTransElDiag : 
				tlcDiagModel.getNonTransitionAssignableEventClassDiagnostics().entrySet()) {
			// Traffic light
			TrafficLightPic pic = new TrafficLightPic(entryTransElDiag.getValue(), unitArea, defaultAbsentMetricFactor, 
					defaultZeroMetricFactor, innerTLSep, lightPadding, TLMetricNormalization.TOTAL);
			
			// Layout
			pic.layout();

			this.pics4EventClasses.put(entryTransElDiag.getKey(), pic);
		}
	}
	
	////////////////////////////////////////////////////////////
	// Handling Event Class Selection
	////////////////////////////////////////////////////////////
	public void addEventClassSelectedListener(EventClassClickListener listener) {
		listenerList.add(EventClassClickListener.class, listener);
	}

	public void removeEventClassSelectedListener(EventClassClickListener listener) {
		listenerList.remove(EventClassClickListener.class, listener);
	}

	protected void fireEventClassSelected(XEventClass evClass) {
		EventClassSelectedEvent evClassSelectEvent = null;
		 // Guaranteed to return a non-null array
		 Object[] listeners = listenerList.getListenerList();
		 // Process the listeners last to first, notifying
		 // those that are interested in this event
		 for (int i = listeners.length-2; i >= 0; i-=2) {
			 if (listeners[i] == EventClassClickListener.class) {
				 // Lazily create the event:
				 if (evClassSelectEvent == null)
					 evClassSelectEvent = new EventClassSelectedEvent(evClass);
				 ((EventClassClickListener) listeners[i+1]).eventClassClicked(evClassSelectEvent);
			 }
		 }
	 }
}
