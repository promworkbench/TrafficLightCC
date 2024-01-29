package org.processmining.trafficlightcc.visualization.trafficlight;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.OrderDiagnosticType;

import com.kitfox.svg.Group;
import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;


public class TrafficLightPic extends InOutInLayoutableElement {

	/**
	 * The associated element diagnostics.
	 */
	private final TLCElementDiagnostic elDiag;
	
	/**
	 * Area of the pic that should be associated with a metric value of 1.
	 */
	private float  maxArea;
	
	/**
	 * Minimum inner sep between the traffic light rectangle and the closest inner element.
	 */
	private final float innerSep;
	
	/**
	 * Minimum distance between inner elements
	 */
	private final float elementPadding;

	/**
	 *  Default factor for the pic's area if there is no metric value.
	 */
	protected float defaultAreaAbsentMetric;
	
	/**
	 * Picture for additional behavior
	 * @throws SVGElementException
	 */
	private final PicAdditional picAdd;

	private final InOutInLayoutableBox boxPicAdd;

	/**
	 * Picture for missing behavior 
	 * @throws SVGElementException
	 */
	private final PicMissing picMiss;

	private final InOutInLayoutableBox boxPicMiss;

	/**
	 * Picture for early executions 
	 * @throws SVGElementException
	 */
	private final PicEarly picEarly;

	private final InOutInLayoutableBox boxPicEarly;

	/**
	 * Picture for chaotic executions 
	 * @throws SVGElementException
	 */
	private final PicChaos picChaos;

	private final InOutInLayoutableBox boxPicChaos;

	/**
	 * Picture for late executions 
	 * @throws SVGElementException
	 */
	private final PicLate picLate;

	private final InOutInLayoutableBox boxPicLate;

	/**
	 * Picture for sync executions 
	 * @throws SVGElementException
	 */
	private final PicSync picSync;

	private final InOutInLayoutableBox boxPicSync;
	
	/**
	 * Column layout of the traffic light.
	 */
	private TLRowLayoutVerticalGroup trafficLightColumn;
	
	/**
	 * Method how to normalize this pic's associated metric value.
	 */
	private final TLMetricNormalization normalizationMethod;
	
	public TrafficLightPic(TLCElementDiagnostic elDiag, float maxArea, 
			float defaultAreaAbsentMetric, float defaultAreaZeroMetric, 
			float innerSep, float elementPadding, TLMetricNormalization normalizationMethod) {
		super();
		this.elDiag = elDiag;
		this.maxArea = maxArea;
		this.defaultAreaAbsentMetric = defaultAreaAbsentMetric;
		this.innerSep = innerSep;
		this.elementPadding = elementPadding;
		this.normalizationMethod = normalizationMethod;
		
		////////////////////////////////////////
		// Init the Subpicture
		////////////////////////////////////////
		////////////////////
		// Sync
		////////////////////
		switch(elDiag.getDiagnosticType()) {
			case ATTRIBUTABLE_TRANSITION: // Metric value available
			case TRANSITION_NONUNIQUE:
				float metric = calcMetric(elDiag.getSync(), elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				this.picSync = 
						new PicSync(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, Optional.of(metric));
				break;
			case TRANSITION_ADDITIONAL: // No metric value available
			case EVENTCLASS_ADDITIONAL:
			case EVENTCLASS_NONUNIQUE:
			default:
				this.picSync = new PicSync(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, Optional.empty());
				break;
		}
		this.boxPicSync = new InOutInLayoutableBox(Optional.of(this.picSync));

		////////////////////
		// Order
		////////////////////
		switch(elDiag.getDiagnosticType()) {
			case ATTRIBUTABLE_TRANSITION: // Metric value available
			case TRANSITION_NONUNIQUE:
				float metricEarly = calcMetric(elDiag.getOrder().getOrderDetail().get(OrderDiagnosticType.EARLY), 
						elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				float metricChaos = calcMetric(elDiag.getOrder().getOrderDetail().get(OrderDiagnosticType.CHAOTICSECTION), 
						elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				float metricLate = calcMetric(elDiag.getOrder().getOrderDetail().get(OrderDiagnosticType.LATE), 
						elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				this.picEarly = new PicEarly(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.of(metricEarly));
				this.picChaos = new PicChaos(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.of(metricChaos));
				this.picLate = new PicLate(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.of(metricLate));
				break;
			case TRANSITION_ADDITIONAL: // No metric value available
			case EVENTCLASS_ADDITIONAL: 
			case EVENTCLASS_NONUNIQUE:
			default:
				this.picEarly = new PicEarly(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, Optional.empty());
				this.picChaos = new PicChaos(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, Optional.empty());
				this.picLate = new PicLate(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, Optional.empty());
				break;
		}
		this.boxPicEarly = new InOutInLayoutableBox(Optional.of(this.picEarly));
		this.boxPicLate = new InOutInLayoutableBox(Optional.of(this.picLate));
		this.boxPicChaos = new InOutInLayoutableBox(Optional.of(this.picChaos));

		////////////////////
		// Additional
		////////////////////
		switch(elDiag.getDiagnosticType()) {
			case ATTRIBUTABLE_TRANSITION: // Metric value available
			case EVENTCLASS_ADDITIONAL: 
			case EVENTCLASS_NONUNIQUE:
				float metric = calcMetric(elDiag.getAdditional(), elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				this.picAdd = new PicAdditional(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.of(metric));
				break;
			case TRANSITION_NONUNIQUE:
			case TRANSITION_ADDITIONAL: // No metric value available: 
			default:
				this.picAdd = new PicAdditional(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.empty());
				break;
		}
		this.boxPicAdd = new InOutInLayoutableBox(Optional.of(this.picAdd));

		////////////////////
		// Missing
		////////////////////
		switch(elDiag.getDiagnosticType()) {
			case ATTRIBUTABLE_TRANSITION: // Metric value available
			case TRANSITION_NONUNIQUE:
			case TRANSITION_ADDITIONAL: 
				float metric = calcMetric(elDiag.getMissing(), elDiag.getNbrCases(), elDiag.getNbrCasesLog());
				this.picMiss = new PicMissing(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.of(metric));
				break;
			case EVENTCLASS_ADDITIONAL: // No metric value available: 
			case EVENTCLASS_NONUNIQUE:
			default:
				this.picMiss = new PicMissing(maxArea, defaultAreaAbsentMetric, defaultAreaZeroMetric, 
						Optional.empty());
				break;
		}
		this.boxPicMiss = new InOutInLayoutableBox(Optional.of(this.picMiss));
		
		////////////////////////////////////////
		// Init the column Layout;
		////////////////////////////////////////
		// Row 1 
		List<InOutInLayoutableElement> listRow1 = new LinkedList<>();
		listRow1.add(boxPicAdd);
		listRow1.add(boxPicMiss);
		TLRowLayoutHorizontalGroup row1 = new TLRowLayoutHorizontalGroup(this.elementPadding, listRow1);

		// Row 2 
		List<InOutInLayoutableElement> listRow2 = new LinkedList<>();
		listRow2.add(boxPicEarly);
		listRow2.add(boxPicChaos);
		listRow2.add(boxPicLate);
		TLRowLayoutHorizontalGroup row2 = new TLRowLayoutHorizontalGroup(this.elementPadding, listRow2);
		
		// Traffic Light Column
		List<InOutInLayoutableElement> listColumn = new LinkedList<>(); 
		listColumn.add(row1);
		listColumn.add(row2);
		listColumn.add(this.boxPicSync);
		
		this.trafficLightColumn = new TLRowLayoutVerticalGroup(elementPadding, listColumn);
		
	}
	
	private float calcMetric(int c, int nbrCasesActivated, int nbrCasesLog) {
		if (nbrCasesActivated == 0) {
			return 0f;
		}
		switch (normalizationMethod) {
			case RELATIVE:
				return ((float) Math.max(0, c)) / nbrCasesActivated; 
			case TOTAL:
				return ((float) Math.max(0, c)) / nbrCasesLog; 
			default:
				return 0f;
		}
	}
	
	public String getSVGString() {
		// Note on SVG
		// THE DEPENDENCIES ARE A MESS
		// SVGDiagram cannot export (string or files)
		// Recent version of batik results in dependency problems (a version that is not updated
		// requires a package from a more recent version that does not contain the class; 
		// sorting all this out will be a nightmare
		// Using the current version of batik:
		// Importing org.w3c.dom.Document is super problematic with new versions of Java:
		// org.w3c is now contained in the jdk; however, batik has som org.w3c.events package implementation
		// Results in package conflicts that Java 11+ do not accept
		// FOR NOW: I'll build the SVG string manually

		// We are using a constant available on the SVGDOMImplementation,
		// but we could have used "http://www.w3.org/2000/svg".
		StringBuilder svgBuilder = new StringBuilder();
		
		svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		svgBuilder.append(String.format(Locale.ROOT, "<svg version=\"1.1\" width=\"%fpt\" height=\"%fpt\" "
				+ "viewBox=\"0.00 0.00 %f %f\" "
				+ "xmlns=\"http://www.w3.org/2000/svg\">\n", 
				this.layoutInfo.w, this.layoutInfo.h,
				this.layoutInfo.w, this.layoutInfo.h));
		this.writeSVG(svgBuilder);
		svgBuilder.append("</svg>");
		return svgBuilder.toString();
		
	}
	
	public void layout() {
		this.initInternalLayout();
		this.finalPositionUpdate();
	}

	@Override
	public void initInternalLayout() {
		this.trafficLightColumn.initInternalLayout();
		// Traffic light height and width
		this.layoutInfo.w = this.trafficLightColumn.layoutInfo.w + 2 * this.innerSep;
		this.layoutInfo.h = this.trafficLightColumn.layoutInfo.h + 2 * this.innerSep;
		
		// Boxes early and late should have equal width
		float wEarlyLate = Math.max(this.boxPicEarly.layoutInfo.w, this.boxPicLate.layoutInfo.w);
		this.boxPicEarly.processExternalDimensions(Optional.of(wEarlyLate), Optional.empty());
		this.boxPicLate.processExternalDimensions(Optional.of(wEarlyLate), Optional.empty());
		
		// Boxes for additional and missing should have equal width (currently this is true by design)
		float wAddMissing = Math.max(this.boxPicAdd.layoutInfo.w, this.boxPicMiss.layoutInfo.w);
		this.boxPicAdd.processExternalDimensions(Optional.of(wAddMissing), Optional.empty());
		this.boxPicMiss.processExternalDimensions(Optional.of(wAddMissing), Optional.empty());
		
		this.boxPicEarly.contentAlignment = ContentAlignment.RIGHT;
		this.boxPicLate.contentAlignment = ContentAlignment.LEFT;
		
		// Fix the boxes that should have equals width
		this.boxPicEarly.fixDim = true;
		this.boxPicLate.fixDim = true;
		this.boxPicAdd.fixDim = true;
		this.boxPicMiss.fixDim = true;
		
		// Update Layout
		this.trafficLightColumn.initInternalLayout();
		
		this.layoutInfo.w = 2 * this.innerSep + this.trafficLightColumn.layoutInfo.w;
		this.layoutInfo.h = 2 * this.innerSep + this.trafficLightColumn.layoutInfo.h;
	}

	@Override
	public void processExternalDimensions(Optional<Float> wDesiredExt, Optional<Float> hDesiredExt) {
		this.layoutInfo.w = Math.max(this.layoutInfo.w, wDesiredExt.orElse(0f));
		this.layoutInfo.h = Math.max(this.layoutInfo.h, wDesiredExt.orElse(0f));
	}

	@Override
	public void finalPositionUpdate() {
		this.trafficLightColumn.finalPositionUpdate();

		// Center the light column
		this.layoutInfo.x = (this.layoutInfo.w - this.trafficLightColumn.layoutInfo.w) / 2;
		this.layoutInfo.y = (this.layoutInfo.h - this.trafficLightColumn.layoutInfo.h) / 2;
	}
	
	@Override
	public void writeSVG(StringBuilder builder) {
		builder.append(String.format(Locale.ROOT, "<rect x=\"0\" y=\"0\" width=\"%f\" height=\"%f\"/>\n",
				this.layoutInfo.w, this.layoutInfo.h));
		builder.append(String.format(Locale.ROOT, "<g id=\"groupTLLightColumn\" transform=\"translate(%f, %f)\">\n", 
				this.layoutInfo.x, this.layoutInfo.y));
		this.trafficLightColumn.writeSVG(builder);
		builder.append("</g>\n");
	}

	@Override
	public void add2SVG(SVGElement svgEl) throws SVGElementException {
		Group gTrafficLight = new Group();
		svgEl.loaderAddChild(null, gTrafficLight);
		
		// Out rectangle
		Rect rect = new Rect();
		rect.addAttribute("x", AnimationElement.AT_XML, Float.toString(0f));
		rect.addAttribute("y", AnimationElement.AT_XML, Float.toString(0f));
		rect.addAttribute("width", AnimationElement.AT_XML, Float.toString(this.layoutInfo.w));
		rect.addAttribute("height", AnimationElement.AT_XML, Float.toString(this.layoutInfo.h));
		rect.addAttribute("rx", AnimationElement.AT_XML, Float.toString(this.innerSep));
		
		gTrafficLight.loaderAddChild(null, rect);
		
		// Light column
		Group gLightColumn = new Group();
		gLightColumn.addAttribute("transform", AnimationElement.AT_XML, 
				String.format(Locale.ROOT, "translate(%f, %f)", this.layoutInfo.x, this.layoutInfo.y));
		gTrafficLight.loaderAddChild(null, gLightColumn);
		this.trafficLightColumn.add2SVG(gLightColumn);
	}

}
