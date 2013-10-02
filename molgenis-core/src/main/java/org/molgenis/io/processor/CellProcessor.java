package org.molgenis.io.processor;

import java.io.Serializable;

public interface CellProcessor extends Serializable
{
	public Object process(Object value);

	public boolean processHeader();

	public boolean processData();
}
