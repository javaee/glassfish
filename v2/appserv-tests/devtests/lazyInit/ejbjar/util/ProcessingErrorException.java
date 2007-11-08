package examples.sfsb;

/**
 * This class is used with the examples.ejb20.statefulSession package.
 * ProcessingErrorException is thrown if an error occurs while
 * either buying or selling a stock.
 *
 * @author Copyright (c) 1998-2002 by BEA Systems, Inc. All Rights Reserved.
 */
public class ProcessingErrorException extends Exception {

  /**
   * Catches exceptions without a specified string
   *
   */
  public ProcessingErrorException() {}

  /**
   * Constructs the appropriate exception with the specified string
   *
   * @param message           Exception message
   */
  public ProcessingErrorException(String message) {super(message);}
}
