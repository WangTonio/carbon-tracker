package de.unigoettingen.ct.data;

public class TrackSummary {

	private int measurementCount;
	private boolean closed;
	
	public TrackSummary(int measurementCount, boolean closed) {
		this.measurementCount = measurementCount;
		this.closed = closed;
	}

	public int getMeasurementCount() {
		return measurementCount;
	}

	public boolean isClosed() {
		return closed;
	}
	
	
}
