package SWDModelReferenceClasses;

/**
 * This class represents an exception which is thrown in the SWDCellMulti
 * when the temperature data runs out (i.e. the simulator runs for more timesteps
 * than there are temperature data points).
 * 
 * @author MikeD
 */

public class EndOfDataException extends Exception {
    
	/**
	 * Constructor for the exception; allows for a specified error message
	 * @param message - the message which is returned when the exception is thrown
	 */
	public EndOfDataException(String message) {
        super(message);
    }
}