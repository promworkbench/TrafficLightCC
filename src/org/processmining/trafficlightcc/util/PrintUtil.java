package org.processmining.trafficlightcc.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;

public class PrintUtil {
	
	public static String formatLogMoveCost(Map<XEventClass, Integer> m) {
		return m.entrySet().stream()
			.map(e -> e.getKey().getId() + ": " + e.getValue())
			.collect(Collectors.joining("\n", "{", "}"));
	}

	public static String formatModelMoveCost(Map<Transition, Integer> m) {
		return m.entrySet().stream()
			.map(e -> e.getKey().getLabel() + ": " + e.getValue())
			.collect(Collectors.joining("\n", "{", "}"));
	}

	public static String formatModelClassMoveCost(Map<TransClass, Integer> m) {
		return m.entrySet().stream()
			.map(e -> e.getKey().getId() + ": " + e.getValue())
			.collect(Collectors.joining("\n", "{", "}"));
	}
	
	public static String formattedT2EMap(TransEvClassMapping m) {
		return m.entrySet().stream()
			.map(e -> e.getKey().getLabel() + " -> " + e.getValue())
			.collect(Collectors.joining("\n", "t -> e {\n", "}"));
	}

	public static String formattedTClass2EClassesMap(Map<TransClass, Set<EvClassPattern>> m) {
		return m.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), 
						e.getValue().stream().map(EvClassPattern::toString).collect(Collectors.joining(", ", "{", "}"))
						))
				.map(p -> p.getLeft().getId() + ": " + p.getRight())
				.collect(Collectors.joining("\n", "{{", "}}"));
	}

	public static String formattedTClass2EClassesMap(TransClass2PatternMap m, Collection<Transition> transitions) {
		return transitions.stream()
			.map(t -> Pair.of(t, m.getPatternsOf(t)))
			.filter(p -> p.getRight() != null)
			.map(p-> {
				StringBuilder builder = new StringBuilder("[");
				short[] patterns = p.getRight();
				// See TransClass2Pattern class for the weird layout of the patterns array
				for (int i = 0; i < patterns.length; i += 2 + patterns[i + 1] ) {
					short s = patterns[i];
					builder.append(m.getPatternStr(s));
					builder.append(", ");
				}
				builder.append("]");
				return p.getLeft().getLabel() + ": " + builder.toString();
			})
			.collect(Collectors.joining("\n"));
	}
	
	public static Object lazy(Callable<?> callable) {
	    return new Object() {
	        @Override
	        public String toString() {
	            try {
	                Object result = callable.call();
	                if (result == null) {
	                    return "null";
	                }

	                return result.toString();
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        }
	    };
	}


}
