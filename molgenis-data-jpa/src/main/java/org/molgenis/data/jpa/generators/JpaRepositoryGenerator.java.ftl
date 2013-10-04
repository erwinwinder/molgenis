<#--
implementation notes:
* for nested collections we use List because that allows extra lazy load.
* we don't use CascadeType=persist|merge on associations because of performance and other problems
* 
-->
<#include "GeneratorHelper.ftl">
package ${package};

import org.molgenis.data.jpa.JpaRepository;

import org.springframework.stereotype.Component;

@Component
public class ${Name(entity)}Repository extends JpaRepository<${Name(entity)}>
{
	@Override
	public ${entity.namespace}.${Name(entity)}MetaData getEntityMetaData()
	{
		return new ${Name(entity)}MetaData();
	}

}