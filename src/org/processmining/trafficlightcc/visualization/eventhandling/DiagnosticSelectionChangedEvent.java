package org.processmining.trafficlightcc.visualization.eventhandling;

import java.util.UUID;

public class DiagnosticSelectionChangedEvent {
	
	private final UUID elementDiagnosticId;

	public DiagnosticSelectionChangedEvent(UUID elementDiagnosticId) {
		super();
		this.elementDiagnosticId = elementDiagnosticId;
	}

	public UUID getElementDiagnosticId() {
		return elementDiagnosticId;
	}

}
