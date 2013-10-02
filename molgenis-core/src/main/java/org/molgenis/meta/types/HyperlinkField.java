package org.molgenis.meta.types;

import org.molgenis.meta.MetaDataException;

public class HyperlinkField extends DataType
{
	@Override
	public String getJavaPropertyType()
	{
		return "String";
	}

	@Override
	public String getJavaAssignment(String value)
	{
		if (value == null || value.equals("")) return "null";
		return "\"" + value + "\"";
	}

	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "VARCHAR(255)";
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		return "VARCHAR2(255)";
	}

	@Override
	public String getHsqlType()
	{
		return "TEXT";
	}

	@Override
	public String getXsdType()
	{
		return "url";
	}

	@Override
	public String getFormatString()
	{
		return "%s";
	}

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		return "string";
	}

	@Override
	public String getCppJavaPropertyType()
	{
		return "Ljava/lang/String;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return String.class;
	}

	@Override
	public String convert(Object value)
	{
		return TypeUtils.toString(value);
	}

	@Override
	public String getName()
	{
		return "hyperlink";
	}

	@Override
	public String toString(Object value)
	{
		return TypeUtils.toString(value);
	}
}
