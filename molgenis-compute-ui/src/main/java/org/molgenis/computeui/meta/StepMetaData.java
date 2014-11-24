package org.molgenis.computeui.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class StepMetaData extends DefaultEntityMetaData
{
	public static final StepMetaData INSTANCE = new StepMetaData();

	public static final String ENTITY_NAME = "Step";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String PROTOCOL = "protocol";
	public static final String PREVIOUS_STEPS = "previousSteps";

	private StepMetaData()
	{
		super(ENTITY_NAME, new ComputeUIPackage());
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(NAME).setNillable(false);
		addAttribute(PROTOCOL).setDataType(MolgenisFieldTypes.SCRIPT).setNillable(false);
		addAttribute(PREVIOUS_STEPS).setDataType(MolgenisFieldTypes.MREF).setRefEntity(this);
	}
}
