package org.processmining.trafficlightcc.util.datastat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class LogStatUtil {
	
	public static Map<XEventClass, EventClassAggStat> getEventClassStats(XLog log, XEventClasses evClasses) {
	
		// Create result map
		Map<XEventClass, EventClassAggStat> aggMap = new HashMap<>();
		for (XEventClass evClass : evClasses.getClasses()) {
			aggMap.put(evClass, new EventClassAggStat());
		}
		
		////////////////////
		// Process Log
		////////////////////
		// The final aggregation class is synchronized
		log.stream()
			.parallel() // all traces concurrently
			.flatMap((XTrace t) -> {
				final int traceLen = t.size();
				Map<XEventClass, Optional<EventOccurrenceInfo>> res = IntStream.range(0, t.size())
					.mapToObj((int i) -> {		// Extract an event occurrence object for each event in the trace
						XEvent e = t.get(i);
						return new EventOccurrenceInfo(evClasses.getClassOf(e), i, ((float) i) / traceLen);
					})
					.collect( 	// Keep the first occurrence for each event
						Collectors.groupingBy(EventOccurrenceInfo::eventClass, 
								Collectors.minBy(Comparator.comparingInt(EventOccurrenceInfo::occPosition)))
					);
				// To stream (for flatmap)
				 return res.entrySet().stream()
						 .filter((Entry<XEventClass, Optional<EventOccurrenceInfo>> e) -> e.getValue().isPresent())
						 .map(Entry::getValue)
						 .map(Optional::get);
			})
			// Enter into the initially declared result map 
			// (at most event class per event class per case)
			.forEach((EventOccurrenceInfo o) -> aggMap.get(o.eventClass()).consume(o));
		
		return aggMap;
	
	}					

}
