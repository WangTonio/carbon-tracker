package de.unigoettingen.ct.json;

import java.util.Calendar;

import de.unigoettingen.ct.data.DebugLog;
import de.unigoettingen.ct.data.TrackPart;
import flexjson.JSONSerializer;

public class JSONMarshaller {

	public static String marshalDebugLog(DebugLog log){
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true).transform(new CalendarTransformer(), Calendar.class);
		return serializer.deepSerialize(log);
	}
	
	public static String marshalTrackPart(TrackPart tp){
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true).transform(new CalendarTransformer(), Calendar.class);
		return serializer.deepSerialize(tp);
	}
}
