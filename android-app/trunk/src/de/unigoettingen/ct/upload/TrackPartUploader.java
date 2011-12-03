package de.unigoettingen.ct.upload;

import de.unigoettingen.ct.data.io.TrackPart;

/**
 * Used to upload a {@link TrackPart} as described in {@link AbstractUploader}.
 * @author Fabian Sudau
 *
 */
public class TrackPartUploader extends AbstractUploader{

	private TrackPart track;

	public TrackPartUploader(TrackPart track) {
		this.track = track;
	}

	@Override
	protected String getBodyContent() {
		return JSONMarshaller.marshalTrackPart(track);
	}

	@Override
	protected String getUrlSuffix() {
		return "TrackPart";
	}
	
	
	

	
}
