package org.molgenis.data;

import org.molgenis.Entity;

public interface Queryable<E extends Entity>
{
	long count();

	long count(Query q);

	E findOne(Integer id);

	Iterable<E> findAll(Iterable<Integer> ids);

	Iterable<E> findAll(Query q);
}
