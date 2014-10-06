package org.molgenis.fieldtypes;

import java.text.ParseException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryField extends FieldType
{
	private static final long serialVersionUID = 8478955568678248384L;

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		return Geometry.class.getCanonicalName();
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getJavaPropertyDefault() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getJavaAssignment(String value) throws MolgenisModelException
	{
		return null;
	}

	@Override
	public Class<?> getJavaType() throws MolgenisModelException
	{
		return Point.class;
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		return "GEOMETRY";
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getFormatString()
	{
		return "%s";
	}

	@Override
	public String getCppJavaPropertyType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		return null;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		try
		{
			return new WKTReader().read(value);
		}
		catch (com.vividsolutions.jts.io.ParseException e)
		{
			throw new ParseException(e.getMessage(), 0);
		}
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.GEOMETRY;
	}

	@Override
	public Object convert(Object value)
	{
		if (value == null) return null;
		if (value instanceof Geometry) return value;
		if (value instanceof String)
		{
			try
			{
				return getTypedValue((String) value);
			}
			catch (ParseException e)
			{
				throw new RuntimeException("GeometryField.convert(" + value + ") failed", e);
			}
		}

		throw new RuntimeException("GeometryField.convert(" + value + ") failed");
	}
}
