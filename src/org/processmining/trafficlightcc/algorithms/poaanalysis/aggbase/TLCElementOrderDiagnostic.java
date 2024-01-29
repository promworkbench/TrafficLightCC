package org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

import org.processmining.trafficlightcc.algorithms.poaanalysis.base.OrderDiagnosticType;

public class TLCElementOrderDiagnostic {

	/**
	 * MISORDERED execution counter
	 */
	private int order;
	
	/**
	 * Detailed Split
	 */
	private final EnumMap<OrderDiagnosticType, Integer> orderDetail;
	
	private final ArrayList<Long> tdEarly;

	private final ArrayList<Long> tdLate;

	public TLCElementOrderDiagnostic() {
		order = 0;
		this.tdEarly = new ArrayList<>();
		this.tdLate = new ArrayList<>();

		this.orderDetail = new EnumMap<>(OrderDiagnosticType.class);
		for (OrderDiagnosticType diagType : OrderDiagnosticType.values()) {
			this.orderDetail.put(diagType, 0);
		}
	}
	
	public void incOrderCounter(OrderDiagnosticType diagType, int inc) {
		// Counter total
		this.order += inc;

		// Update details counter
		this.orderDetail.put(diagType, orderDetail.get(diagType) + inc);
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("MISORDER(");
		// There exist misorder information
		if (this.order > -1) {
			// Early
			builder.append("<: ");
			builder.append(this.orderDetail.get(OrderDiagnosticType.EARLY));
			builder.append(", ");

			// Chaos
			builder.append("chaos: ");
			builder.append(this.orderDetail.get(OrderDiagnosticType.CHAOTICSECTION));
			builder.append(", ");

			// Late
			builder.append(">: ");
			builder.append(this.orderDetail.get(OrderDiagnosticType.LATE));
		}
		builder.append(")");
		
		return builder.toString();
	}

	public EnumMap<OrderDiagnosticType, Integer> getOrderDetail() {
		return orderDetail;
	}
	
	public void addTimedelta(OrderDiagnosticType orderTyp, Collection<Long> timedeltas) {
		if(orderTyp == OrderDiagnosticType.EARLY) {
			this.tdEarly.addAll(timedeltas);
		}
		else if(orderTyp == OrderDiagnosticType.LATE) {
			this.tdLate.addAll(timedeltas);
		}
	}

	public ArrayList<Long> getTdEarly() {
		return tdEarly;
	}

	public ArrayList<Long> getTdLate() {
		return tdLate;
	}
}
