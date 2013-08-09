package org.molgenis.opal.controller;

import static org.molgenis.opal.controller.OpalIntegrationController.URI;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.opal.DataSetExporter;
import org.molgenis.opal.ValueTableImporter;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.rest.client.magma.RestDatasourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping(URI)
@SessionAttributes("datasource")
public class OpalIntegrationController
{
	public static final String URI = "/plugin/opal";
	private static final Logger logger = Logger.getLogger(OpalIntegrationController.class);

	private final ValueTableImporter valueTableImporter;
	private final DataSetExporter dataSetExporter;
	private final Database database;

	@Autowired
	public OpalIntegrationController(ValueTableImporter valueTableImporter, DataSetExporter dataSetExporter,
			Database database)
	{
		if (valueTableImporter == null) throw new IllegalArgumentException("ValueTableImporter is null");
		if (dataSetExporter == null) throw new IllegalArgumentException("DataSetExporter is null");
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.valueTableImporter = valueTableImporter;
		this.dataSetExporter = dataSetExporter;
		this.database = database;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		if (model.containsAttribute("datasource"))
		{
			return "redirect:" + URI + "/list-tables";
		}

		model.addAttribute(new OpenDatasourceForm());

		return "opal-open-datasource";
	}

	@RequestMapping(value = "/open-datasource", method = RequestMethod.POST)
	public String openDatasource(@ModelAttribute
	@Valid
	OpenDatasourceForm form, BindingResult bindingResult, Model model)
	{

		if (bindingResult.hasErrors())
		{
			return "opal-open-datasource";
		}

		try
		{
			RestDatasourceFactory restDatasourceFactory = new RestDatasourceFactory(form.getName(), form.getUrl(),
					form.getUsername(), form.getPassword(), form.getRemoteName());

			Datasource datasource = restDatasourceFactory.create();// TODO close
			datasource.initialise();
			model.addAttribute("datasource", datasource);
		}
		catch (MagmaRuntimeException e)
		{
			model.addAttribute("errorMessage", e.getMessage());
			return "opal-open-datasource";
		}

		return "redirect:" + URI + "/list-tables";
	}

	@RequestMapping(value = "/list-tables", method = RequestMethod.GET)
	public String listTables(@ModelAttribute("datasource")
	Datasource datasource, Model model) throws DatabaseException
	{
		List<TableInfo> tables = new ArrayList<TableInfo>();
		for (ValueTable table : datasource.getValueTables())
		{
			tables.add(new TableInfo(table.getName(), table.getEntityType()));
		}
		model.addAttribute("tables", tables);

		List<DataSet> dataSets = database.find(DataSet.class);
		model.addAttribute("dataSets", dataSets);

		return "opal-list-tables";
	}

	@RequestMapping(value = "/import-tables", method = RequestMethod.POST)
	public String importTables(@RequestParam("tables")
	List<String> tables, @ModelAttribute("datasource")
	Datasource datasource, Model model) throws DatabaseException
	{
		try
		{
			valueTableImporter.importValueTables(datasource, tables);
			model.addAttribute("successMessage", "Tables imported");
		}
		catch (Exception e)
		{
			logger.error("Exception importing tables [" + tables + "]", e);
			model.addAttribute("errorMessage", "Error importing tables: " + e.getMessage());
		}

		return listTables(datasource, model);
	}

	@RequestMapping(value = "/export-datasets", method = RequestMethod.POST)
	public String exportDataSets(@RequestParam("dataSets")
	List<Integer> dataSetIds, @ModelAttribute("datasource")
	Datasource datasource, Model model) throws DatabaseException
	{
		try
		{
			dataSetExporter.exportDataSet(datasource, dataSetIds);
			model.addAttribute("successMessage", "Datasets exported");
		}
		catch (Exception e)
		{
			logger.error("Exception exporting DataSets " + dataSetIds, e);
			model.addAttribute("errorMessage", "Error importing datasets: " + e.getMessage());
		}

		return listTables(datasource, model);
	}

	@RequestMapping("/logout")
	public String logout(SessionStatus status)
	{
		status.setComplete();
		return "redirect:" + URI;
	}
}
