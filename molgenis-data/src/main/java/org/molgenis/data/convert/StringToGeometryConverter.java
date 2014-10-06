package org.molgenis.data.convert;

import org.molgenis.MolgenisFieldTypes;
import org.springframework.core.convert.converter.Converter;

import com.vividsolutions.jts.geom.Geometry;

public class StringToGeometryConverter implements Converter<String, Geometry>
{

	@Override
	public Geometry convert(String source)
	{
		return (Geometry) MolgenisFieldTypes.GEOMETRY.convert(source);
	}

}
