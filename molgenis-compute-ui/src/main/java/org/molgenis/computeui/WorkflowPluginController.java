package org.molgenis.computeui;

import java.io.File;
import java.io.IOException;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.computeui.meta.WorkflowMetaData;
import org.molgenis.computeui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(WorkflowPluginController.URI)
public class WorkflowPluginController extends MolgenisPluginController
{
	public static final String ID = "workflow";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final DataService dataService;
	private final PipelineImporter pipelineImporter;

	@Autowired
	public WorkflowPluginController(DataService dataService, PipelineImporter pipelineImporter)
	{
		super(URI);
		this.dataService = dataService;
		this.pipelineImporter = pipelineImporter;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("workflows",
				Lists.newArrayList(dataService.findAll(WorkflowMetaData.INSTANCE.getName(), UIWorkflow.class)));

		return "view-workflows";
	}

	@RequestMapping(value = "{workflowName}", method = RequestMethod.GET)
	public String view(@PathVariable("workflowName") String workflowName, Model model)
	{
		UIWorkflow workflow = dataService.findOne(WorkflowMetaData.INSTANCE.getName(), workflowName, UIWorkflow.class);
		// TODO check not null
		model.addAttribute("workflow", workflow);

		return "view-workflow";
	}

	@RequestMapping(value = "add", method = RequestMethod.POST)
	public String addWorkflow(@RequestParam("workflowFolder") String workflowFolder) throws IOException
	{
		ComputeProperties props = new ComputeProperties(new String[]
		{});
		pipelineImporter.importPipeline(new File(workflowFolder), props);
		return "redirect:" + URI;
	}

	@RequestMapping(value = "{workflowName}/workflowsteps.js", method = RequestMethod.GET)
	public String viewStepsGraph(@PathVariable("workflowName") String workflowName, Model model)
	{
		UIWorkflow workflow = dataService.findOne(WorkflowMetaData.INSTANCE.getName(), workflowName, UIWorkflow.class);
		// TODO check not null
		model.addAttribute("workflow", workflow);

		return "view-steps";
	}
}
