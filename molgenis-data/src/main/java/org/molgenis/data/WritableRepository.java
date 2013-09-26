package org.molgenis.data;

import org.molgenis.Entity;

public interface WritableRepository<E extends Entity> extends Repository<E>
{
	void create(E entity);

	void create(Iterable<E> entities);
}
