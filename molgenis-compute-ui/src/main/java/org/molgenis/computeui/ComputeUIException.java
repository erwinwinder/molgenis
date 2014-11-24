package org.molgenis.computeui;

public class ComputeUIException extends RuntimeException
{

	private static final long serialVersionUID = -3063171201965166270L;

	public ComputeUIException(String message)
	{
		super(message);
	}

	public ComputeUIException(Throwable cause)
	{
		super(cause);
	}

	public ComputeUIException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
