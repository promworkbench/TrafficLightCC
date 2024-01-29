package org.processmining.trafficlightcc.visualization.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementType;
import org.processmining.trafficlightcc.visualization.eventhandling.DiagnosticSelectionChangedEvent;
import org.processmining.trafficlightcc.visualization.eventhandling.DiagnosticSelectionChangedListener;

public class TLCDiagnosticsModel {

	/**
	 * Collection of extracted traffic light conformance diagnostics
	 */
	private final Map<UUID, TLCElementDiagnostic> elDiags;
	
	/**
	 * Mapping from transition -> diagnostic id.
	 */
	private final Map<Transition, UUID> trans2DiagId;

	/**
	 * Mapping from event class -> diagnostic id.
	 */
	private final Map<XEventClass, UUID> evCl2DiagId;
	
	/**
	 * Petri net.
	 */
	private final Petrinet pn;
	
	/**
	 * Which element diagnostics is currently selected by the user. 
	 */
	private UUID selectedDiagnostic;
	
	/**
	 * Table model containing detailed diagnostics on the 
	 * currently selected element.
	 */
	private final DiagnosticDetailTableModel diagDetailTableModel;
	
	private final List<DiagnosticSelectionChangedListener> selChangedListeners;

	public TLCDiagnosticsModel(Petrinet pn, Collection<TLCElementDiagnostic> elDiagsCollection) {
		this.pn = pn;
		this.selChangedListeners = new LinkedList<>();
		this.elDiags = new HashMap<>();
		this.selectedDiagnostic = null;
		this.diagDetailTableModel = new DiagnosticDetailTableModel();
		
		this.trans2DiagId = elDiagsCollection.stream()
			.filter(d -> (
					d.getDiagnosticType() == TLCElementType.ATTRIBUTABLE_TRANSITION
					|| d.getDiagnosticType() == TLCElementType.TRANSITION_ADDITIONAL 
					|| d.getDiagnosticType() == TLCElementType.TRANSITION_NONUNIQUE))
			.collect(Collectors.toMap(d -> d.getTransition().get(), d -> d.getUuid()));

		this.evCl2DiagId = elDiagsCollection.stream()
			.filter(d -> (
					d.getDiagnosticType() == TLCElementType.EVENTCLASS_ADDITIONAL
					|| d.getDiagnosticType() == TLCElementType.EVENTCLASS_NONUNIQUE))
			.collect(Collectors.toMap(d -> d.getEvClass().get(), d -> d.getUuid()));

		elDiagsCollection.forEach(d -> this.elDiags.put(d.getUuid(), d));
	}
	
	public Map<Transition, TLCElementDiagnostic> getTransitionAssignableDiagnostics() {
		return this.trans2DiagId.entrySet().stream()
				.map(Entry::getValue)
				.map(elDiags::get)
				.collect(Collectors.toMap(d -> d.getTransition().get(), Function.identity()));
	}

	public Map<XEventClass, TLCElementDiagnostic> getNonTransitionAssignableEventClassDiagnostics() {
		return this.evCl2DiagId.entrySet().stream()
				.map(Entry::getValue)
				.map(elDiags::get)
				.collect(Collectors.toMap(d -> d.getEvClass().get(), Function.identity()));
	}
	
	public Petrinet getPetriNet() {
		return this.pn;
	}
	
	public void setSelectedUUID(UUID idSelected) {
		// Selection changed
		if (idSelected != null && !idSelected.equals(this.selectedDiagnostic)) {
			this.selectedDiagnostic = idSelected;
			this.diagDetailTableModel.populateWithStatistics(this.elDiags.get(idSelected));
			fireDiagnosticsSelectionChangedEvent(idSelected);
		}
	}
	
	public void addDiagnosticsSelectionChanedListener(DiagnosticSelectionChangedListener listener) {
		this.selChangedListeners.add(listener);
	}

	public void removeDiagnosticsSelectionChanedListener(DiagnosticSelectionChangedListener listener) {
		this.selChangedListeners.remove(listener);
	}
	
	public void fireDiagnosticsSelectionChangedEvent(UUID idSelected) {
		 DiagnosticSelectionChangedEvent selectEvent = null;
		 // Process the listeners last to first, notifying
		 // those that are interested in this event
		 for (DiagnosticSelectionChangedListener listener : this.selChangedListeners) {
			 // Lazily create the event:
			 if (selectEvent == null) {
				 selectEvent = new DiagnosticSelectionChangedEvent(idSelected);
			 }
			 listener.selectedElementChanged(selectEvent);
		 }
	}
	
	public DiagnosticDetailTableModel getDiagDetailTableModel() {
		return diagDetailTableModel;
	}

	public Map<Transition, UUID> getTrans2DiagId() {
		return trans2DiagId;
	}

	public Map<XEventClass, UUID> getEvCl2DiagId() {
		return evCl2DiagId;
	}
	
	public TLCElementDiagnostic getDiagnosticsData(UUID idEl) {
		return this.elDiags.get(idEl);
	}

}
