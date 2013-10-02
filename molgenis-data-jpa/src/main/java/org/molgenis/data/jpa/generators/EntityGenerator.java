package org.molgenis.data.jpa.generators;

import org.molgenis.meta.EntityMetaData;

public class EntityGenerator extends ForEachEntityGenerator
{
	public EntityGenerator()
	{
		super(true);
	}

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType()
	{
		return "";
	}

	@Override
	public String getPackageName(EntityMetaData entity)
	{
		return entity.getNamespace();
	}

}
