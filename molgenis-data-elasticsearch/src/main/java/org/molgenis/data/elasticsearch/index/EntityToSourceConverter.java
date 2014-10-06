package org.molgenis.data.elasticsearch.index;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Converts entities to Elasticsearch documents
 */
@Component
public class EntityToSourceConverter
{
	/**
	 * Converts entity to Elasticsearch document
	 * 
	 * @param entity
	 * @param entityMetaData
	 * @return
	 */
	public Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData)
	{
		return convert(entity, entityMetaData, true);
	}

	/**
	 * Converts entity to Elasticsearch document
	 * 
	 * @param entity
	 * @param entityMetaData
	 * @return
	 */
	private Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData, boolean nestRefs)
	{
		Map<String, Object> doc = new HashMap<String, Object>();

		for (AttributeMetaData attributeMetaData : entityMetaData.getAtomicAttributes())
		{
			String attrName = attributeMetaData.getName();
			Object value = convertAttribute(entity, attributeMetaData, nestRefs);
			doc.put(attrName, value);
		}

		return doc;
	}

	private Object convertAttribute(Entity entity, AttributeMetaData attributeMetaData, final boolean nestRefs)
	{
		Object value;

		String attrName = attributeMetaData.getName();
		FieldTypeEnum dataType = attributeMetaData.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
				value = entity.getBoolean(attrName);
				break;
			case DECIMAL:
				value = entity.getDouble(attrName);
				break;
			case INT:
				value = entity.getInt(attrName);
				break;
			case LONG:
				value = entity.getLong(attrName);
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = entity.getString(attrName);
				break;
			case DATE:
				Date date = entity.getDate(attrName);
				value = date != null ? MolgenisDateFormat.getDateFormat().format(date) : null;
				break;
			case DATE_TIME:
				Date dateTime = entity.getDate(attrName);
				value = dateTime != null ? MolgenisDateFormat.getDateTimeFormat().format(dateTime) : null;
				break;
			case CATEGORICAL:
			case XREF:
			{
				Entity xrefEntity = entity.getEntity(attrName);
				if (xrefEntity != null)
				{
					EntityMetaData xrefEntityMetaData = attributeMetaData.getRefEntity();
					if (nestRefs)
					{
						value = convert(xrefEntity, xrefEntityMetaData, false);
					}
					else
					{
						value = convertAttribute(xrefEntity, xrefEntityMetaData.getLabelAttribute(), false);
					}
				}
				else
				{
					value = null;
				}
				break;
			}
			case MREF:
			{
				final Iterable<Entity> refEntities = entity.getEntities(attrName);
				if (refEntities != null && !Iterables.isEmpty(refEntities))
				{
					final EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
					value = Lists.newArrayList(Iterables.transform(refEntities, new Function<Entity, Object>()
					{
						@Override
						public Object apply(Entity refEntity)
						{
							if (nestRefs)
							{
								return convert(refEntity, refEntityMetaData, false);
							}
							else
							{
								return convertAttribute(refEntity, refEntityMetaData.getLabelAttribute(), false);
							}
						}
					}));
				}
				else
				{
					value = null;
				}
				break;
			}
			case GEOMETRY:
				Geometry geometry = entity.getGeometry(attrName);
				if (geometry == null)
				{
					value = null;
				}
				else
				{
					if (geometry instanceof Point)
					{
						value = ShapeBuilder.newPoint(((Point) geometry).getCoordinate());
					}
					else if (geometry instanceof Polygon)
					{
						Polygon poly = ((Polygon) geometry);
						PolygonBuilder pb = ShapeBuilder.newPolygon().points(poly.getExteriorRing().getCoordinates());
						for (int i = 0; i < poly.getNumInteriorRing(); i++)
						{
							pb.hole().points(poly.getInteriorRingN(i).getCoordinates()).close();
						}
						pb.close();

						value = pb;
					}
					else
					{
						throw new IllegalArgumentException("Unsupported geometry type [" + geometry + "]");
					}

				}

				break;
			case COMPOUND:
				throw new RuntimeException("Compound attribute is not an atomic attribute");
			case FILE:
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type for indexing [" + dataType + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return value;
	}
}
