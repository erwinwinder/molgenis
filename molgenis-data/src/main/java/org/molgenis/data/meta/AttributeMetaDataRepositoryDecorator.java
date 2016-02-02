package org.molgenis.data.meta;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.security.core.Permission;
import org.molgenis.util.SecurityDecoratorUtils;

public class AttributeMetaDataRepositoryDecorator implements Repository
{
	private final Repository decorated;
	private final DataService dataService;
	private final LanguageService languageService;

	public AttributeMetaDataRepositoryDecorator(Repository decorated, DataService dataService,
			LanguageService languageService)
	{
		this.decorated = Objects.requireNonNull(decorated);
		this.dataService = Objects.requireNonNull(dataService);
		this.languageService = Objects.requireNonNull(languageService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decorated.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decorated.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decorated.findOne(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		updateBackend(entity);
		decorated.update(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		decorated.update(entities.filter(entity -> {
			updateBackend(entity);
			return true;
		}));
	}

	private void updateBackend(Entity attributeMeta)
	{
		MetaValidationUtils.validateName(attributeMeta.getString(AttributeMetaDataMetaData.NAME));

		Entity entityMeta = getAttributeEntityMeta(attributeMeta);
		if (entityMeta != null)
		{
			String entityName = entityMeta.getString(EntityMetaDataMetaData.FULL_NAME);
			SecurityDecoratorUtils.validatePermission(entityName, Permission.WRITEMETA);

			getBackend(entityMeta).updateAttribute(entityName,
					DefaultAttributeMetaData.fromAttributeEntity(entityMeta, dataService, languageService));
		}
	}

	@Override
	public void delete(Entity entity)
	{
		deleteBackendAttribute(entity);
		decorated.delete(entity);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decorated.add(entities.filter(entity -> {
			deleteBackendAttribute(entity);
			return true;
		}));
	}

	private void deleteBackendAttribute(Entity attributeMeta)
	{
		Entity entityMeta = getAttributeEntityMeta(attributeMeta);
		if (entityMeta != null)
		{
			String entityName = entityMeta.getString(EntityMetaDataMetaData.FULL_NAME);
			SecurityDecoratorUtils.validatePermission(entityName, Permission.WRITEMETA);
			getBackend(entityMeta).deleteAttribute(entityName, attributeMeta.getString(AttributeMetaDataMetaData.NAME));
		}
	}

	@Override
	public void deleteById(Object id)
	{
		decorated.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		decorated.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		addBackendAttribute(entity);
		decorated.add(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return decorated.add(entities.filter(entity -> {
			addBackendAttribute(entity);
			return true;
		}));
	}

	private void addBackendAttribute(Entity attributeMeta)
	{
		MetaValidationUtils.validateName(attributeMeta.getString(AttributeMetaDataMetaData.NAME));

		Entity entityMeta = getAttributeEntityMeta(attributeMeta);
		if (entityMeta != null)
		{
			String entityName = entityMeta.getString(EntityMetaDataMetaData.FULL_NAME);
			SecurityDecoratorUtils.validatePermission(entityName, Permission.WRITEMETA);

			getBackend(entityMeta).addAttribute(entityName,
					DefaultAttributeMetaData.fromAttributeEntity(entityMeta, dataService, languageService));
		}
	}

	@Override
	public void flush()
	{
		decorated.flush();
	}

	@Override
	public void clearCache()
	{
		decorated.clearCache();
	}

	@Override
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decorated.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decorated.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decorated.removeEntityListener(entityListener);
	}

	// Get the backend the entity belongs to
	private ManageableRepositoryCollection getBackend(Entity entityMeta)
	{
		RepositoryCollection backend = dataService.getMeta().getBackend(
				entityMeta.getString(EntityMetaDataMetaData.BACKEND));

		if (backend == null)
		{
			backend = dataService.getMeta().getDefaultBackend();
		}

		if (!(backend instanceof ManageableRepositoryCollection))
		{
			throw new MolgenisDataException(backend.getName() + " is not manageable");
		}

		return ((ManageableRepositoryCollection) backend);

	}

	// Get entity the attributes belong to
	private Entity getAttributeEntityMeta(Entity attributeMeta)
	{
		String identifier = attributeMeta.getString(AttributeMetaDataMetaData.IDENTIFIER);

		return dataService.query(EntityMetaDataMetaData.ENTITY_NAME).eq(EntityMetaDataMetaData.ATTRIBUTES, identifier)
				.findOne();
	}

}
