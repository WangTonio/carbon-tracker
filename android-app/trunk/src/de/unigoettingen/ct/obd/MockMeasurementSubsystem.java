package de.unigoettingen.ct.obd;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.service.AsynchronousSubsystem;
import de.unigoettingen.ct.service.SubsystemStatus;
import de.unigoettingen.ct.service.SubsystemStatusListener;

/**
 * Mock (dummy) implementation which uses no I/O at all but is still asynchronous and fires out random data every second.
 * It does not cause exceptions and produces some delays on certain I/O operations on purpose.
 * @author Fabian Sudau
 *
 */
public class MockMeasurementSubsystem implements AsynchronousSubsystem{
	
	private ScheduledExecutorService exec;
	private TrackCache dataCache;
	private SubsystemStatusListener listener;
	
	private double lng;
	private double lat;
	
	public MockMeasurementSubsystem(TrackCache dataCache, long measurementInterval){
		this.dataCache = dataCache;
		this.exec = Executors.newSingleThreadScheduledExecutor();
		this.lat = 10D;
		this.lng = 10D;
	}

	@Override
	public void setStatusListener(SubsystemStatusListener listener) {
		this.listener = listener;
	}

	@Override
	public void setUp() {
		listener.notify(new SubsystemStatus(SubsystemStatus.States.SETTING_UP), MockMeasurementSubsystem.this);
		Runnable mockLogic = new Runnable() {		
			@Override
			public void run() {
				dataCache.matchVinOfActiveTrack("UNKNOWNVIN");
				listener.notify(new SubsystemStatus(SubsystemStatus.States.SET_UP), MockMeasurementSubsystem.this);
			}
		};
		this.exec.schedule(mockLogic, 4, TimeUnit.SECONDS);
	}

	@Override
	public void start() {
		//indicate that this is running just fine
		this.exec.execute(new Runnable() {
			@Override
			public void run() {
				listener.notify(new SubsystemStatus(SubsystemStatus.States.IN_PROGRESS), MockMeasurementSubsystem.this);
			}
		});
		
		//every second, fire out some pretty random values
		Runnable mockLogic = new Runnable() {
			@Override
			public void run() {
				Measurement m = new Measurement();
				m.setAltitude(10D);
				m.setLongitude(lng++);
				m.setLatitude(lat++);
				m.setMaf(Math.random()*100);
				m.setLambda(1);
				m.setPointOfTime(Calendar.getInstance());
				dataCache.addMeasurementToActiveTrack(m);
				Logg.log(Log.DEBUG, "MockMeasurementSS", "Added a tuple");
			}
		};
		this.exec.scheduleAtFixedRate(mockLogic, 3, 1, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		//indicate that this terminated gracefully
		//this will hopefully be the last job
		this.exec.execute(new Runnable() {
			@Override
			public void run() {
				listener.notify(new SubsystemStatus(SubsystemStatus.States.STOPPED_BY_USER), MockMeasurementSubsystem.this);
			}
		});
		this.exec.shutdown();
	}
	
	@Override
	public String toString() {
		return "MockMeasurementSys";
	}

}
