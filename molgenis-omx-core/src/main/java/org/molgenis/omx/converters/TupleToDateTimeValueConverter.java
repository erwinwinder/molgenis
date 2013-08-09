package org.molgenis.omx.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToDateTimeValueConverter implements TupleToValueConverter<DateTimeValue, String>
{
	private static final String DATEFORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String DATEFORMAT_DATETIME_MSEC = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	@Override
	public DateTimeValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		String dateTimeStr = tuple.getString(colName);
		if (dateTimeStr == null) return null;

		SimpleDateFormat iso8601DateTimeFormat = new SimpleDateFormat(DATEFORMAT_DATETIME);
		DateTimeValue dateTimeValue = new DateTimeValue();
		try
		{
			dateTimeValue.setValue(iso8601DateTimeFormat.parse(dateTimeStr));
		}
		catch (ParseException e)
		{
			// Try with msecs
			try
			{
				dateTimeValue.setValue(new SimpleDateFormat(DATEFORMAT_DATETIME_MSEC).parse(dateTimeStr));
			}
			catch (ParseException e1)
			{
				throw new ValueConverterException(e);
			}
		}
		return dateTimeValue;
	}

	@Override
	public String extractValue(Value value)
	{
		SimpleDateFormat iso8601DateTimeFormat = new SimpleDateFormat(DATEFORMAT_DATETIME);
		return iso8601DateTimeFormat.format(((DateTimeValue) value).getValue());
	}
}
