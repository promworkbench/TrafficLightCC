package org.processmining.trafficlightcc.visualization.model;

import javax.swing.table.DefaultTableModel;

import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.OrderDiagnosticType;

public class DiagnosticDetailTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7125759005257055001L;
	
	public DiagnosticDetailTableModel() {
		// Popoulate with empty table
		super(new Object[][] {
			{"Element", "nothing selected"},
			{"Case Activations", "-"},
			{"Activations", "-"},
			{"Missing", "-"},
			{"Additional", "-"},
			{"Early", "-"},
			{"Chaos", "-"},
			{"Late", "-"},
			{"Sync", "-"}
		}, new Object[] {"", "Value"});
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	public void populateWithStatistics(TLCElementDiagnostic diag) {
		switch (diag.getDiagnosticType()) {
		case ATTRIBUTABLE_TRANSITION:
		case TRANSITION_ADDITIONAL:
		case TRANSITION_NONUNIQUE:
			// Element label
			this.setValueAt(diag.getTransition().get().getLabel(), 0, 1);
			// Case Activations
			this.setValueAt(Integer.toString(diag.getNbrCases()), 1, 1);
			// General Activations
			this.setValueAt(Integer.toString(diag.getNbrCases()), 2, 1);
			// Missing 
			this.setValueAt(Integer.toString(diag.getMissing()), 3, 1);
			// Additional 
			this.setValueAt(Integer.toString(diag.getAdditional()), 4, 1);
			// Early 
			this.setValueAt(Integer.toString(diag.getOrder().getOrderDetail().get(OrderDiagnosticType.EARLY)), 5, 1);
			// Chaos 
			this.setValueAt(Integer.toString(diag.getOrder().getOrderDetail().get(OrderDiagnosticType.CHAOTICSECTION)), 6, 1);
			// Late 
			this.setValueAt(Integer.toString(diag.getOrder().getOrderDetail().get(OrderDiagnosticType.LATE)), 7, 1);
			// Sync 
			this.setValueAt(Integer.toString(diag.getSync()), 8, 1);
			break;
		case EVENTCLASS_ADDITIONAL:
		case EVENTCLASS_NONUNIQUE:
			// Element label
			this.setValueAt(diag.getEvClass().get().getId(), 0, 1);
			// Case Activations
			this.setValueAt(Integer.toString(diag.getNbrCases()), 1, 1);
			// General Activations
			this.setValueAt(Integer.toString(diag.getNbrActivations()), 2, 1);
			// Missing 
			this.setValueAt("-", 3, 1);
			// Additional 
			this.setValueAt(Integer.toString(diag.getAdditional()), 4, 1);
			// Early 
			this.setValueAt("-", 5, 1);
			// Chaos 
			this.setValueAt("-", 6, 1);
			// Late 
			this.setValueAt("-", 7, 1);
			// Sync 
			this.setValueAt("-", 8, 1);
			break;
		default:
			break;

		}
	}
	
	

}
