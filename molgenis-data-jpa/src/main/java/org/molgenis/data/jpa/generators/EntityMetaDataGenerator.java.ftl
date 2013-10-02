<#include "GeneratorHelper.ftl">
package ${package};

import org.molgenis.meta.EntityMetaData;
import org.molgenis.meta.FieldMetaData;

public class ${JavaName(entity)}MetaData extends EntityMetaData
{
	public  ${JavaName(entity)}MetaData()
	{
		this.setName("${JavaName(entity)}");
		this.setXrefLabel("${entity.label}");
		
<#list entity.allFields as f>
		FieldMetaData ${f.name}_field = new FieldMetaData();
		${f.name}_field.setName("${JavaName(f)}");
		${f.name}_field.setType("${f.type}");
		${f.name}_field.setNillable(<#if f.nillable>true<#else>false</#if>);
		${f.name}_field.setAuto(<#if f.auto>true<#else>false</#if>);
		${f.name}_field.setDescription("${f.description}");
		this.addField(${f.name}_field);	
</#list>
	}
}