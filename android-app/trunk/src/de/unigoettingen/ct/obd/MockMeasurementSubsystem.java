package de.unigoettingen.ct.obd;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothSocket;
import android.location.LocationManager;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.Measurement;

/**
 * Mock (dummy) implementation which uses no I/O at all but is still asynchronous and fires out random data every second.
 * It does not cause exceptions and produces some delays on certain I/O operations on purpose.
 * @author Fabian Sudau
 *
 */
public class MockMeasurementSubsystem implements MeasurementSubsystem{
	
	private ScheduledExecutorService exec;
	private TrackCache dataCache;
	private MeasurementStatusListener listener;
	
	private double lng;
	private double lat;
	
	public MockMeasurementSubsystem(TrackCache dataCache, long measurementInterval){
		this.dataCache = dataCache;
		this.exec = Executors.newSingleThreadScheduledExecutor();
		this.lat = 10D;
		this.lng = 10D;
	}

	@Override
	public void addStatusListener(MeasurementStatusListener listener) {
		this.listener = listener;
	}

	@Override
	public void setUp(BluetoothSocket socket, LocationManager locationMgr) {
		listener.notify(new MeasurementStatus(MeasurementStatus.States.SETTING_UP), MockMeasurementSubsystem.this);
		Runnable mockLogic = new Runnable() {		
			@Override
			public void run() {
				listener.notify(new MeasurementStatus(MeasurementStatus.States.SET_UP), MockMeasurementSubsystem.this);
			}
		};
		this.exec.schedule(mockLogic, 4, TimeUnit.SECONDS);
	}

	@Override
	public void startMeasurement() {
		//indicate that this is running just fine
		this.exec.execute(new Runnable() {
			@Override
			public void run() {
				listener.notify(new MeasurementStatus(MeasurementStatus.States.IN_PROGRESS), MockMeasurementSubsystem.this);
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
				dataCache.addMeasurementToActiveRoute(m);
			}
		};
		this.exec.scheduleAtFixedRate(mockLogic, 3, 1, TimeUnit.SECONDS);
	}

	@Override
	public void stopMeasurement() {
		//indicate that this terminated gracefully
		//this will hopefully be the last job
		this.exec.execute(new Runnable() {
			@Override
			public void run() {
				listener.notify(new MeasurementStatus(MeasurementStatus.States.STOPPED_BY_USER), MockMeasurementSubsystem.this);
			}
		});
		try {
			this.exec.awaitTermination(3, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
