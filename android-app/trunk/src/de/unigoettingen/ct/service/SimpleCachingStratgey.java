package de.unigoettingen.ct.service;

import java.util.List;

import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.TrackSummary;

/**
 * This simple strategy uploads inactive tracks immediately (and with priority) and uploads active tracks when
 * they exceed 60 measurements. INTENDED TO BE A FIRST SHOT.
 * @author Fabian Sudau
 *
 */
public class SimpleCachingStratgey extends AbstractCachingStrategy{

	private static final int MEASUREMENT_THRESHOLD = 10; //there is no sophisticated reason for this number
	
	public SimpleCachingStratgey(TrackCache cache) {
		super(cache);
	}

	@Override
	protected void handleCacheChange(List<TrackSummary> tracks) {
		if(tracks.size() > 1){
			this.invokeUpload(0);
		}
		else if (tracks.get(0).getMeasurementCount() > MEASUREMENT_THRESHOLD){
			this.invokeUpload(0);
		}
	}

}
