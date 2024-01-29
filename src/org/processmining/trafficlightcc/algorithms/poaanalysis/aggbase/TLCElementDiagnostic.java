package org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.OrderDiagnosticType;

public class TLCElementDiagnostic {
	
	/**
	 * ID of this diagnostic
	 */
	private final UUID uuid;
	
	/**
	 * Associated transition, if any
	 */
	private final Optional<Transition> transition;
	
	/**
	 * Associated event class, if any
	 */
	private final Optional<XEventClass> evClass;
	
	/**
	 * Type of the diagnostics.
	 */
	private final TLCElementType diagnosticType;
	
	/**
	 * SYNC execution counter
	 */
	private int sync;
	
	/**
	 * MISORDERED execution counter
	 */
	private TLCElementOrderDiagnostic order;

	/**
	 * ADDITIONAL execution counter
	 */
	private int additional;

	/**
	 * MISSING execution counter
	 */
	private int missing;
	
	/**
	 * How often the element has been activated
	 */
	private int nbrActivations;
	
	/**
	 * Number of cases in the log where this element was activated 
	 * (useful for "global" normalization of the counts)
	 */
	private int nbrCases;
	
	/**
	 * Number of cases in log.
	 */
	private final int logSize;
	
	public TLCElementDiagnostic(TLCElementType diagnosticType, Optional<Transition> transition, 
			Optional<XEventClass> evClass,  int logSize) {
		this.uuid = UUID.randomUUID();
		this.diagnosticType = diagnosticType;
		// TODO Sanity check of transition and event class
		this.transition = transition;
		this.evClass = evClass;
		this.sync = 0;
		this.order = new TLCElementOrderDiagnostic();
		this.additional = 0;
		this.missing = 0;
		this.nbrActivations = 0;
		this.nbrCases = 0;
		this.logSize = logSize;
	}

	public int getSync() {
		return this.sync;
	}

	public TLCElementOrderDiagnostic getOrder() {
		return order;
	}

	public int getAdditional() {
		return additional;
	}

	public int getMissing() {
		return missing;
	}

	public int getNbrActivations() {
		return nbrActivations;
	}

	public int getNbrCases() {
		return nbrCases;
	}
	
	public int getNbrCasesLog() {
		return logSize;
	}
	
	public void incSync() {
		sync++;
	}

	public void incSync(int nbrCases) {
		sync += nbrCases;
	}

	public void incOrder(OrderDiagnosticType orderType, int nbrCases) {
		this.order.incOrderCounter(orderType, nbrCases);
	}
	
	public void addTimedeltas(OrderDiagnosticType orderType, Collection<Long> timedeltas) {
		this.order.addTimedelta(orderType, timedeltas);
	}

	public void incAdditional() {
		additional++;
	}

	public void incAdditional(int nbrCases) {
		additional += nbrCases;
	}
	
	public void incMissing() {
		missing++;
	}

	public void incMissing(int nbrCases) {
		missing += nbrCases;
	}
	
	public void incActivations() {
		nbrActivations++;
	}

	public void incActivations(int nbrCases) {
		nbrActivations += nbrCases;
	}
	
	public void incNbrCases() {
		this.nbrCases++;
	}

	public void incNbrCases(int nbrCases) {
		this.nbrCases += nbrCases;
	}

	public Optional<Transition> getTransition() {
		return transition;
	}

	public Optional<XEventClass> getEvClass() {
		return evClass;
	}

	public TLCElementType getDiagnosticType() {
		return diagnosticType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		switch (diagnosticType) {
			case ATTRIBUTABLE_TRANSITION:
				builder.append(this.evClass.get().getId());
				builder.append("(+Transition): {");
				break;
			case EVENTCLASS_ADDITIONAL:
				builder.append(this.evClass.get().getId());
				builder.append("(NotInPN): {");
				break;
			case EVENTCLASS_NONUNIQUE:
				builder.append(this.evClass.get().getId());
				builder.append("(NonUnique): {");
				break;
			case TRANSITION_ADDITIONAL:
				builder.append(this.transition.get().getLabel());
				builder.append("-");
				builder.append(this.transition.get().getId());
				builder.append("(OnlyPN): {");
				break;
			case TRANSITION_NONUNIQUE:
				builder.append(this.transition.get().getLabel());
				builder.append("-");
				builder.append(this.transition.get().getId());
				builder.append("(NoMatch): {");
				break;
			default:
				break;
		}
		
		// General information
		builder.append("cases: ");
		builder.append(this.nbrCases);
		builder.append(", ");
		builder.append("activations: ");
		builder.append(this.nbrActivations);
		builder.append(", ");

		// Sync
		builder.append("SYNC: ");
		builder.append(Integer.toString(this.sync));
		builder.append(", ");
		
		// Missing
		builder.append("MISS: ");
		builder.append(Integer.toString(this.missing));
		builder.append(", ");

		// Missing
		builder.append("ADD: ");
		builder.append(Integer.toString(this.additional));
		builder.append(", ");
		
		// Order
		builder.append("ORDER: ");
		builder.append(this.order.toString());
		
		builder.append("}");
		
		return builder.toString();
	}

	public UUID getUuid() {
		return uuid;
	}
	

}
