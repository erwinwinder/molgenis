package org.molgenis.omx.datasetdeleter;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSetDeleterServiceImpl implements DataSetDeleterService
{
	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public DataSetDeleterServiceImpl(DataService dataService, SearchService searchService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (searchService == null) throw new IllegalArgumentException("Search service is null");
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	@Transactional
	public String deleteData(String dataSetIdentifier, boolean deleteMetadata)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);

		deleteData(dataSet);
		searchService.deleteDocumentsByType(dataSet.getIdentifier());

		if (deleteMetadata)
		{
			List<Entity> entitiesList = dataService.findAllAsList(ObservedValue.ENTITY_NAME, new QueryImpl());
			entitiesList.addAll(dataService.findAllAsList(Protocol.ENTITY_NAME, new QueryImpl()));
			entitiesList.addAll(dataService.findAllAsList(DataSet.ENTITY_NAME, new QueryImpl()));
			entitiesList.remove(dataSet);

			Protocol protocolUsed = dataSet.getProtocolUsed();
			deleteProtocol(protocolUsed, entitiesList);
		}

		return dataSet.getName();
	}

	/**
	 * Deletes the data from a given dataSet
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            DataSet from which the data should be deleted
	 */
	void deleteData(DataSet dataset)
	{
		int count = 0;
		List<ObservationSet> observationSets = dataService.findAllAsList(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataset));

		List<ObservedValue> observedValues = new ArrayList<ObservedValue>();
		for (ObservationSet observationSet : observationSets)
		{
			List<ObservedValue> list = dataService.findAllAsList(ObservedValue.ENTITY_NAME,
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet));

			observedValues.addAll(list);

			if (count % 20 == 0)
			{
				dataService.delete(ObservedValue.ENTITY_NAME, observedValues);
				observedValues = new ArrayList<ObservedValue>();
			}
			count++;
		}
		if (observedValues.size() != 0)
		{
			dataService.delete(ObservedValue.ENTITY_NAME, observedValues);
		}
		dataService.delete(ObservationSet.ENTITY_NAME, observationSets);
		dataService.delete(DataSet.ENTITY_NAME, dataset);
	}

	/**
	 * Deletes all subprotocols which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            protocols that should be deleted
	 */
	List<Entity> deleteProtocol(Protocol protocol, List<Entity> entitiesList)
	{
		boolean deleteInBatch = true;
		List<Protocol> subprotocolsToDelete = protocol.getSubprotocols();
		List<Protocol> subprotocolsToKeep = new ArrayList<Protocol>();

		// check if any of the subprotocols had subprotocols of its own
		for (Protocol subprotocol : subprotocolsToDelete)
		{
			if (subprotocol.getSubprotocols().size() > 0) deleteInBatch = false;
		}
		for (Protocol subprotocol : subprotocolsToDelete)
		{
			int superprotocolcount = countReferringEntities(subprotocol, entitiesList);
			if (superprotocolcount == 1)
			{
				if (!deleteInBatch)
				{
					entitiesList = deleteProtocol(subprotocol, entitiesList);
				}
				else
				{
					List<ObservableFeature> subFeatures = subprotocol.getFeatures();
					deleteFeatures(subFeatures, entitiesList);
				}
			}
			else
			{
				subprotocolsToKeep.add(subprotocol);
			}
		}
		if (deleteInBatch)
		{
			subprotocolsToDelete.removeAll(subprotocolsToKeep);
			dataService.delete(Protocol.ENTITY_NAME, subprotocolsToDelete);
			entitiesList.removeAll(subprotocolsToDelete);
		}
		List<ObservableFeature> features = protocol.getFeatures();
		entitiesList.remove(protocol);
		deleteFeatures(features, entitiesList);
		dataService.delete(Protocol.ENTITY_NAME, protocol);

		return entitiesList;
	}

	/**
	 * Deletes all features which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            features that should be deleted
	 */
	void deleteFeatures(List<ObservableFeature> features, List<Entity> entitiesList)
	{
		List<ObservableFeature> removableFeatures = new ArrayList<ObservableFeature>();

		for (ObservableFeature feature : features)
		{
			List<Category> categories = dataService.findAllAsList(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature));
			deleteCategories(categories);

			int entityCount = countReferringEntities(feature, entitiesList);
			if (entityCount <= 1)
			{
				removableFeatures.add(feature);
			}
		}
		dataService.delete(ObservableFeature.ENTITY_NAME, removableFeatures);
	}

	/**
	 * Note: package-private for testability
	 * 
	 * @param categories
	 */
	void deleteCategories(List<Category> categories)
	{
		for (Category category : categories)
		{
			List<CategoricalValue> categoricalValues = dataService.findAllAsList(CategoricalValue.ENTITY_NAME,
					new QueryImpl().eq(CategoricalValue.VALUE, category));

			for (CategoricalValue cat : categoricalValues)
			{
				dataService.delete(CategoricalValue.ENTITY_NAME, cat);
			}
			dataService.delete(Category.ENTITY_NAME, category);
		}
	}

	/**
	 * Count the number of times a protocol of feature is referred to from a(n other) protocol
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            feature of protocol that is referred to
	 * 
	 * @return the number of referring protocols
	 */
	int countReferringEntities(Characteristic characteristic, List<Entity> entitiesList)
	{
		int entityCount = 0;
		Class<? extends Characteristic> clazz = characteristic.getClass();

		for (Entity entity : entitiesList)
		{
			if (entity.getClass().equals(Protocol.class))
			{
				Protocol protocol = (Protocol) entity;
				if ((clazz.equals(ObservableFeature.class) && protocol.getFeatures().contains(characteristic) || (clazz
						.equals(Protocol.class) && protocol.getSubprotocols().contains(characteristic))))
				{
					entityCount++;
				}
			}
			if (entity.getClass().equals(DataSet.class))
			{
				DataSet dataSet = (DataSet) entity;
				if ((clazz.equals(Protocol.class) && dataSet.getProtocolUsed().getIdentifier()
						.equals(characteristic.getIdentifier())))
				{
					entityCount++;
				}
			}
			if (entity.getClass().equals(ObservedValue.class))
			{
				ObservedValue value = (ObservedValue) entity;
				if (clazz.equals(ObservableFeature.class) && value.getFeature().equals(characteristic))
				{
					entityCount++;
				}
			}
		}
		return entityCount;
	}
}
