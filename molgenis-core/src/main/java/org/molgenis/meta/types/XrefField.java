package org.molgenis.meta.types;

import java.text.ParseException;

import org.molgenis.meta.XmlFieldMetaData;
import org.molgenis.meta.MetaDataException;

public class XrefField extends DataType 
{	
	@Override
	public String getJavaAssignment(String value)
	{
		return "NOT IMPLEMENTED";
	}
	
	@Override
	public String getJavaPropertyType() throws MetaDataException
	{
		return f.getXrefEntity().getNamespace()+"."+f.getXrefEntity().getName();
	}
	
	@Override
	public String getJavaPropertyDefault()
	{
		if(f.getDefaultValue() == null || f.getDefaultValue() == "") return "null";
		return f.getDefaultValue();
	}
	
	public String getJavaGetterType()
	{
		return "List";
	}
	
	@Override
	public String getMysqlType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getMysqlType();
	}
	
	@Override
	public String getOracleType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getOracleType();
	}

	@Override
	public String getHsqlType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getHsqlType();
	}
	
	public String getXsdType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getXsdType();
	}

	@Override
	public String getFormatString()
	{
		return "";
	}

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		XmlFieldMetaData f_ref = f.getXrefField();
		return getFieldType(f_ref).getCppPropertyType();
	}
	
	@Override
	public String getCppJavaPropertyType() throws MetaDataException
	{
		XmlFieldMetaData f_ref = f.getXrefField();
		return getFieldType(f_ref).getCppJavaPropertyType();
	}

	@Override
	public Class<?> getJavaType()
	{
		return null;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Xref conversion not supported.");
	}
	
	public String getName()
	{
		return "xref";
	}
	
	@Override
	public String toString(Object value)
	{
		if(value == null) return "";
		return value.toString();
	}

}
