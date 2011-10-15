package de.unigoettingen.ct.obd.cmd;

public class UnsupportedObdCommandException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public UnsupportedObdCommandException(String msg){
		super(msg);
	}

}
