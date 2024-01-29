package org.processmining.trafficlightcc.models;

import java.util.Collection;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;

public class TLConfDiagnosticsBundle {
	private final Petrinet pn;

	private final PNRepResult resRound1;

	private final PNRepResult resRound2;

	private final Collection<TLCElementDiagnostic> tlcDiagnostics;
	
	private final int nbrUnreliableAlignmentsR1;

	private final int nbrUnreliableAlignmentsR2;

	public TLConfDiagnosticsBundle(Petrinet pn, PNRepResult resRound1, PNRepResult resRound2,
			Collection<TLCElementDiagnostic> tlcDiagnostics, 
			int nbrUnreliableAlignmentsR1, int nbrUnreliableAlignmentsR2) {
		super();
		this.pn = pn;
		this.resRound1 = resRound1;
		this.resRound2 = resRound2;
		this.tlcDiagnostics = tlcDiagnostics;
		this.nbrUnreliableAlignmentsR1 = nbrUnreliableAlignmentsR1;
		this.nbrUnreliableAlignmentsR2 = nbrUnreliableAlignmentsR2;
	}

	public Petrinet pn() {
		return pn;
	}

	public PNRepResult resRound1() {
		return resRound1;
	}

	public PNRepResult resRound2() {
		return resRound2;
	}

	public Collection<TLCElementDiagnostic> tlcDiagnostics() {
		return tlcDiagnostics;
	}

	public int getNbrUnreliableAlignmentsR1() {
		return nbrUnreliableAlignmentsR1;
	}

	public int getNbrUnreliableAlignmentsR2() {
		return nbrUnreliableAlignmentsR2;
	}



}
