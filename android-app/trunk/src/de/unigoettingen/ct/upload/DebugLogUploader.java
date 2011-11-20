package de.unigoettingen.ct.upload;

import de.unigoettingen.ct.data.io.DebugLog;
import de.unigoettingen.ct.json.JSONMarshaller;

/**
 *  Used to upload a {@link DebugLog} as described in {@link AbstractUploader}.
 * @author Fabian Sudau
 *
 */
public class DebugLogUploader extends AbstractUploader{

	private DebugLog log;
	
	public DebugLogUploader(DebugLog log){
		this.log = log;
	}
	
	@Override
	protected String getBodyContent() {
		return JSONMarshaller.marshalDebugLog(log);
	}

	@Override
	protected String getUrlSuffix() {
		return "DebugLog";
	}

}
