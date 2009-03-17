package com.sun.common.util.logging;

public class LoggingPropertyNames {
	
	public static final String GFFileHandler = "com.sun.enterprise.server.logging.GFFileHandler.";

    public static final String SyslogHandler = "com.sun.enterprise.server.logging.SyslogHandler.";
		
	public static final String logRotationLimitInBytes = GFFileHandler + "rotationLimitInBytes";
	
	public static final String logRotationTimelimitInMinutes = GFFileHandler + "rotationTimelimitInMinutes";
	
	public static final String file = GFFileHandler +"file";
	
	public static String logFormatter = GFFileHandler +"formatter";
	
	public static final String logHandler = "handlers";
	
	public static final String useSystemLogging	= SyslogHandler + "useSystemLogging";

	public static final String retainErrorStatisticsForHours = GFFileHandler + "retainErrorsStasticsForHours";
	
	public static final String logFilter= GFFileHandler + "logFilterClass";
	
	public static final String logToConsole = GFFileHandler + "logtoConsole";
	
	public static final String alarms= GFFileHandler + "alarms";
	
		
	public static final String MAX_QUEUE_SIZE = GFFileHandler + "maxQueueSize";
	
	public static final String QUEUE_FLUSH_FREQUENCY = GFFileHandler + "queueFlushFrequency";	
	
}

