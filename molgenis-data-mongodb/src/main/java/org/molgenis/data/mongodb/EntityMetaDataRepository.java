package org.molgenis.data.mongodb;

import static org.molgenis.data.mongodb.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.mongodb.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mongodb.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.mongodb.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mongodb.EntityMetaDataMetaData.LABEL;
import static org.molgenis.data.mongodb.EntityMetaDataMetaData.NAME;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class EntityMetaDataRepository extends MongoRepositoryImpl
{
	public static final EntityMetaDataMetaData META_DATA = new EntityMetaDataMetaData();

	public EntityMetaDataRepository(MongoRepositoryCollection mongoRepositoryCollection)
	{
		super(META_DATA, mongoRepositoryCollection);
	}

	public List<DefaultEntityMetaData> getEntityMetaDatas()
	{
		List<DefaultEntityMetaData> meta = Lists.newArrayList();
		for (Entity entity : this)
		{
			meta.add(toEntityMetaData(entity));
		}

		return meta;
	}

	public List<String> getEntityNames()
	{
		List<String> names = Lists.newArrayList();
		for (Entity entity : this)
		{
			names.add(entity.getString(NAME));
		}

		return names;
	}

	public DefaultEntityMetaData getEntityMetaData(String name)
	{
		Entity entity = findOne(name);
		if (entity == null)
		{
			return null;
		}

		return toEntityMetaData(entity);
	}

	private DefaultEntityMetaData toEntityMetaData(Entity entity)
	{
		String name = entity.getString(NAME);
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
		entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
		entityMetaData.setIdAttribute(entity.getString(ID_ATTRIBUTE));
		entityMetaData.setLabel(entity.getString(LABEL));
		entityMetaData.setDescription(entity.getString(DESCRIPTION));

		// Extends
		String extendsEntityName = entity.getString(EXTENDS);
		if (extendsEntityName != null)
		{
			EntityMetaData extendsEmd = getEntityMetaData(extendsEntityName);
			if (extendsEmd == null) throw new MolgenisDataException("Missing super entity [" + extendsEntityName
					+ "] of entity [" + name + "]");
			entityMetaData.setExtends(extendsEmd);
		}

		// Add attributes
		for (DefaultAttributeMetaData attr : mongoRepositoryCollection.getAttributeMetaDataRepository()
				.getEntityAttributeMetaData(name))
		{
			entityMetaData.addAttributeMetaData(attr);
		}

		return entityMetaData;
	}

	public void addEntityMetaData(EntityMetaData emd)
	{
		Entity entityMetaDataEntity = new MapEntity();
		entityMetaDataEntity.set(NAME, emd.getName());
		entityMetaDataEntity.set(DESCRIPTION, emd.getDescription());
		entityMetaDataEntity.set(ABSTRACT, emd.isAbstract());
		if (emd.getIdAttribute() != null) entityMetaDataEntity.set(ID_ATTRIBUTE, emd.getIdAttribute().getName());
		entityMetaDataEntity.set(LABEL, emd.getLabel());
		if (emd.getExtends() != null) entityMetaDataEntity.set(EXTENDS, emd.getExtends().getName());

		add(entityMetaDataEntity);

		for (AttributeMetaData attr : emd.getAttributes())
		{
			mongoRepositoryCollection.getAttributeMetaDataRepository().addAttributeMetaData(emd.getName(), attr);
		}
	}

}
