package de.unigoettingen.ct.upload;

import java.util.Calendar;

import de.unigoettingen.ct.data.io.DebugLog;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.data.io.TrackPart;
import flexjson.JSONSerializer;

/**
 * Provides static methods transforming certain data objects into JSON string representations.
 * These representations are understood by the server side.
 * @author Fabian Sudau
 *
 */
public class JSONMarshaller {

	public static String marshalDebugLog(DebugLog log){
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true).transform(new CalendarTransformer(), Calendar.class);
		return serializer.deepSerialize(log);
	}
	
	public static String marshalTrackPart(TrackPart tp){
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true).transform(new CalendarTransformer(), Calendar.class).
		transform(new MeasurementTransformer(), Measurement.class);
		return serializer.deepSerialize(tp);
	}
}
