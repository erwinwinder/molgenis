package org.molgenis.opal.controller;

public class TableInfo
{
	private final String name;
	private final String entityType;

	public TableInfo(String name, String entityType)
	{
		this.name = name;
		this.entityType = entityType;
	}

	public String getName()
	{
		return name;
	}

	public String getEntityType()
	{
		return entityType;
	}

}
