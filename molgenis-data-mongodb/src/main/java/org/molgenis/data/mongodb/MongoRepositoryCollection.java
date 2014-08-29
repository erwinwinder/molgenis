package org.molgenis.data.mongodb;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;

import com.mongodb.DB;

public class MongoRepositoryCollection implements RepositoryCollection
{
	private final DB database;
	private final EntityMetaDataRepository entityMetDataRepository;
	private final AttributeMetaDataRepository attributeMetaDataRepository;

	public MongoRepositoryCollection(DB database)
	{
		this.database = database;
		this.entityMetDataRepository = new EntityMetaDataRepository(this);
		this.attributeMetaDataRepository = new AttributeMetaDataRepository(this);
	}

	public DB getMongoDB()
	{
		return database;
	}

	protected EntityMetaDataRepository getEntityMetDataRepository()
	{
		return entityMetDataRepository;
	}

	protected AttributeMetaDataRepository getAttributeMetaDataRepository()
	{
		return attributeMetaDataRepository;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return entityMetDataRepository.getEntityNames();
	}

	@Override
	public MongoRepository getRepositoryByEntityName(String name)
	{
		EntityMetaData emd = entityMetDataRepository.getEntityMetaData(name);
		if (emd == null)
		{
			return null;
		}

		return new MongoRepositorySecurityDecorator(new MongoRepositoryImpl(emd, this));
	}

	public MongoRepository add(EntityMetaData emd)
	{
		entityMetDataRepository.addEntityMetaData(emd);
		return getRepositoryByEntityName(emd.getName());
	}

}
