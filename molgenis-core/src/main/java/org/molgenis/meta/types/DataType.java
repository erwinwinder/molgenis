package org.molgenis.meta.types;

import java.util.Arrays;
import java.util.List;

import org.molgenis.FieldTypes;
import org.molgenis.meta.FieldMetaData;
import org.molgenis.meta.MetaDataException;

/**
 * Definition of a MOLGENIS field type. For example <field name="x" type="string" would relate to type StringField
 */
public abstract class DataType
{
	/**
	 * For xref purposes we sometimes need a handle of the field this type was defined as part of.
	 */
	protected FieldMetaData f;

	/**
	 * Get the field type from a field. Equal to field.getType();
	 * 
	 * @param f
	 * @return
	 * @throws MetaDataException
	 */
	public DataType getFieldType(FieldMetaData f) throws MetaDataException
	{
		return FieldTypes.get(f);
	}

	/**
	 * 
	 * @return
	 * @throws MetaDataException
	 */
	public String getJavaSetterType() throws MetaDataException
	{
		return this.getJavaPropertyType();
	}

	public String getJavaGetterType() throws MetaDataException
	{
		return this.getJavaSetterType();
	}

	/**
	 * Product the Java type of this field type. Default: "String".
	 * 
	 * @return type in java code
	 * @throws MetaDataException
	 */
	abstract public String getJavaPropertyType() throws MetaDataException;

	/**
	 * Product the Java type of this field type. Default: "String".
	 * 
	 * @return type in java code
	 * @throws MetaDataException
	 */
	abstract public String getCppPropertyType() throws MetaDataException;

	/**
	 * Produce a valid Java snippet to set the default of a field, using the 'getDefault' function of that field.
	 * Default: "\""+f.getDefault()+"\"".
	 * 
	 * @return default in java code
	 * @throws MetaDataException
	 */
	abstract public String getJavaPropertyDefault() throws MetaDataException;

	/**
	 * Produce a valid Java snippet to set a value for field.
	 * 
	 * @return default in java code
	 * @throws MetaDataException
	 */
	public abstract String getJavaAssignment(String value) throws MetaDataException;

	/**
	 * Produce the Java class corresponding to the value
	 * 
	 * @return Java class
	 * @throws MetaDataException
	 */
	public abstract Class<?> getJavaType() throws MetaDataException;

	/**
	 * Produce a valid mysql snippet indicating the mysql type. E.g. "BOOL".
	 * 
	 * @return mysql type string
	 * @throws MetaDataException
	 */
	abstract public String getMysqlType() throws MetaDataException;

	/**
	 * Produce valid XSD type
	 */
	abstract public String getXsdType() throws MetaDataException;

	/**
	 * Convert a list of string to comma separated values.
	 * 
	 * @param elements
	 * @return csv
	 */
	public String toCsv(List<String> elements)
	{
		String result = "";

		for (String str : elements)
		{
			result += ((elements.get(0) == str) ? "" : ",") + "'" + str + "'";
		}

		return result;
	}

	/**
	 * Produce a valid hsql snippet indicating the mysql type. E.g. "BOOL".
	 * 
	 * @return hsql type string
	 * @throws MetaDataException
	 */
	public abstract String getHsqlType() throws MetaDataException;

	public void setField(FieldMetaData f)
	{
		this.f = f;
	}

	/**
	 * Get the format string, e.g. '%s'
	 * 
	 * @return
	 */
	public abstract String getFormatString();

	/**
	 * The string value of this type, e.g. 'int' or 'xref'.
	 */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName().replace("Field", "").toLowerCase();
	}

	public abstract String getCppJavaPropertyType() throws MetaDataException;

	public abstract String getOracleType() throws MetaDataException;

	public abstract Object convert(Object value);

	public abstract String getName();

	public List<String> getAllowedOperators()
	{
		return Arrays.asList("EQUALS", "NOT EQUALS");
	}

	public abstract String toString(Object value);

}
