
package test;


public class FlushException extends Exception {

	String flushEx;

	public FlushException() {
		super();             // call superclass constructor
	}

	public FlushException(String flushException) {
		super(flushException);
		flushEx = flushException;
	}

	public String getError()
  	{
    		return flushEx;
  	}

}

		

