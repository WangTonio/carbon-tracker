package de.unigoettingen.ct.obd;

public interface MeasurementStatusListener {
	
	public void notify(MeasurementStatus status, MeasurementFacade sender);

}
