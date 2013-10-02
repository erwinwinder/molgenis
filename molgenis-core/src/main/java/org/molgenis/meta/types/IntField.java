package org.molgenis.meta.types;

import java.util.Arrays;
import java.util.List;

import org.molgenis.meta.MetaDataException;

public class IntField extends DataType
{
	@Override
	public String getJavaPropertyType() throws MetaDataException
	{
		return "Integer";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Integer.parseInt(value);
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "INTEGER";
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		return "NUMBER (10,0)";
	}

	@Override
	public String getJavaSetterType() throws MetaDataException
	{
		return "Integer";
	}

	@Override
	public String getHsqlType()
	{
		return "INT";
	}

	@Override
	public String getXsdType()
	{
		return "int";
	}

	@Override
	public String getFormatString()
	{
		return "%d";
	}

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		return "int";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Integer;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return Integer.class;
	}

	@Override
	public Integer convert(Object value)
	{
		return TypeUtils.toInteger(value);
	}

	@Override
	public String getName()
	{
		return "int";
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
