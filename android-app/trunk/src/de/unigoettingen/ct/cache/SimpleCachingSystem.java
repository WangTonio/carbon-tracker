package de.unigoettingen.ct.cache;

import java.util.List;

import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.TrackSummary;

/**
 * This simple strategy uploads inactive tracks immediately (and with priority) and uploads active tracks when
 * they exceed 300 measurements. INTENDED TO BE A FIRST SHOT.
 * @author Fabian Sudau
 *
 */
public class SimpleCachingSystem extends AbstractCachingSystem{

	private static final int MEASUREMENT_THRESHOLD = 300; //there is no sophisticated reason for this number, approx. one upload every 5 min so far
	
	/**
	 * See {@link AbstractCachingSystem#AbstractCachingSystem(TrackCache, OngoingTrack, PersistenceBinder)}
	 * @param cache
	 * @param activeTrack
	 * @param persistence
	 * @param webServiceUrl
	 */
	public SimpleCachingSystem(TrackCache cache, OngoingTrack activeTrack, PersistenceBinder persistence, String webServiceUrl) {
		super(cache, activeTrack, persistence, webServiceUrl);
	}

	@Override
	protected void handleCacheChange(List<TrackSummary> tracks) {
		if(tracks.size() > 1 && tracks.get(0).getMeasurementCount() > 0){
			this.invokeUpload(0);
		}
		else if (tracks.size() == 1 && tracks.get(0).getMeasurementCount() > MEASUREMENT_THRESHOLD){
			this.invokeUpload(0);
		}
	}

}
