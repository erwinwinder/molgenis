package org.molgenis.data;

import org.molgenis.Entity;

/**
 * Class to deal with data backends.
 */
public interface CrudRepository<E extends Entity> extends WritableRepository<E>
{
	void update(E record);

	void update(Iterable<E> records);

	void delete(Integer id);

	void delete(E record);

	void delete(Iterable<E> entities);

	void deleteAll();
}