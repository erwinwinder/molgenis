package org.molgenis.meta.types;

import java.text.ParseException;

import org.molgenis.meta.MetaDataException;

public class EmailField extends DataType
{
	@Override
	public String getJavaAssignment(String value) throws MetaDataException
	{
		if(value == null || value.equals("") ) return "null";
		return "\""+value+"\"";
	}
	
	@Override
	public String getJavaPropertyDefault() throws MetaDataException
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getJavaPropertyType() throws MetaDataException
	{
		return "String";
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
	public String getHsqlType() throws MetaDataException
	{
		return "VARCHAR(255)";
	}
	@Override
	public String getXsdType() throws MetaDataException
	{
		return "string";
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

	public Class<?> getJavaType()
	{
		return String.class;
	}

	@Override
	public String getTypedValue(String value) throws ParseException
	{
		return value;
	}


	public String getName()
	{
		return "email";
	}
	
	@Override
	public String toString(Object value)
	{
		if(value == null) return "";
		return value.toString();
	}
}
