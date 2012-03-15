package de.unigoettingen.ct.upload;

import java.util.Calendar;

import flexjson.transformer.AbstractTransformer;

/**
 * Used during the JSON marshalling to transform a {@link java.util.Calendar} object into a human-readable string representation.
 * This can not be changed unless the server side does the same.
 * @author Fabian Sudau
 *
 */
public class CalendarTransformer extends AbstractTransformer{

	public void transform(Object object) {
		this.getContext().writeQuoted(de.unigoettingen.ct.data.io.DateUtils.calendarToString((Calendar) object));
	}

}
