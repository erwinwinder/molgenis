package org.molgenis.data;

import org.molgenis.AttributeMetaData;
import org.molgenis.Entity;
import org.molgenis.EntityMetaData;

public abstract class AbstractEntity implements Entity
{
	private static final long serialVersionUID = -8726137525751922688L;
	private final EntityMetaData entityMetaData;

	protected AbstractEntity(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public EntityMetaData getMetaData()
	{
		return entityMetaData;
	}

	@Override
	public void set(Entity entity)
	{
		for (AttributeMetaData attribute : entity.getMetaData().getAttributes())
		{
			if (entity.get(attribute.getName()) != null)
			{
				set(attribute.getName(), entity.get(attribute.getName()));
			}
		}
	}

	@Override
	public Integer getIdValue()
	{
		AttributeMetaData idAttribute = getMetaData().getIdAttribute();
		if (idAttribute == null)
		{
			throw new MolgenisDataException("No id attribute specified");
		}

		Object id = get(idAttribute.getName());
		if (!id.getClass().isAssignableFrom(Integer.class))
		{
			throw new MolgenisDataException("Id attribute should be of type Integer but is of type [" + id.getClass()
					+ "]");
		}

		return (Integer) id;
	}

	@Override
	public String getLabelValue()
	{
		AttributeMetaData labelAttribute = getMetaData().getLabelAttribute();
		if (labelAttribute == null)
		{
			throw new MolgenisDataException("No label attribute specified");
		}

		Object label = get(labelAttribute.getName());
		return labelAttribute.getDataType().toString(label);
	}

}
