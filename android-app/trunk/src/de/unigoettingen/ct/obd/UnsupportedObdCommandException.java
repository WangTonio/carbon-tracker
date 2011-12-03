package de.unigoettingen.ct.obd;

/**
 * Indicates that a concrete OBD system has no unit connected to the bus, that could answer the specified request.
 * @author Fabian Sudau
 *
 */
public class UnsupportedObdCommandException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public UnsupportedObdCommandException(String msg){
		super(msg);
	}

}
