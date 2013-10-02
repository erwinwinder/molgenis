package org.molgenis.meta.types;

import java.util.Arrays;
import java.util.List;

import org.molgenis.meta.MetaDataException;

public class LongField extends DataType
{
	@Override
	public String getJavaPropertyType()
	{
		return "Long";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Long.parseLong(value) + "L";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "BIGINT";
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		return "NUMBER (19,0)";
	}

	@Override
	public String getHsqlType()
	{
		return "LONG";
	}

	@Override
	public String getXsdType()
	{
		return "boolean";
	}

	@Override
	public String getFormatString()
	{
		return "%d";
	}

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		return "long";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Long;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Long.class;
	}

	@Override
	public Long convert(Object value)
	{
		return TypeUtils.toLong(value);
	}

	@Override
	public String getName()
	{
		return "long";
	}

	@Override
	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS", "LESS", "GREATER");
	}

	@Override
	public String toString(Object value)
	{
		if (value == null) return "";
		return value.toString();
	}

}
