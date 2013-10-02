package org.molgenis.io.processor;

public class LowerCaseProcessor extends AbstractCellProcessor
{
	private static final long serialVersionUID = 1L;

	public LowerCaseProcessor()
	{
		super();
	}

	public LowerCaseProcessor(boolean processHeader, boolean processData)
	{
		super(processHeader, processData);
	}

	@Override
	public Object process(Object value)
	{
		if ((value == null) || (!(value instanceof String)))
		{
			return value;
		}

		return ((String) value).toLowerCase();
	}
}
