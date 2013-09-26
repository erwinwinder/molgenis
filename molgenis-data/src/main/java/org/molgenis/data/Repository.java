package org.molgenis.data;

import org.molgenis.Entity;
import org.molgenis.EntityMetaData;

public interface Repository<E extends Entity> extends Iterable<E>
{
	String getName();

	EntityMetaData getEntityMetaData();
}
