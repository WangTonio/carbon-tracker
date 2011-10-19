package de.unigoettingen.ct.obd;

import android.bluetooth.BluetoothSocket;
import android.location.LocationManager;

public interface MeasurementSubsystem {

	public abstract void addStatusListener(MeasurementStatusListener listener);

	public abstract void setUp(final BluetoothSocket socket, final LocationManager locationMgr);

	public abstract void startMeasurement();

	public abstract void stopMeasurement();

}