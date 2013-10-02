package org.molgenis.ui.data;

import static org.molgenis.ui.data.DataPluginController.URI;

import org.molgenis.Entity;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class DataPluginController extends MolgenisPluginController
{
	public static final String URI = "/plugin/data";
	private static final String VIEW_NAME = "view-data";
	private final DataService dataService;

	@Autowired
	public DataPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping
	public String init(Model model)
	{
		model.addAttribute("entityNames", Iterables.toArray(dataService.getEntityNames(), String.class));
		return VIEW_NAME;
	}

	@RequestMapping("/{entityName}")
	public String showData(Model model, @PathVariable("entityName")
	String entityName)
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);
		model.addAttribute("entityMetaData", repo.getEntityMetaData());
		model.addAttribute("entities", Iterables.toArray(repo, Entity.class));

		return init(model);
	}
}
