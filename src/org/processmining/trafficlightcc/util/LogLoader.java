package org.processmining.trafficlightcc.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.xeslite.parser.XesLiteXmlParser;

public class LogLoader {
	public static XLog loadLog(String pathLog) {
		File logFile = new File(pathLog);
		
		return loadLog(logFile);

	}
	
	public static XLog loadLog(Path pathLog) {
		File logFile = new File(pathLog.toUri());

		return loadLog(logFile);
	}
	
	private static XLog loadLog(File logFile) {
		List<XLog> parsedLogs = null;
		XesLiteXmlParser parserLog = new XesLiteXmlParser(true);
		try {
			parsedLogs = parserLog.parse(logFile);
		} catch (Exception e) {
			e.printStackTrace();
			//logger.error("Failed to load log");
			return null;
		}
		//logger.info("Done loading log.");
		return parsedLogs.get(0);
	}
}
