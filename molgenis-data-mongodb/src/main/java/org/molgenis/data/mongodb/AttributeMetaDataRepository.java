package org.molgenis.data.mongodb;

import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.mongodb.AttributeMetaDataMetaData.VISIBLE;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mongodb.QueryBuilder;

public class AttributeMetaDataRepository extends MongoRepositoryImpl
{
	public static final AttributeMetaDataMetaData META_DATA = new AttributeMetaDataMetaData();

	public AttributeMetaDataRepository(MongoRepositoryCollection mongoRepositoryCollection)
	{
		super(META_DATA, mongoRepositoryCollection);
	}

	public List<DefaultAttributeMetaData> getEntityAttributeMetaData(String entityName)
	{
		List<DefaultAttributeMetaData> attributes = Lists.newArrayList();
		for (Entity entity : find(QueryBuilder.start(ENTITY).is(entityName)))
		{
			attributes.add(toAttributeMetaData(entity));
		}

		return attributes;
	}

	public void addAttributeMetaData(String entityName, AttributeMetaData att)
	{
		Entity attributeMetaDataEntity = new MapEntity();
		attributeMetaDataEntity.set(ENTITY, entityName);
		attributeMetaDataEntity.set(NAME, att.getName());
		attributeMetaDataEntity.set(DATA_TYPE, att.getDataType().toString());
		attributeMetaDataEntity.set(ID_ATTRIBUTE, att.isIdAtrribute());
		attributeMetaDataEntity.set(NILLABLE, att.isNillable());
		attributeMetaDataEntity.set(AUTO, att.isAuto());
		attributeMetaDataEntity.set(VISIBLE, att.isVisible());
		attributeMetaDataEntity.set(LABEL, att.getLabel());
		attributeMetaDataEntity.set(DESCRIPTION, att.getDescription());
		attributeMetaDataEntity.set(AGGREGATEABLE, att.isAggregateable());
		attributeMetaDataEntity.set(LOOKUP_ATTRIBUTE, att.isLookupAttribute());

		if (att.getDataType() instanceof EnumField)
		{
			attributeMetaDataEntity.set(ENUM_OPTIONS, Joiner.on(",").join(att.getEnumOptions()));
		}

		if (att.getRange() != null)
		{
			attributeMetaDataEntity.set(RANGE_MIN, att.getRange().getMin());
			attributeMetaDataEntity.set(RANGE_MAX, att.getRange().getMax());
		}

		if (att.getRefEntity() != null) attributeMetaDataEntity.set(REF_ENTITY, att.getRefEntity().getName());

		add(attributeMetaDataEntity);
	}

	private DefaultAttributeMetaData toAttributeMetaData(Entity entity)
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(entity.getString(NAME));
		attributeMetaData.setDataType(MolgenisFieldTypes.getType(entity.getString(DATA_TYPE)));
		attributeMetaData.setNillable(entity.getBoolean(NILLABLE));
		attributeMetaData.setAuto(entity.getBoolean(AUTO));
		attributeMetaData.setIdAttribute(entity.getBoolean(ID_ATTRIBUTE));
		attributeMetaData.setLookupAttribute(entity.getBoolean(LOOKUP_ATTRIBUTE));
		attributeMetaData.setVisible(entity.getBoolean(VISIBLE));
		attributeMetaData.setLabel(entity.getString(LABEL));
		attributeMetaData.setDescription(entity.getString(DESCRIPTION));
		attributeMetaData.setAggregateable(entity.getBoolean(AGGREGATEABLE) == null ? false : entity
				.getBoolean(AGGREGATEABLE));
		attributeMetaData.setEnumOptions(entity.getList(ENUM_OPTIONS));

		Long rangeMin = entity.getLong(RANGE_MIN);
		Long rangeMax = entity.getLong(RANGE_MAX);
		if ((rangeMin != null) || (rangeMax != null))
		{
			attributeMetaData.setRange(new Range(rangeMin, rangeMax));
		}

		String refEntityName = entity.getString(REF_ENTITY);
		if (refEntityName != null)
		{
			EntityMetaData refEntity = mongoRepositoryCollection.getEntityMetDataRepository().getEntityMetaData(
					refEntityName);
			if (refEntity == null) throw new UnknownEntityException("Unknown entity [" + refEntityName + "]");
			attributeMetaData.setRefEntity(refEntity);
		}

		return attributeMetaData;
	}
}
