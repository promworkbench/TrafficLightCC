package org.processmining.trafficlightcc.visualization.views;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.util.PrintUtil;
import org.processmining.trafficlightcc.visualization.eventhandling.TransitionClickListener;
import org.processmining.trafficlightcc.visualization.eventhandling.TransitionSelectedEvent;
import org.processmining.trafficlightcc.visualization.eventhandling.TransitionSelectedEventImpl;
import org.processmining.trafficlightcc.visualization.graphviz.DotPanelNodePictures;
import org.processmining.trafficlightcc.visualization.graphviz.SVGElementManipulator;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;
import org.processmining.trafficlightcc.visualization.trafficlight.InOutInLayoutableElement;
import org.processmining.trafficlightcc.visualization.trafficlight.TLMetricNormalization;
import org.processmining.trafficlightcc.visualization.trafficlight.TrafficLightPic;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class TLCDiagnosticPNView extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4636687860352975506L;

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
	 * Reference to the visualization model.
	 */
	private final TLCDiagnosticsModel tlcDiagModel;
	
	/**
	 * Mapping transitions -> traffic light picture 
	 */
	private final Map<Transition, TrafficLightPic> pics4Transitions;
	
	/**
	 * Handle to the actual ProM dot panel
	 */
	private DotPanelNodePictures dotPanel;
	
	private TLMetricNormalization tlNormMethod;
	
	private JRadioButton rBtnTotal;

	private JRadioButton rBtnRelative;
	
	public TLCDiagnosticPNView(TLCDiagnosticsModel tlcDiagModel) {
		super();
		this.setLayout(new BorderLayout());

		this.setPreferredSize(new Dimension(600, 400));

		this.tlcDiagModel = tlcDiagModel;
		this.pics4Transitions = new HashMap<>();
		this.tlNormMethod = TLMetricNormalization.RELATIVE;
		
		this.initTLPics();
		this.dotPanel = createDotNodePicturePanel();
		this.add(dotPanel, BorderLayout.CENTER);
		
		//////////////////////////////
		// Config Panel
		//////////////////////////////
		// init instance
		SlickerFactory slickerFactory = SlickerFactory.instance();
		JPanel configPanel = new JPanel();
		
		double mainPanelSize[][] = { { TableLayoutConstants.FILL }, { 40, 30, 30 } };
		configPanel.setLayout(new TableLayout(mainPanelSize));

		// Background color
		configPanel.setBackground(new Color(200, 200, 200));
		
		configPanel.add(slickerFactory.createLabel(
				"<html><h2>Normalization</h1>"),
			"0, 0, l, t");
		
		rBtnTotal = slickerFactory.createRadioButton("Log");
		rBtnRelative = slickerFactory.createRadioButton("Relative");
		rBtnTotal.addItemListener(this);
		rBtnRelative.addItemListener(this);

		rBtnRelative.setSelected(true);
		rBtnTotal.setSelected(false);
		//DefActListener defaultAction = new DefActListener(context, fitnessRB, fitnessCompleteRB, net, log, mapping);

		ButtonGroup rGroupNorm = new ButtonGroup();
		rGroupNorm.add(rBtnTotal);
		rGroupNorm.add(rBtnRelative);
		configPanel.add(rBtnTotal, "0, 1, l, t");
		configPanel.add(rBtnRelative, "0, 2, l, t");

		this.add(configPanel, BorderLayout.EAST);
		
	}
	
	private void initTLPics() {
		for (Entry<Transition, TLCElementDiagnostic> entryTransElDiag : 
				tlcDiagModel.getTransitionAssignableDiagnostics().entrySet()) {
			// Traffic light
			TrafficLightPic pic = new TrafficLightPic(entryTransElDiag.getValue(), unitArea, defaultAbsentMetricFactor, 
					defaultZeroMetricFactor, innerTLSep, lightPadding, this.tlNormMethod);
			
			// Layout
			pic.layout();

			this.pics4Transitions.put(entryTransElDiag.getKey(), pic);
		}
	}
	
	private DotPanelNodePictures createDotNodePicturePanel() {
		Pair<Dot, Map<DotElement, SVGElementManipulator<Group>>> content = 
				this.createDotSVGContent();
		return new DotPanelNodePictures(content.getLeft(), content.getRight());
	}
	
	private Pair<Dot, Map<DotElement, SVGElementManipulator<Group>>> createDotSVGContent() {
		////////////////////////////////////////////////////////////
		// Create Dot Image
		////////////////////////////////////////////////////////////
		Dot dot = new Dot();
		
		dot.setOption("forcelabels", "true");

		////////////////////
		// Places
		////////////////////
		Map<Place, DotNode> places2dotNode = new HashMap<>();
		for (Place p : this.tlcDiagModel.getPetriNet().getPlaces()) {
			DotNode dotNode = dot.addNode("");
			dotNode.setOption("shape", "circle");
			places2dotNode.put(p, dotNode);

			// TODO initial marking
//			if (pn.isInInitialMarking(place) > 0) {
//				dotNode.setOption("style", "filled");
//				dotNode.setOption("fillcolor", "#80ff00");
//			}
		}

		////////////////////
		// Transitions
		////////////////////
		Map<DotElement, SVGElementManipulator<Group>> postTrafficLightAdding = new HashMap<>();
		for (Transition t : this.tlcDiagModel.getPetriNet().getTransitions()) {
			DotNode dotNode;

			if (t.isInvisible()) {
				dotNode = dot.addNode("");
				dotNode.setOption("shape", "box");
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "black");
			} else {
				dotNode = dot.addNode(t.getLabel());
				dotNode.setOption("shape", "box");
				dotNode.setOption("imagepos", "bc");
				dotNode.setOption("labelloc", "t");
				// For the click listener
				dotNode.setOption("fillcolor", "white");
				TrafficLightPic pic = this.pics4Transitions.get(t);
				
				dotNode.setOption("height", Float.toString((35 + pic.getLayoutInfo().getH()) * 0.0138889f));
				dotNode.setOption("width", Float.toString(pic.getLayoutInfo().getW() * 0.0138889f));
				
				dotNode.addSelectionListener(new DotElementSelectionListener() {
					
					@Override
					public void selected(DotElement element, SVGDiagram image) {
						fireTransitionSelected(t);
					}
					
					@Override
					public void deselected(DotElement element, SVGDiagram image) {
						
					}
				});
				
				postTrafficLightAdding.put(dotNode, createSVGManipulatorAddPictureUnderText(pic));
			}

			////////////////////
			// Adjacent Edges 
			////////////////////
			this.tlcDiagModel.getPetriNet().getInEdges(t).stream()
				.map(PetrinetEdge::getSource)
				.forEach(p -> dot.addEdge(places2dotNode.get(p), dotNode));

			this.tlcDiagModel.getPetriNet().getOutEdges(t).stream()
				.map(PetrinetEdge::getTarget)
				.forEach(p -> dot.addEdge(dotNode, places2dotNode.get(p)));

		}
		
		
		return Pair.of(dot, postTrafficLightAdding);
		
	}
	
	public SVGElementManipulator<Group> createSVGManipulatorAddPictureUnderText(final InOutInLayoutableElement pic) {
		return new SVGElementManipulator<Group>() {

			@Override
			public void applySVGManipulation(Group svgElement) {
				Text nodeLabel = (Text) svgElement.getChild(2);
				StyleAttribute attrib = new StyleAttribute("x");
				try {
					// By default, the test anchor is middle bottom
					nodeLabel.getStyle(attrib);
					float xText = attrib.getFloatValue();
					attrib = new StyleAttribute("y");
					nodeLabel.getStyle(attrib);
					float yText = attrib.getFloatValue();
					
					float x = xText - (pic.getLayoutInfo().getW() / 2f);
					float y = yText + 5;

					Group g = new Group();
					g.addAttribute("transform", AnimationElement.AT_XML, 
							String.format(Locale.ROOT, "translate(%f, %f)", x, y));
					svgElement.loaderAddChild(null, g);
					pic.add2SVG(g);
				} catch (SVGElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SVGException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
	
	public void recomputeContent() {
		this.initTLPics();
		Pair<Dot, Map<DotElement, SVGElementManipulator<Group>>> content = createDotSVGContent();
		this.dotPanel.changeContent(content.getLeft(), content.getRight());
	}
	
	////////////////////////////////////////////////////////////
	// Handling Transition Clicking
	////////////////////////////////////////////////////////////
	public void addTransitionSelectedListener(TransitionClickListener listener) {
		listenerList.add(TransitionClickListener.class, listener);
	}

	public void removeTransitionSelectedListener(TransitionClickListener listener) {
		listenerList.remove(TransitionClickListener.class, listener);
	}

	protected void fireTransitionSelected(Transition t) {
		TransitionSelectedEvent transSelectEvent = null;
		 // Guaranteed to return a non-null array
		 Object[] listeners = listenerList.getListenerList();
		 // Process the listeners last to first, notifying
		 // those that are interested in this event
		 for (int i = listeners.length-2; i >= 0; i-=2) {
			 if (listeners[i] == TransitionClickListener.class) {
				 // Lazily create the event:
				 if (transSelectEvent == null)
					 transSelectEvent = new TransitionSelectedEventImpl(t);
				 ((TransitionClickListener) listeners[i+1]).transitionSelected(transSelectEvent);
			 }
		 }
	 }

	////////////////////////////////////////////////////////////
	// Handling Config Selection 
	////////////////////////////////////////////////////////////

	@Override
	public void itemStateChanged(ItemEvent e) {
	  if (e.getSource() == rBtnRelative) {
			if (e.getStateChange() == 1) {
				this.tlNormMethod = TLMetricNormalization.RELATIVE;
				this.recomputeContent();
			}
		}
		else {
			if (e.getStateChange() == 1) {
				this.tlNormMethod = TLMetricNormalization.TOTAL;
				this.recomputeContent();
			}
		}
		
	}
}
