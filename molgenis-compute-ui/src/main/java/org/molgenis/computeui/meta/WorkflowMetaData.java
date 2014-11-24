package org.molgenis.computeui.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class WorkflowMetaData extends DefaultEntityMetaData
{
	public static final WorkflowMetaData INSTANCE = new WorkflowMetaData();

	public static final String ENTITY_NAME = "Workflow";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String STEPS = "steps";

	private WorkflowMetaData()
	{
		super(ENTITY_NAME, new ComputeUIPackage());
		addAttribute(NAME).setIdAttribute(true).setNillable(false);
		addAttribute(DESCRIPTION);
		addAttribute(STEPS).setNillable(false).setDataType(MolgenisFieldTypes.MREF).setRefEntity(StepMetaData.INSTANCE);
	}
}
