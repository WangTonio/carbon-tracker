package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import de.unigoettingen.ct.data.Measurement;

/**
 * Abstract superclass for all odb-command-encapsulating classes. The template method pattern is used here, as this class
 * already performs some common processing steps and calls methods implemented in subclasses on parts that vary from command to command.
 * It also provides some commonly needed utility.
 * @author Fabian Sudau
 *
 */
public abstract class ObdCommand {
	
	private static final String CHARSET_USED = "ASCII";
	private byte[] buffer = new byte[10]; //will be used temporarily, when bytes are read from input streams. 
										//this is not located in the method scope to avoid allocation costs in every call.

	/**
	 * Sends the command to the ELM adapter, retrieves the result, interprets the result and then stores
	 * the result into the {@link Measurement} object.
	 * @param in the input stream expected to be from the ELM adapter
	 * @param out the output stream expected to be from the ELM adapter
	 * @param measure data object, where the result will be stored in
	 * @throws IOException if the obd command is not supported or the connection was lost
	 */
	public void queryResult(Measurement measure, InputStream in, OutputStream out) throws IOException{
		out.write((getCommandString()+"\r\n").getBytes(CHARSET_USED));
		out.flush();
		processResponse(readResultBackIn(in), measure);
	}
	
	/**
	 * Creates a string with the encoding used by ELM from the bytes provided.
	 * @param response 
	 * @return string made out of the argument
	 */
	public String responseToString(byte[] response){
		try {
			return new String(response, CHARSET_USED);
		}
		catch (UnsupportedEncodingException e) {
			throw new InternalError("The platform does not even support ASCII encoding. This is very unexpected");
		}
	}
	
	/**
	 * Returns the specific OBD/ELM command, that will be send out to the adapter.
	 * Subclasses must implement this and provide their command.
	 * @return a valid command without any line termination 
	 */
	public abstract String getCommandString();
	
	/**
	 * This template method must be implemented by subclasses. It interprets the bytes provided by the caller
	 * as a result from the ELM adapter. It then interprets them and stores the retrieved result in the 
	 * {@link Measurement} object.
	 * @param response
	 * @param measure
	 * @throws IOException if the result could not be interpreted / was invalid
	 */
	public abstract void processResponse(byte[] response, Measurement measure) throws IOException;
	
	/**
	 * Utility method, that reads as many ASCII characters from the stream until the command prompt has returned.
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private byte[] readResultBackIn(InputStream in) throws IOException{
		byte read = -1;
		int index =0;
		while ( (read = (byte) in.read()) != '>'){
			this.buffer[index]=read;
			index++;
		}
		byte[] ret = new byte[index];
		System.arraycopy(this.buffer, 0, ret, 0, index);
		return ret;
	}
	


}
