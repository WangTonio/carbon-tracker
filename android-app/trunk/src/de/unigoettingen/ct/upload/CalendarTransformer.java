package de.unigoettingen.ct.upload;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;

public class CalendarTransformer extends AbstractTransformer implements ObjectFactory{

	public void transform(Object object) {
		this.getContext().writeQuoted(de.unigoettingen.ct.data.io.DateUtils.calendarToString((Calendar) object));
	}

	@SuppressWarnings("rawtypes")
	public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
		try{
			return de.unigoettingen.ct.data.io.DateUtils.parseTimestamp((String) value);
		}
		catch(ParseException e){
			throw new RuntimeException(e);
		}
	}
	

}
