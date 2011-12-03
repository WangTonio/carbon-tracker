package de.unigoettingen.ct.obd;

public class UnsupportedObdCommandException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public UnsupportedObdCommandException(String msg){
		super(msg);
	}

}
