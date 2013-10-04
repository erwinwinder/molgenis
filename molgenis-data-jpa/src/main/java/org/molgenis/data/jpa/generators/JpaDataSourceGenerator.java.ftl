<#include "GeneratorHelper.ftl">
package org.molgenis.data.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Repository
public class ${model.dataSourceName} implements JpaDataSource
{
	private final Map<String, JpaRepository<? extends Entity>> repos = new LinkedHashMap<String, JpaRepository<? extends Entity>>();
	
	@Override
	public String getUrl()
	{
		return "jpa://${model.dataSourceName}";
	}

	<#list model.entities as entity>
	<#if !entity.abstract>
	@Autowired
	public void set${JavaName(entity)}Repository(${entity.namespace}.${JavaName(entity)}Repository ${entity.name}Repository)
	{	
		repos.put("${entity.name}", ${entity.name}Repository);
	}
	</#if>
	</#list>

	@Override
	public String getDescription()
	{
		return "Database entitities";
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return new ArrayList<String>(repos.keySet());
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		Repository<? extends Entity> repo = repos.get(entityName);
		if (repo == null)
		{
			throw new MolgenisDataException("Unknown jpa entity [" + entityName + "]");
		}
		
		return repo;
	}

}
