package org.molgenis.data;

public class MolgenisDataException extends RuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisDataException()
	{
	}

	public MolgenisDataException(String message)
	{
		super(message);
	}

	public MolgenisDataException(Throwable t)
	{
		super(t);
	}

	public MolgenisDataException(String message, Throwable t)
	{
		super(message, t);
	}

}
