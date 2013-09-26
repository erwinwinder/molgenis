package org.molgenis.data;

public interface AttributeQueryable
{
	long countAttributes(Query q);

	Iterable<String> findAllAttributes(Query q);
}
