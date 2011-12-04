package de.unigoettingen.ct.upload;

import de.unigoettingen.ct.data.io.DebugLog;

/**
 *  Used to upload a {@link DebugLog} as described in {@link AbstractUploader}.
 * @author Fabian Sudau
 *
 */
public class DebugLogUploader extends AbstractUploader{

	private DebugLog log;
	
	public DebugLogUploader(String webServiceUrl, DebugLog log){
		super(webServiceUrl);
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
