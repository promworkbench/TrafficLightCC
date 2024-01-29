package org.processmining.trafficlightcc.visualization.views;

import java.awt.BorderLayout;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementOrderDiagnostic;
import org.processmining.trafficlightcc.visualization.eventhandling.DiagnosticSelectionChangedEvent;
import org.processmining.trafficlightcc.visualization.eventhandling.DiagnosticSelectionChangedListener;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;

public class TLCDOrderTimeView extends JPanel implements DiagnosticSelectionChangedListener {
	
	public static int NBR_BINS = 10;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6565554623348846000L;

	/**
	 * Model providing the diagnostics data
	 */
	private final TLCDiagnosticsModel tlcDiagModel;

	/**
	 * Divide the x-axis time deltas (in ms) to 
	 * obtain a better readable {@link #scaleDiv}
	 */
	private double scaleDiv = 1;
	
	/**
	 * Scale of the x-axis.
	 */
	private String timeUnit = "ms";
	
	public TLCDOrderTimeView(TLCDiagnosticsModel tlcDiagModel) {
		this.tlcDiagModel = tlcDiagModel;
		
		// Listen to selection changed events
		tlcDiagModel.addDiagnosticsSelectionChanedListener(this);
		
		this.setLayout(new BorderLayout());

	}
	@Override
	public void selectedElementChanged(DiagnosticSelectionChangedEvent event) {
		TLCElementDiagnostic diagData = tlcDiagModel.getDiagnosticsData(event.getElementDiagnosticId());
		if (diagData != null) {
			switch(diagData.getDiagnosticType()) {
				case ATTRIBUTABLE_TRANSITION:
				case TRANSITION_ADDITIONAL:
				case TRANSITION_NONUNIQUE:
					Optional<HistogramDataset> dataset = instantiateHistogramData(diagData);
					this.updateChart(dataset);
					break;
				case EVENTCLASS_ADDITIONAL:
				case EVENTCLASS_NONUNIQUE:
					this.updateChart(Optional.empty());
					break;
				default:
					break;
			}
			
		}
	}
	
	private void updateChart(Optional<HistogramDataset> dataset) {
		if(!dataset.isPresent()) {
		}
		else {
		}
		this.removeAll();
		if (dataset.isPresent()) {
			JFreeChart histogram = ChartFactory.createHistogram("Early/Late Events",
                    "Timedelta (in " + timeUnit + ")", "Frequency", dataset.get(), PlotOrientation.VERTICAL, true, true, false);
			ChartPanel chartPanel = new ChartPanel(histogram);
			this.add(chartPanel, BorderLayout.CENTER);
		}
		this.revalidate();
		this.repaint();
	}
	
	private Optional<HistogramDataset> instantiateHistogramData(TLCElementDiagnostic diagData) {
		final TLCElementOrderDiagnostic orderDiag = diagData.getOrder();
		// If there are no early or late events, 
		// we cannot instantiate a reasonable dataset
		if (orderDiag.getTdEarly().size() == 0 && orderDiag.getTdLate().size() == 0) {
			return Optional.empty();
		}
		else {	// There is at least one early or late event 
			
			////////////////////
			// Determine Scale
			////////////////////
			// Determine the maximum time delta
			DoubleSummaryStatistics statistics = Stream.concat(
					orderDiag.getTdEarly().stream().map(Long::doubleValue),
					orderDiag.getTdLate().stream().map(Long::doubleValue)
				)
				.mapToDouble(d -> d)
				.summaryStatistics();
			double maxTD = Math.max(statistics.getMax(), Math.abs(statistics.getMin()));
		
			
			if (maxTD < 10 * 1000) { // < 10s -> ms
			}
			else if (maxTD < 10 * 60 * 1000) { // < 10 min -> sec 
				scaleDiv = 1000;
				timeUnit = "s";
			}
			else if (maxTD < 120 * 60 * 1000) { // <2h -> min 
				scaleDiv = 60 * 1000;
				timeUnit = "min";
			}
			else if (maxTD < 72 * 60 * 60 * 1000) { // <3d -> h 
				scaleDiv = 60 * 60 * 1000;
				timeUnit = "h";
			}
			else { // days
				scaleDiv = 24 * 60 * 60 * 1000;
				timeUnit = "d";
			}
			final double scaleDivFin = scaleDiv;
			
			HistogramDataset dataset = new HistogramDataset();
			
			// Add a series of EARLY timedeltas (if there exist early events)
			if (orderDiag.getTdEarly().size() > 0) {
				double[] tdEarly = orderDiag.getTdEarly().stream()
					.mapToDouble(Long::doubleValue)
					.map(d -> d / scaleDivFin)
					.toArray();
				dataset.addSeries("Early", tdEarly, NBR_BINS);
			}

			// Add a series of LATE timedeltas (if there exist early events)
			if (orderDiag.getTdLate().size() > 0) {
				double[] tdLate = orderDiag.getTdLate().stream()
					.mapToDouble(Long::doubleValue)
					.map(d -> d / scaleDivFin)
					.toArray();
				dataset.addSeries("Late", tdLate, NBR_BINS);
			}
			
			return Optional.of(dataset);
		}
			
		
	}

}
