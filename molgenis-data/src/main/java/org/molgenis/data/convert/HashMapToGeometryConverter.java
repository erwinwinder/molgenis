package org.molgenis.data.convert;

import java.io.IOException;
import java.util.HashMap;

import org.geotools.geojson.geom.GeometryJSON;
import org.springframework.core.convert.converter.Converter;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Geometry;

public class HashMapToGeometryConverter implements Converter<HashMap<?, ?>, Geometry>
{
	@Override
	public Geometry convert(HashMap<?, ?> source)
	{
		try
		{
			String type = (String) source.get("type");
			if (type == null)
			{
				throw new IllegalArgumentException("Missing geometry type in [" + source + "]");
			}

			if (type.equalsIgnoreCase("point"))
			{
				return new GeometryJSON().readPoint(new Gson().toJson(source));
			}

			if (type.equalsIgnoreCase("polygon"))
			{
				return new GeometryJSON().readPolygon(new Gson().toJson(source));
			}

			throw new IllegalArgumentException("Unsupported geometry type [" + type + "]");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to convert (" + source + ") to geometry", e);
		}
	}
}
