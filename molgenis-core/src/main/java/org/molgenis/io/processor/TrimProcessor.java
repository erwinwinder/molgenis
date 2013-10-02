package org.molgenis.io.processor;

public class TrimProcessor extends AbstractCellProcessor
{
	private static final long serialVersionUID = 1L;

	public TrimProcessor()
	{
		super();
	}

	public TrimProcessor(boolean processHeader, boolean processData)
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

		return ((String) value).trim();
	}
}
