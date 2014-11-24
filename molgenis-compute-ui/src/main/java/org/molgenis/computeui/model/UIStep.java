package org.molgenis.computeui.model;

import java.util.List;
import java.util.UUID;

import org.molgenis.computeui.meta.StepMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class UIStep extends MapEntity
{
	private static final long serialVersionUID = 6310548646838674141L;

	public UIStep(String name, String protocol)
	{
		this();
		setName(name);
		setProtocol(protocol);
	}

	public UIStep()
	{
		super(StepMetaData.IDENTIFIER);
		set(StepMetaData.IDENTIFIER, UUID.randomUUID().toString().replaceAll("-", ""));
	}

	public String getIdentifier()
	{
		return getString(StepMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(StepMetaData.NAME);
	}

	public void setName(String name)
	{
		set(StepMetaData.NAME, name);
	}

	public String getProtocol()
	{
		return getString(StepMetaData.PROTOCOL);
	}

	public void setProtocol(String protocol)
	{
		set(StepMetaData.PROTOCOL, protocol);
	}

	public List<UIStep> getPreviousSteps()
	{
		Iterable<UIStep> steps = getEntities(StepMetaData.PREVIOUS_STEPS, UIStep.class);
		if (steps == null) return Lists.newArrayList();
		return Lists.newArrayList(steps);
	}

	public void addPreviousStep(UIStep step)
	{
		List<UIStep> previousSteps = getPreviousSteps();
		previousSteps.add(step);
		set(StepMetaData.PREVIOUS_STEPS, previousSteps);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((UIStep) obj).getIdentifier());
	}
}
