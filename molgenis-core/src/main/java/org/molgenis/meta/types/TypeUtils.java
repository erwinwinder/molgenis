package org.molgenis.meta.types;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.util.ListEscapeUtils;

/**
 * Helper methods to convert object to types.
 * 
 * TODO: decide on better name.
 */
public class TypeUtils
{
	public static boolean isNull(Object value)
	{
		return value == null;
	}

	public static String toString(Object value)
	{
		if (value == null) return null;
		else if (value instanceof String) return (String) value;
		else return value.toString();
	}

	public static Integer toInteger(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Integer) return (Integer) value;
		else return Integer.parseInt(value.toString());
	}

	public static Long toLong(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Long) return (Long) value;
		else return Long.parseLong(value.toString());
	}

	public static Boolean toBoolean(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Boolean) return (Boolean) value;
		else
		{
			String str = value.toString();
			return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1");
		}
	}

	public static Double toDouble(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Double) return (Double) value;
		else return Double.parseDouble(value.toString());
	}

	public static Date toDate(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Date) return (Date) value;
		else return Date.valueOf(value.toString());
	}

	public static Timestamp toTimestamp(Object value)
	{
		if (value == null) return null;
		else if (value instanceof Timestamp) return (Timestamp) value;
		else if (value instanceof Date) return new Timestamp(((Date) value).getTime());
		else return Timestamp.valueOf(value.toString());
	}

	@SuppressWarnings("unchecked")
	public static List<String> toList(Object value)
	{
		if (value == null) return null;
		else if (value instanceof List<?>) return (List<String>) value;
		else if (value instanceof String) return ListEscapeUtils.toList((String) value);
		else return ListEscapeUtils.toList(value.toString());
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> toList(Class<E> klass, Object value)
	{
		if (value == null) return null;
		else if (value instanceof List<?>)
		{
			List<E> list = (List<E>) value;
			boolean valid = true;
			for (E e : list)
			{
				if (!klass.isInstance(e))
				{
					valid = false;
					break;
				}
			}
			if (valid) return list;
			else
			{
				throw new RuntimeException("cannot convert toList(" + klass.getSimpleName() + ", list");
			}
		}
		else if (value instanceof String)
		{
			// List<String> list = ListEscapeUtils.toList((String) value);

			// todo: converts strings to types?
			throw new RuntimeException("cannot convert toList(" + klass.getSimpleName() + ", list");
		}
		else
		{
			throw new RuntimeException("cannot convert toList(" + klass.getSimpleName() + ", list");
		}
	}
}