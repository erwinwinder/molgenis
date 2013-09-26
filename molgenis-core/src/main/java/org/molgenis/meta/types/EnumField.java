package org.molgenis.meta.types;

import java.text.ParseException;

import org.molgenis.meta.MetaDataException;

public class EnumField extends DataType
{
	@Override
	public String getJavaPropertyType()
	{
		return "String";
	}
	
	@Override
	public String getJavaAssignment(String value)
	{
		if(value == null || value.equals("") ) return "null";
		return "\""+value+"\"";
	}
	
	@Override
	public String getJavaPropertyDefault()
	{
		return getJavaAssignment(f.getDefaultValue());
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		return "ENUM("+this.toCsv(f.getEnumOptions())+")";
	}
	
	@Override
	public String getOracleType() throws MetaDataException
	{
		return "VARCHAR2(255)";
	}


	@Override
	public String getHsqlType()
	{
		return "VARCHAR(1024)";
	}
	@Override
	public String getXsdType()
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

	@Override
	public Class<?> getJavaType()
	{
		return Enum.class;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Unable to cast enum type");
	}
	
	public String getName()
	{
		return "enum";
	}
	
	@Override
	public String toString(Object value)
	{
		if(value == null) return "";
		return value.toString();
	}
}
