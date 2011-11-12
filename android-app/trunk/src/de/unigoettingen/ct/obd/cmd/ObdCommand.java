package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

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
	private final byte[] buffer = new byte[30]; //will be used temporarily, when bytes are read from input streams. 
										//this is not located in the method scope to avoid allocation costs in every call.

	/**
	 * Sends the command to the ELM adapter, retrieves the result, interprets the result and then stores
	 * the result into the {@link Measurement} object.
	 * @param in the input stream expected to be from the ELM adapter
	 * @param out the output stream expected to be from the ELM adapter
	 * @param measure data object, where the result will be stored in
	 * @throws IOException if the obd command is not supported or the connection was lost
	 * @throws UnsupportedObdCommandException 
	 */
	public void queryResult(Measurement measure, InputStream in, OutputStream out) throws IOException, UnsupportedObdCommandException{
		if(getCommandString() != null){
			out.write((getCommandString()+"\r").getBytes(CHARSET_USED)); //a carriage return terminates a command. line feed not necessary.
		}
		out.flush();
		processResponse(readResultBackIn(in), measure);
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
	public abstract void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException;
	
	/**
	 * Utility method, that reads as many ASCII characters from the stream until the command prompt has returned.
	 * It returns those characters as a string, stripping any spaces, NULL chars, carriage returns or command echoes.
	 * @param in
	 * @return the pure response string containing only hex digits directly interpretable as the result (e.g. "A1E4") or a custom
	 * ASCII string in the case of an ELM system command
	 * @throws IOException
	 * @throws UnsupportedObdCommandException 
	 */
	private String readResultBackIn(InputStream in) throws IOException, UnsupportedObdCommandException{
		byte read = -1;
		int index =0; //will be the number of characters read after the while loop
		while ( (read = (byte) in.read()) != '>'){
			if(read == -1){
				//end of stream reached, before the answer could be read => connection lost
				throw new IOException("Lost connection to the adapter.");
			}
			if(read != 0 && read != ' ' && read != '\r' && read !='\n'){ 
				//bytes with value 0 must be filtered out. see the ELM documentation p. 8
				//white spaces are also filtered out to simplify further processing
				this.buffer[index]=read;
				index++;
			}
		}
		String rawString = new String(this.buffer,0,index);
		Log.d(this.getClass().getSimpleName(), "OBD Output after filtering is:"+rawString);
		
		//in the initialization phase, SEARCHING... is sometimes part of the output
		//TODO figure out why and get rid of this hack
		if(rawString.startsWith("SEARCHING...")){
			rawString = rawString.substring(12);
			Log.d(this.getClass().getSimpleName(), "Output contained SEARCHING an was truncated to:"+rawString);
		}
		
		//check, if the ELM 'error code' for an unsupported command was returned
		if(rawString.equalsIgnoreCase("NODATA")){
			throw new UnsupportedObdCommandException("Command "+this.getCommandString()+" returns NODATA and is not supported by the vehicle.");
		}
		
		//when obd commands were issued, the first 4 returned bytes echo the command itself back
		//in that case, there are already removed from the result
		//commands directed at the ELM chip however, do not echo the command; their length is < 4 and will not be touched
		if(rawString.length() > 4){
			rawString = rawString.substring(4);
		}

		Log.d(this.getClass().getSimpleName(), "Processing OBD String:"+rawString);
		return rawString;
	}
	


}
