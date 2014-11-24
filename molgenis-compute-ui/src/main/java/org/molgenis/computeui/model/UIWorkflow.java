package org.molgenis.computeui.model;

import java.util.List;

import org.molgenis.computeui.meta.StepMetaData;
import org.molgenis.computeui.meta.WorkflowMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.DependencyResolver;

import com.google.common.collect.Lists;

public class UIWorkflow extends MapEntity
{
	private static final long serialVersionUID = -3317438940388235718L;

	public UIWorkflow()
	{
	}

	public UIWorkflow(String name)
	{
		super(WorkflowMetaData.NAME);
		set(WorkflowMetaData.NAME, name);
	}

	public UIWorkflow(String name, List<UIStep> steps)
	{
		this(name);
		setSteps(steps);
	}

	public String getName()
	{
		return getString(WorkflowMetaData.NAME);
	}

	public List<UIStep> getSteps()
	{
		Iterable<UIStep> steps = getEntities(WorkflowMetaData.STEPS, UIStep.class);
		if (steps == null) return Lists.newArrayList();

		steps = DependencyResolver.resolveSelfReferences(steps, StepMetaData.INSTANCE);

		return Lists.newArrayList(steps);
	}

	public void setSteps(List<UIStep> steps)
	{
		set(WorkflowMetaData.STEPS, steps);
	}

	public void addStep(UIStep step)
	{
		List<UIStep> steps = getSteps();
		steps.add(step);
		setSteps(steps);
	}

	public String getDescription()
	{
		return getString(WorkflowMetaData.DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(WorkflowMetaData.DESCRIPTION, description);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getName().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getName().equals(((UIWorkflow) obj).getName());
	}
}
