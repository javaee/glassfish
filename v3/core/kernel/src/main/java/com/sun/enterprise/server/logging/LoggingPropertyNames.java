package com.sun.enterprise.server.logging;

public class LoggingPropertyNames {
	
	public static final String FSHandler = "com.sun.enterprise.server.logging.FileandSyslogHandler.";
		
	public static final String logRotationLimitInBytes = FSHandler + "rotationLimitInBytes";
	
	public static final String logRotationTimelimitInMinutes = FSHandler + "rotationTimelimitInMinutes";
	
	public static final String file = FSHandler +"file";
	
	public static String logFormatter = FSHandler +"formatter";
	
	public static final String logHandler = "handlers";
	
	public static final String useSystemLogging	= FSHandler + "useSystemLogging";

	public static final String retainErrorStatisticsForHours = FSHandler + "retainErrorsStasticsForHours";
	
	public static final String logFilter= FSHandler + "logFilterClass";
	
	public static final String logToConsole = FSHandler + "logtoConsole";
	
	public static final String alarms= FSHandler + "alarms";
	
		
	public static final String MAX_QUEUE_SIZE = FSHandler + "maxQueueSize";
	
	public static final String QUEUE_FLUSH_FREQUENCY = FSHandler + "queueFlushFrequency";	
	
}

