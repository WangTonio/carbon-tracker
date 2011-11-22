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

	private static final int MEASUREMENT_THRESHOLD = 300; //there is no sophisticated reason for this number, approx. one upload every 5 min so far
	
	public SimpleCachingStratgey(TrackCache cache, PersistenceBinder persistence, String currentForename, String currentLastname) {
		super(cache, persistence,currentForename, currentLastname);
	}

	@Override
	protected void handleCacheChange(List<TrackSummary> tracks) {
		if(tracks.size() > 1 && tracks.get(0).getMeasurementCount() > 0){
			this.invokeUpload(0);
		}
		else if (tracks.get(0).getMeasurementCount() > MEASUREMENT_THRESHOLD){
			this.invokeUpload(0);
		}
	}

}
