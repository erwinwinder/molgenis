package org.molgenis.opal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.util.tuple.Tuple;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataSetExporter
{
	private final Database database;

	@Autowired
	public DataSetExporter(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	public void exportDataSet(Datasource datasource, List<Integer> dataSetIds) throws DatabaseException,
			TableException, IOException
	{
		for (Integer dataSetId : dataSetIds)
		{
			exportDataSet(datasource, dataSetId);
		}
	}

	public void exportDataSet(Datasource datasource, Integer dataSetId) throws DatabaseException, TableException,
			IOException
	{
		DataSet dataSet = DataSet.findById(database, dataSetId);
		if (dataSet == null)
		{
			throw new DatabaseException("DataSet with id [" + dataSetId + "] does not exist");
		}

		DataSetTable dataSetTable = new DataSetTable(dataSet, database);

		String tableName = dataSet.getName();
		String entityType = dataSet.getProtocolUsed().getName();
		ValueTableWriter valueTableWriter = datasource.createWriter(tableName, entityType);

		// Create Variables
		VariableWriter variableWriter = valueTableWriter.writeVariables();

		Map<String, Variable> variableByName = new HashMap<String, Variable>();

		for (Field field : dataSetTable.getAllColumns())
		{
			ValueType valueType = getValueTypeForFieldType(field.getType());
			Variable.Builder variableBuilder = Variable.Builder.newVariable(field.getName(), valueType, entityType)
					.addAttribute("label", field.getLabel());

			String unit = getUnit(field);
			if (unit != null)
			{
				variableBuilder.unit(unit);
			}

			if (field.getType().getEnumType() == FieldTypeEnum.CATEGORICAL)
			{
				ObservableFeature feature = getObservableFeature(field);
				List<Category> categories = database.query(Category.class).equals(Category.OBSERVABLEFEATURE, feature)
						.find();
				for (Category category : categories)
				{
					variableBuilder.addCategory(category.getName(), category.getValueCode());
				}
			}

			Variable variable = variableBuilder.build();
			variableWriter.writeVariable(variable);
			variableByName.put(variable.getName(), variable);
		}

		variableWriter.close();

		// Create ValueSets

		// TODO test with big datasets, need to do in batches, or does Opal lib provide this functionality?
		Iterator<Tuple> it = dataSetTable.iterator();
		while (it.hasNext())
		{
			Tuple tuple = it.next();

			// TODO what do we use for the VariableEntity identifier, ObserVationSet id? But then we can not download
			// Opal
			// data, edit it and upload it again because the identifiers are changed then. Should we use an
			// ObervationTarget?
			// but how do we now wich feature holds ObservationTargets? and nothing prevents us for having multiple
			// ObservationTarget features in a Protocol.
			// For now use ObservationSet id (added ObservationSetId to DataSetTable value tuples, not to column
			// definitions)

			VariableEntity variableEntity = new VariableEntityBean(entityType, tuple.getString("ObservationSetId"));
			ValueSetWriter valueSetWriter = valueTableWriter.writeValueSet(variableEntity);

			for (Field field : dataSetTable.getAllColumns())
			{
				Variable variable = variableByName.get(field.getName());
				Value value = variable.getValueType().valueOf(tuple.get(field.getName()));
				valueSetWriter.writeValue(variable, value);
			}

			valueSetWriter.close();
		}

		valueTableWriter.close();
	}

	private ValueType getValueTypeForFieldType(FieldType fieldType)
	{
		String valueTypeName = null;

		switch (fieldType.getEnumType())
		{
			case INT:
			case LONG:
				valueTypeName = "integer";
				break;
			case BOOL:
				valueTypeName = "boolean";
				break;
			case CATEGORICAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case STRING:
			case TEXT:
				valueTypeName = "text";
				break;
			case DECIMAL:
				valueTypeName = "decimal";
				break;
			case DATE:
				valueTypeName = "date";
				break;
			case DATE_TIME:
				valueTypeName = "datetime";
				break;

			case FILE:
			case IMAGE:
			case XREF:
			case MREF:
				throw new RuntimeException("Unsupported fieldType [" + fieldType.getEnumType() + "] for export to Opal");

		}

		return ValueType.Factory.forName(valueTypeName);
	}

	private ObservableFeature getObservableFeature(Field field) throws DatabaseException
	{
		ObservableFeature feature = ObservableFeature.findByIdentifier(database, field.getName());
		if (feature == null)
		{
			throw new RuntimeException("Unknown ObservableFeature with identifier [" + field.getName() + "]");
		}

		return feature;
	}

	private String getUnit(Field field) throws DatabaseException
	{
		ObservableFeature feature = getObservableFeature(field);
		OntologyTerm unitOntologyTerm = feature.getUnit();
		if (unitOntologyTerm != null)
		{
			return unitOntologyTerm.getName();
		}

		return null;
	}

}
