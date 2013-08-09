package org.molgenis.opal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DataSetImportedEvent;
import org.molgenis.util.tuple.SingletonTuple;
import org.molgenis.util.tuple.Tuple;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValueTableImporter
{
	private final Database database;
	private final ValueConverter valueConverter;
	private final Logger logger = Logger.getLogger(ValueTableImporter.class);

	@Autowired
	public ValueTableImporter(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
		this.valueConverter = new ValueConverter(database);
	}

	public void importValueTables(Datasource datasource, List<String> tableNames) throws DatabaseException
	{
		database.beginTx();

		try
		{
			for (String tableName : tableNames)
			{
				ValueTable table = datasource.getValueTable(tableName);
				importValueTable(table);
			}

			database.commitTx();
		}
		catch (Exception e)
		{

			database.rollbackTx();
			throw new DatabaseException(e);
		}

		// publish dataset imported event(s)
		try
		{
			for (DataSet ds : database.find(DataSet.class))
				ApplicationContextProvider.getApplicationContext().publishEvent(
						new DataSetImportedEvent(this, ds.getId()));
		}
		catch (DatabaseException e)
		{
			logger.error("Error publishing " + DataSet.class.getSimpleName() + " imported event(s)");
		}
	}

	private void importValueTable(ValueTable table) throws DatabaseException, ValueConverterException
	{
		String protocolIdentifier = "protocol-" + table.getTableReference();

		if (Protocol.findByIdentifier(database, protocolIdentifier) != null)
		{
			// TODO delete
			throw new DatabaseException("Table [" + table.getTableReference() + "] is already imported");
		}

		// Create protocol
		Protocol protocol = new Protocol();
		protocol.setIdentifier(protocolIdentifier);
		protocol.setName(table.getName());// TODO or table.getEntityType() ?

		// Create the ObservableFeatures, opal calls them 'Variables'
		Map<Variable, ObservableFeature> featureMap = new HashMap<Variable, ObservableFeature>();

		// Add VariableEntity identifier feature
		ObservableFeature idFeature = new ObservableFeature();
		idFeature.setIdentifier("identifier-" + table.getTableReference());
		idFeature.setName(table.getEntityType());
		idFeature.setDataType("string");
		database.add(idFeature);
		protocol.getFeatures().add(idFeature);

		for (Variable variable : table.getVariables())
		{
			if (!variable.isRepeatable())// TODO
			{
				ObservableFeature feature = createFeature(variable, table);
				logger.info("Created feature [" + feature.getName() + "]");

				protocol.getFeatures().add(feature);
				featureMap.put(variable, feature);
			}
		}

		database.add(protocol);
		logger.info("Created Protocol [" + protocol.getName() + "]");

		// Create dataset
		String datasetIdentifier = "dataset-" + table.getTableReference();

		DataSet dataSet = new DataSet();
		dataSet.setIdentifier(datasetIdentifier);
		dataSet.setName(table.getName()); // TODO or table.getEntityType() ?
		dataSet.setProtocolUsed(protocol);
		database.add(dataSet);
		logger.info("Created DataSet [" + dataSet.getName() + "]");

		// Add ObservationSets, opal calls them ValueSets
		int rowNr = 0;
		for (ValueSet valueSet : table.getValueSets())
		{
			ObservationSet observationSet = new ObservationSet();
			observationSet.setPartOfDataSet(dataSet);
			database.add(observationSet);
			logger.debug("Created ObservationSet [" + observationSet.getId() + "]");

			List<ObservedValue> observedValues = new ArrayList<ObservedValue>();
			Map<Class<? extends org.molgenis.omx.observ.value.Value>, List<org.molgenis.omx.observ.value.Value>> valueMap = new HashMap<Class<? extends org.molgenis.omx.observ.value.Value>, List<org.molgenis.omx.observ.value.Value>>();

			// Add VariableEntity identifier ObservedValue
			StringValue idValue = new StringValue();
			idValue.setValue(valueSet.getVariableEntity().getIdentifier());
			database.add(idValue);

			ObservedValue idObservedValue = new ObservedValue();
			idObservedValue.setValue(idValue);
			idObservedValue.setFeature(idFeature);
			idObservedValue.setObservationSet(observationSet);
			database.add(idObservedValue);

			// TODO valueSet.getTimestamps()
			for (Variable variable : table.getVariables())
			{
				if (!variable.isRepeatable())// TODO
				{
					Value opalValue = table.getValue(variable, valueSet);

					ObservableFeature feature = featureMap.get(variable);
					Tuple tuple = new SingletonTuple<String>(feature.getIdentifier(), opalValue.toString());
					org.molgenis.omx.observ.value.Value molgenisValue;

					try
					{
						molgenisValue = valueConverter.fromTuple(tuple, feature.getIdentifier(), feature);
						logger.debug("Converted Opal Value [" + opalValue + "] to Molgenis Value [" + molgenisValue
								+ "]");
					}
					catch (ValueConverterException e)
					{
						logger.error(
								"Exception converting Value [" + opalValue + "] for Variable [" + variable.getName()
										+ "] of type [" + variable.getValueType().getName() + "]", e);
						throw e;
					}

					if (molgenisValue != null)
					{
						List<org.molgenis.omx.observ.value.Value> valueList = valueMap.get(molgenisValue.getClass());
						if (valueList == null)
						{
							valueList = new ArrayList<org.molgenis.omx.observ.value.Value>();
							valueMap.put(molgenisValue.getClass(), valueList);
						}
						valueList.add(molgenisValue);
						logger.debug("Added Value [" + molgenisValue + "]");

						ObservedValue observedValue = new ObservedValue();
						observedValue.setFeature(feature);
						observedValue.setObservationSet(observationSet);
						observedValue.setValue(molgenisValue);
						observedValues.add(observedValue);
						logger.debug("Added ObservedValue for feature [" + feature.getName() + "]");
					}
				}
			}

			database.add(observedValues);
			for (List<org.molgenis.omx.observ.value.Value> values : valueMap.values())
			{
				database.add(values);
			}

			database.getEntityManager().flush();
			database.getEntityManager().clear();
			logger.info("Inserted [" + ++rowNr + "] ObservatonSets");
		}
	}

	private ObservableFeature createFeature(Variable variable, ValueTable table) throws DatabaseException
	{
		ObservableFeature feature = new ObservableFeature();
		feature.setIdentifier(variable.getVariableReference(table));
		feature.setName(variable.getName());
		feature.setDataType(getMolgenisDataType(variable));

		if (StringUtils.isNotBlank(variable.getUnit()))
		{
			OntologyTerm unit = getUnitOntologyTerm(variable.getUnit());
			feature.setUnit(unit);
		}

		database.add(feature);

		if (variable.hasCategories())
		{
			for (org.obiba.magma.Category opalCategory : variable.getCategories())
			{
				createCategory(opalCategory, feature);
			}
		}

		return feature;
	}

	private void createCategory(org.obiba.magma.Category opalCategory, ObservableFeature feature)
			throws DatabaseException
	{
		Category molgenisCategory = new Category();
		molgenisCategory.setIdentifier(feature.getIdentifier() + "-" + opalCategory.getName());
		molgenisCategory.setName(opalCategory.getName());
		molgenisCategory.setIsMissing(opalCategory.isMissing());
		molgenisCategory.setObservableFeature(feature);

		// If the opal category has a code use it for the valueCode, if not use the name as valueCode
		String valueCode = opalCategory.getCode() != null ? opalCategory.getCode() : opalCategory.getName();
		molgenisCategory.setValueCode(valueCode);

		database.add(molgenisCategory);
	}

	// Units from opal are stored as OntologyTerms, with an identifier and name of the unit name
	private OntologyTerm getUnitOntologyTerm(String unit) throws DatabaseException
	{
		OntologyTerm ontologyTerm = OntologyTerm.findByIdentifier(database, unit);
		if (ontologyTerm == null)
		{
			ontologyTerm = new OntologyTerm();
			ontologyTerm.setIdentifier(unit);
			ontologyTerm.setName(unit);
			database.add(ontologyTerm);
		}

		return ontologyTerm;
	}

	// Molgenis datatypes:
	// bool,categorical,date,datetime,decimal,email,enum,file,html,hyperlink,image,int,long,mref,string,text,xref
	// Opal datatype: integer, decimal, text, binary, locale, boolean, datetime date (see
	// http://wiki.obiba.org/display/OPALDOC/Variables+and+Data)
	private String getMolgenisDataType(Variable variable)
	{
		ValueType valueType = variable.getValueType();

		String type = null;
		if (variable.hasCategories() && !variable.areAllCategoriesMissing())// If all categories missing, then it is
																			// used to define missing values
		{
			type = "categorical";
		}
		else if (valueType instanceof BooleanType)
		{
			type = "boolean";
		}
		else if (valueType instanceof DateTimeType)
		{
			type = "datetime";
		}
		else if (valueType instanceof DateType)
		{
			type = "date";
		}
		else if (valueType instanceof DecimalType)
		{
			type = "decimal";
		}
		else if (valueType instanceof IntegerType)
		{
			type = "long";
		}
		else if (valueType instanceof LocaleType)
		{
			type = "string";
		}
		else if (valueType instanceof TextType)
		{
			type = "text";
		}
		else if (valueType instanceof PointType)
		{
			type = "text";
		}
		else if (valueType instanceof LineStringType)
		{
			type = "text";
		}
		else if (valueType instanceof PolygonType)
		{
			type = "text";
		}

		if (type == null)
		{
			throw new RuntimeException("Unsupported opal datatype [" + valueType.getName() + "]");
		}

		return type;
	}

}
