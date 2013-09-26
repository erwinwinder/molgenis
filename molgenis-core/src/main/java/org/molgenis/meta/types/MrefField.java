package org.molgenis.meta.types;

import java.text.ParseException;

import org.molgenis.meta.MetaDataException;
import org.molgenis.meta.XmlFieldMetaData;

/**
 * Many to many reference.
 * 
 * Example MOLGENIS DSL,
 * 
 * <pre>
 * <field name="myfield" type="mref" xref_entity="OtherEntity" xref_field="id" xref_label="name"/>
 * </pre>
 * 
 * This example would in the UI show a seletion box with 'name' elements.
 */
public class MrefField extends DataType
{
	@Override
	public String getJavaAssignment(String value)
	{
		return "NOT IMPLEMENTED";
	}

	@Override
	public String getJavaPropertyType() throws MetaDataException
	{
		return String.format("java.util.List<%s.%s>", f.getXrefEntity().getNamespace(), f.getXrefEntity().getName());
	}

	@Override
	public String getJavaPropertyDefault() throws MetaDataException
	{
		return String.format("new java.util.ArrayList<%s.%s>()", f.getXrefEntity().getNamespace(), f.getXrefEntity()
				.getName());
	}

	@Override
	public String getJavaSetterType() throws MetaDataException
	{
		// Entity e_ref = f.getXrefEntity();
		XmlFieldMetaData f_ref = f.getXrefField();
		return "new java.util.ArrayList<" + getFieldType(f_ref).getJavaSetterType() + ">()";
	}

	public String getJavaGetterType()
	{
		return "List";
	}

	@Override
	public String getMysqlType() throws MetaDataException
	{
		// FIXME this function should be never called???
		return getFieldType(f.getXrefField()).getMysqlType();
	}

	@Override
	public String getOracleType() throws MetaDataException
	{
		// FIXME this function should be never called???
		return getFieldType(f.getXrefField()).getOracleType();
	}

	@Override
	public String getHsqlType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getHsqlType();
	}

	@Override
	public String getXsdType() throws MetaDataException
	{
		return getFieldType(f.getXrefField()).getXsdType();
	}

	@Override
	public String getFormatString()
	{
		return "";
	}

	// @Override
	// public HtmlInput<?> createInput(String name, Class<? extends Entity>
	// xrefEntityClassName) throws HtmlInputException
	// {
	// return new MrefInput(name, xrefEntityClassName);
	// }

	@Override
	public String getCppPropertyType() throws MetaDataException
	{
		XmlFieldMetaData f_ref = f.getXrefField();
		return "vector<" + getFieldType(f_ref).getCppPropertyType() + ">";
	}

	@Override
	public String getCppJavaPropertyType() throws MetaDataException
	{
		return "Ljava/util/List;";
	}

	public Class<?> getJavaType()
	{
		return java.util.List.class;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Conversion of MRef not supported.");
	}

	public String getName()
	{
		return "mref";
	}

	public String toString(Object value)
	{
		throw new UnsupportedOperationException();

	}

}
