package de.unigoettingen.ct.upload;

import de.unigoettingen.ct.data.io.Measurement;
import flexjson.JSONContext;
import flexjson.transformer.AbstractTransformer;

/**
 * Used during the JSON marshalling to transform a {@link Measurement} object into a human-readable string representation.
 * This transformer is necessary, because the default flexjson behaviour does not support optional fields.
 * This can not be changed unless the server side does the same.
 * @author Fabian Sudau
 *
 */
public class MeasurementTransformer extends AbstractTransformer {

	@Override
	public void transform(Object arg0) {
		Measurement m = (Measurement) arg0;
		JSONContext context = getContext();
		context.writeOpenObject();
		
		context.writeName("class");
		context.transform(m.getClass().getName());

		writeProperty(context, "pointOfTime", m.getPointOfTime());
		
		if(m.hasLongitude()){
			writeProperty(context, "longitude", m.getLongitude());
		}
		if(m.hasLatitude()){
			writeProperty(context, "latitude", m.getLatitude());
		}
		if(m.hasAltitude()){
			writeProperty(context, "altitude", m.getAltitude());
		}
		if(m.hasRpm()){
			writeProperty(context, "rpm", m.getRpm());
		}
		if(m.hasIat()){
			writeProperty(context, "iat", m.getIat());
		}
		if(m.hasMap()){
			writeProperty(context, "map", m.getMap());
		}
		if(m.hasSpeed()){
			writeProperty(context, "speed", m.getSpeed());
		}
		if(m.hasEot()){
			writeProperty(context, "eot", m.getEot());
		}
		if(m.hasErt()){
			writeProperty(context, "ert", m.getErt());
		}
		if(m.hasMaf()){
			writeProperty(context, "maf", m.getMaf());
		}
		if(m.hasLambda()){
			writeProperty(context, "lambda", m.getLambda());
		}
		
		context.writeCloseObject();
	}
	
	private void writeProperty(JSONContext context, String name, Object value ){
		context.writeComma();
		context.writeName(name);
		context.transform(value);
	}

}
