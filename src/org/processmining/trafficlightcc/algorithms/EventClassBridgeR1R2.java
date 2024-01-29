package org.processmining.trafficlightcc.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;

public class EventClassBridgeR1R2 {

	/**
	 * Key of the attribute that will be added to events to indicate synchronous and non-synchronous 
	 * first round alignment moves.
	 */
	public static final String KEY_SYNC_EVENT = "R1Syc";
	
	/**
	 * Mapping between event classes from round two and the two optional 
	 * (i.e., synchronous and non-synchronous) corresponding event classes in the second round.
	 */
	private final Map<XEventClass, EventClassesContainerR2> classMapR1ToR2;
	
	/**
	 * Event classes in the first round.
	 */
	private final XEventClasses evClassesR1;

	/**
	 * Event classes in the second round.
	 * ONLY used event classes.
	 */
	private final XEventClasses evClassesR2;

	public EventClassBridgeR1R2(XEventClasses evClassesR1, XEventClassifier classifierR2) {
		this.classMapR1ToR2 = new HashMap<>();
		this.evClassesR1 = evClassesR1;
		this.evClassesR2 = new XEventClasses(classifierR2);
	}
	
	/**
	 * Update a synchronously executed event by an attribute that keeps this information.
	 * Adds a new value to the internal attribute map.
	 * @param event
	 */
	public void addInfoSyncEvent(XEvent event) {
		event.getAttributes().put(KEY_SYNC_EVENT, new XAttributeBooleanImpl(KEY_SYNC_EVENT, true)); 
	}

	/**
	 * Update a non-synchronously executed event by an attribute that keeps this information.
	 * Adds a new value to the internal attribute map.
	 * @param event
	 */
	public void addInfoNonSyncEvent(XEvent event) {
		event.getAttributes().put(KEY_SYNC_EVENT, new XAttributeBooleanImpl(KEY_SYNC_EVENT, false)); 
	}
	
	/**
	 * Update the bridge by an event that was executed synchronously
	 * 
	 * IMPORTANT: Assumes that the information added by {@link this#addInfoSyncEvent(XEvent)} is available.
	 * @param event
	 */
	public void updateWithSnycExecution(XEvent event) {
		XEventClass evClassR1 = this.evClassesR1.getClassOf(event);
		EventClassesContainerR2 assocClassesR2 = classMapR1ToR2.get(evClassR1);
		
		// No classes in R2 are associated (neither sync nor non-sync)
		if (assocClassesR2 == null) {
			// Since no container is associated, we also have not registered it yet 
			this.evClassesR2.register(event);
			XEventClass evClassR2 = this.evClassesR2.getClassOf(event);
			classMapR1ToR2.put(evClassR1, new EventClassesContainerR2(Optional.of(evClassR2), Optional.empty()));
		}
		else {
			if (!assocClassesR2.evClassSync().isPresent()) {
				// Since associated container is empty, we also have not registered it yet 
				this.evClassesR2.register(event);
				XEventClass evClassR2 = this.evClassesR2.getClassOf(event);
				classMapR1ToR2.put(evClassR1, 
						new EventClassesContainerR2(
								Optional.of(evClassR2), assocClassesR2.evClassNonSync()));
			}
		}
		
	}
	
	/**
	 * Update the bridge by an event that was executed non-synchronously
	 * 
	 * IMPORTANT: Assumes that the information added by {@link this#addInfoNonSyncEvent(XEvent)} is available.
	 * @param event
	 */
	public void updateWithNonSnycExecution(XEvent event) {
		XEventClass evClassR1 = this.evClassesR1.getClassOf(event);
		EventClassesContainerR2 assocClassesR2 = classMapR1ToR2.get(evClassR1);
		
		// No classes in R2 are associated (neither sync nor non-sync)
		if (assocClassesR2 == null) {
			// Since no container is associated, we also have not registered it yet 
			this.evClassesR2.register(event);
			XEventClass evClassR2 = this.evClassesR2.getClassOf(event);
			classMapR1ToR2.put(evClassR1, new EventClassesContainerR2(Optional.empty(), Optional.of(evClassR2)));
		}
		else {
			if (!assocClassesR2.evClassNonSync().isPresent()) {
				// Since associated container is empty, we also have not registered it yet 
				this.evClassesR2.register(event);
				XEventClass evClassR2 = this.evClassesR2.getClassOf(event);
				classMapR1ToR2.put(evClassR1, 
						new EventClassesContainerR2(
								assocClassesR2.evClassSync(), Optional.of(evClassR2)));
			}
		}
		
	}	
	
	public Optional<EventClassesContainerR2> getEventClassesR2(XEventClass evClassR1) {
		if (this.classMapR1ToR2.containsKey(evClassR1)) {
			return Optional.of(this.classMapR1ToR2.get(evClassR1));
		}
		else {
			return Optional.empty();
		}
	}
	
	public final Map<XEventClass, EventClassesContainerR2> getMapping() {
		return this.classMapR1ToR2;
	}
	
	public XEventClasses getEventClassesR2() {
		return this.evClassesR2;
	}
}
