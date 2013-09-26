package org.molgenis.meta.types;

import org.molgenis.meta.MetaDataException;

public class BoolField extends DataType
{
	@Override
	public String getJavaPropertyType()
	{
		return "Boolean";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "" + Boolean.parseBoolean(value.toString());
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "BOOL";
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		return "CHAR";
	}

	@Override
	public String getHsqlType()
	{
		return "INTEGER";
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
		return "bool";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/Boolean;";
	}

	public Class<?> getJavaType()
	{
		return Boolean.class;
	}

	public Boolean getTypedValue(String value)
	{
		return Boolean.parseBoolean(value);
	}

	public String getName()
	{
		return "bool";
	}

	@Override
	public String toString(Object value)
	{
		if (value == null) return "";
		if (value instanceof Boolean && (Boolean) value == true) return "true";
		if (value instanceof String && Boolean.parseBoolean((String) value) == true) return "true";
		return "false";
	}
}
