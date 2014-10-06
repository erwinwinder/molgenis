package org.molgenis.data.convert;

import java.io.IOException;
import java.io.StringWriter;

import org.springframework.core.convert.converter.Converter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class GeometryToStringConverter implements Converter<Geometry, String>
{
	@Override
	public String convert(Geometry source)
	{
		StringWriter writer = new StringWriter();
		try
		{
			new WKTWriter().write(source, writer);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return writer.toString();
	}

}
