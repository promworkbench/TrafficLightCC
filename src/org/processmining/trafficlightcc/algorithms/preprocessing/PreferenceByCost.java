package org.processmining.trafficlightcc.algorithms.preprocessing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

public class PreferenceByCost {
	
	public static List<EvClassOccPosTieBreakInfo> getEventClassOccurrenceInfo(XLog log, 
			XEventClassifier classifier) {

		XEventClasses eventClasses = XEventClasses.deriveEventClasses(classifier, log);

		////////////////////
		// Collect Event Class Occurrence info
		////////////////////
		Map<XEventClass, EvClassLogOccInfoData> evClassOccInfo = log.stream()
			.flatMap(t -> {
				Map<XEventClass, EvClOccInfo> mapOccEvClMinPos = IntStream.range(0, t.size())
					.mapToObj(i -> new EvClOccInfo(eventClasses.getClassOf(t.get(i)), i))		// Event with index
					// Keep first activity occurrence
					.collect(Collectors.toMap(EvClOccInfo::eventClass, 							
							Function.identity(),
							(occ1, occ2) -> new EvClOccInfo(occ1.eventClass(), Math.min(occ1.pos(), occ2.pos()))));
				return mapOccEvClMinPos.values().stream();
			})
			.collect(Collectors.groupingBy(EvClOccInfo::eventClass, 
					// Count activity trace occurrences and sum in-trace positions
					Collectors.reducing(
							new EvClassLogOccInfoData(0, 0),
							occInfo -> new EvClassLogOccInfoData(1, occInfo.pos()),
							(info1, info2) -> new EvClassLogOccInfoData(
									info1.occurrences() + info2.occurrences(),
									info1.aggPosition() + info2.aggPosition()
								)
						)
				));
		
		// Create results (calculated average first occurrence in trace for each event class
		List<EvClassOccPosTieBreakInfo> lOccPosInfo = evClassOccInfo.entrySet().stream()
			.map(e -> new EvClassOccPosTieBreakInfo(e.getKey(),  // Event class
					e.getValue().occurrences(), // Nbr occurrences
					((float) e.getValue().aggPosition()) / e.getValue().occurrences())) // Avg first occurence
			.collect(Collectors.toCollection(() -> new LinkedList<>())); 	// Mutable list

		
		return lOccPosInfo;
	}
	

}
