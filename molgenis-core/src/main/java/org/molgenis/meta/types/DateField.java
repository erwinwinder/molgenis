package org.molgenis.meta.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.meta.MetaDataException;

public class DateField extends DataType
{
	@Override
	public String getJavaPropertyType() throws MetaDataException
	{
		return "java.util.Date";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "java.sql.Date.valueOf(\"" + value + "\")";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		if (f.getAuto()) return "new java.sql.Date(new java.util.Date().getTime())";
		else return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "DATE";
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		return "DATE";
	}

	@Override
	public String getJavaSetterType() throws MetaDataException
	{
		return "Date";
	}

	@Override
	public String getHsqlType()
	{
		return "DATE";
	}

	@Override
	public String getXsdType()
	{
		return "date";
	}

	@Override
	public String getFormatString()
	{
		return "%s";
	}

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		return "time_t";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/util/Date;";
	}

	public Class<?> getJavaType()
	{
		return Date.class;
	}

	public Date getTypedValue(String value) throws ParseException
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
	}

	public String getName()
	{
		return "date";
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER", "LIKE");
	}

	@Override
	public String toString(Object value)
	{
		if (value == null) return "";
		if (value instanceof Date) return new SimpleDateFormat("MM/dd/yyyy").format((Date) value);
		throw new RuntimeException("cannot convert "+value+" to date");
	}
}
