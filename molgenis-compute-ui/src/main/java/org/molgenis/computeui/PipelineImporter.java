package org.molgenis.computeui;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.computeui.meta.StepMetaData;
import org.molgenis.computeui.meta.WorkflowMetaData;
import org.molgenis.computeui.model.UIStep;
import org.molgenis.computeui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.molgenis.util.DependencyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class PipelineImporter
{
	private static final Logger logger = Logger.getLogger(PipelineImporter.class);
	private final DataService dataService;

	@Autowired
	public PipelineImporter(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void importPipeline(File workflowFolder, ComputeProperties computeProperties) throws IOException
	{

		String workflowName = workflowFolder.getName();
		logger.info("Importing workflow '" + workflowName + "'");

		Workflow workflow = new WorkflowCsvParser().parse(new File(workflowFolder, "workflow.csv").getAbsolutePath(),
				computeProperties);

		Map<String, UIStep> stepByName = Maps.newLinkedHashMap();
		for (Step step : workflow.getSteps())
		{
			UIStep uiStep = new UIStep(step.getName(), step.getProtocol().getTemplate());
			stepByName.put(step.getName(), uiStep);
			dataService.add(StepMetaData.INSTANCE.getName(), uiStep);
		}

		for (Step step : workflow.getSteps())
		{
			if (!step.getPreviousSteps().isEmpty())
			{
				UIStep uiStep = stepByName.get(step.getName());
				for (String prevStepName : step.getPreviousSteps())
				{
					uiStep.addPreviousStep(stepByName.get(prevStepName));
				}
			}
		}

		Iterable<UIStep> uiSteps = DependencyResolver.resolveSelfReferences(stepByName.values(), StepMetaData.INSTANCE);
		dataService.update(StepMetaData.INSTANCE.getName(), uiSteps);
		dataService.add(WorkflowMetaData.INSTANCE.getName(), new UIWorkflow(workflowName, Lists.newArrayList(uiSteps)));
	}
}
