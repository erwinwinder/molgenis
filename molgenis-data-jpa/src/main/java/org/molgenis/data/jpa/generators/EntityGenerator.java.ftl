<#--
implementation notes:
* for nested collections we use List because that allows extra lazy load.
* we don't use CascadeType=persist|merge on associations because of performance and other problems
* 
-->
<#include "GeneratorHelper.ftl">
package ${package};

<#-- imports -->
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.molgenis.Entity;
<#if entity.abstract>
import java.io.Serializable;
<#else>
import java.util.List;

import org.molgenis.EntityMetaData;
import org.molgenis.meta.FieldMetaData;
import org.molgenis.AttributeMetaData;

//data
import org.molgenis.Entity;
import org.molgenis.meta.types.TypeUtils;

//constraints
import javax.validation.constraints.*;
</#if>

/**
 * ${entity.name}:
 * ${entity.description}
 */
<#-- INTERFACE OR CLASS?-->
<#if entity.abstract>
public interface ${JavaName(entity)}<ID extends Serializable> extends <#if entity.hasImplements()><#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)}<#if i_has_next>,</#if></#list><#else>Entity</#if>
<#else>
@javax.persistence.Entity
@javax.persistence.Inheritance(strategy=javax.persistence.InheritanceType.JOINED)
//@javax.persistence.DiscriminatorColumn(name="__Type", discriminatorType=javax.persistence.DiscriminatorType.STRING)
<#-- sql settings: table name, non-pkey constraints and indexes -->
<@compress single_line=true>
@javax.persistence.Table(name = "${SqlName(entity)}"
<#list entity.getUniqueKeysWithoutPk() as uniqueKeys >
	<#if uniqueKeys_index = 0 >, uniqueConstraints={@javax.persistence.UniqueConstraint( columnNames={
	<#else>), @javax.persistence.UniqueConstraint( columnNames={</#if>
    <#list key_fields(uniqueKeys) as uniqueFields >
		"${uniqueFields.name}"<#if uniqueFields_has_next>,</#if>
    </#list>
	}
    <#if !uniqueKeys_has_next>
    )
   }
   </#if>
</#list>
</@compress>)
<#-- indexes -->
<#if entity.indices?has_content ><@compress>
@org.hibernate.annotations.Table(appliesTo="${SqlName(entity)}", indexes={
<#foreach index in entity.indices>
    @org.hibernate.annotations.Index(name="${index.name}", columnNames={
			<#foreach field in index.fields>
	"${field}"<#if field_has_next>,</#if>
			</#foreach>
    })<#if index_has_next>,</#if>
</#foreach>
})
</@compress></#if>
public class ${JavaName(entity)} <#if entity.hasExtends()>extends ${entity.extends.namespace}.${JavaName(entity.extends)}</#if><#if entity.hasImplements()> implements<#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)},</#list>Entity<#else> implements Entity</#if>
</#if>
{
	// fieldname constants
    <#list entity.getImplementedFields() as field>
	public final static String ${field.name?upper_case} = "${field.name}";
	</#list>

<#-- PROPERTIES, GETTERS AND SETTERS -->
<#if entity.abstract>
	// getters & setters for interface<#list entity.fields as f>
	/** Get ${f.name}
	 *  ${f.description}
	 */
	public ${JavaType(f)} get${JavaName(f)}();
	
	/** Set ${f.name}
	 * ${f.description}
	 */
	public void set${JavaName(f)}(${JavaType(f)} ${name(f)});
	
</#list>
<#else>
	private static final long serialVersionUID = 1L;

	//properties for class<#list entity.implementedFields as f>
	//${f}
	<#if f.type == "autoid">
    @javax.persistence.Id 
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    <#elseif !f.nillable>
	@javax.validation.constraints.NotNull
	<#if f.type == "string">@Size(min=1,max=255,message="must consist of 1-255 characters")</#if>	
	</#if>
	<#if f.type == "xref">
	@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.EXTRA)
	@javax.persistence.JoinColumn(name = "${f.name}")
	<#elseif f.type == "mref">
	@javax.persistence.ManyToMany 
	@javax.persistence.JoinTable(name="${f.mrefName}")
	<#else>
	@javax.persistence.Column( name = "${f.name}")
		<#if f.type == "date" || f.type == "datetime">
	@javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
		</#if>
	</#if>
	private ${JavaType(f)} ${f.name} = ${default(f)};
	</#list>
	
	//constructor
	public ${JavaName(entity)}()
	{
		//set the type for a new instance
		//set__Type(this.getClass().getSimpleName());	
	}

<#if !entity.hasExtends()>
	public ${JavaType(entity.primaryKey)} getIdValue()
	{	
		return this.get${JavaName(entity.primaryKey)}();
	}
	
	public String getIdField()
	{	
		return "${entity.primaryKey.name}";
	}
	
	public String getLabelValue()
	{	
		return TypeUtils.toString(this.get${JavaName(entity.label)}());
	}
	
	public String getLabelField()
	{	
		return "${entity.label}";
	}
</#if>	
	
	//getters & setters for class<#list entity.implementedFields as f>
	/** Get ${f.name}
	 *  ${f.description}
	 */
	public ${JavaType(f)} get${JavaName(f)}()
	{
		return this.${f.name};
	}
	
	/** Set ${f.name}
	 * ${f.description}
	 */
	public void set${JavaName(f)}(${JavaType(f)} ${name(f)})
	{
		this.${f.name} = ${name(f)};
	}<#if f.type == "xref">
	/** Set ${f.name} using the @Id
	 * ${f.description}
	 */
	public void set${JavaName(f)}ById(Integer ${name(f)})
	{
		${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} e = new ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}();
		e.set${JavaName(f.xrefField)}(${name(f)});
		this.${f.name} = e;
	}
	
	/** Set ${f.name} using the xrefLabel
	 * ${f.description}
	 */
	public void set${JavaName(f)}ByLabel(String ${name(f)})
	{
		${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} e = new ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}();
		e.set${JavaName(f.xrefLabel)}(${name(f)});
		this.${f.name} = e;
	}<#elseif f.type == "mref">
	
	/** Set ${f.name} using the @Id
	 * ${f.description}
	 */
	public void set${JavaName(f)}ById(java.util.List<Integer> ${name(f)})
	{
		java.util.List<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}> list = new java.util.ArrayList<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}>();
		for(Integer id: ${name(f)})
		{
			${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} e = new ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}();
			e.set${JavaName(f.xrefField)}(id);
			list.add(e);
		}
		this.${f.name} = list;
	}
	
	/** Set ${f.name} using the xrefLabel
	 * ${f.description}
	 */
	public void set${JavaName(f)}ByLabel(java.util.List<String> ${name(f)})
	{
		//todo: use e.setXrefLabel();
		java.util.List<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}> list = new java.util.ArrayList<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}>();
		for(String label: ${name(f)})
		{
			${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} e = new ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}();
			e.set${JavaName(f.xrefLabel)}(label);
			list.add(e);
		}
		this.${f.name} = list;
	}
	</#if>
</#list>


	@Override
	public void set(Entity entity)
	{
		<#list entity.implementedFields as f>
		if(entity.get("${f.name}") != null)
		{
			this.set("${f.name}", entity.get("${f.name}"));
		}
		</#list>
	}
	
	@Override
	public void set(String field, Object value)
	{		
<#list entity.implementedFields as f>
		if("${f.name}".equalsIgnoreCase(field))
		{
<#if f.type == "xref">
			if(value instanceof ${JavaType(f)}) this.set${JavaName(f.name)}((${JavaType(f)})value);
			else if(value instanceof Integer) this.set${JavaName(f.name)}ById(TypeUtils.toInteger(value));
			else if(value instanceof String) this.set${JavaName(f.name)}ByLabel(TypeUtils.toString(value));
<#elseif f.type == "mref">
			try
			{
				//if list of string ... assume labels
				this.set${JavaName(f.name)}ByLabel(TypeUtils.toList(String.class, value));
			}
			catch(Exception e)
			{
				try
				{
					//if list of Integer ... assume ids
					this.set${JavaName(f.name)}ById(TypeUtils.toList(Integer.class, value));
				}
				catch(Exception e2)
				{
					//else assume they are full objects
					this.set${JavaName(f.name)}(TypeUtils.toList(${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class, value));
				}
			}
<#else>
			this.set${JavaName(f.name)}(TypeUtils.to${settertype(f)}(value));
</#if>
		}
</#list>
	}		

	@Override
	public Object get(String colName)
	{
		if(colName == null) return null;
<#list entity.implementedFields as f>
		if(colName.toLowerCase().equals("${f.name?lower_case}"))
			return this.get${JavaName(f.name)}();
</#list>	
		return null;
	}

	@Override @javax.persistence.Transient
	public EntityMetaData getMetaData()
	{
		return new ${JavaName(entity)}MetaData();
	}
	
  	@Override
    public int hashCode() {
    	int firstNumber = this.getClass().getName().hashCode();
    	int secondNumber = this.getClass().getSimpleName().hashCode();
    	if(firstNumber % 2 == 0) {
    	  firstNumber += 1;
    	}
    	if(secondNumber % 2 == 0) {
    		secondNumber += 1;
    	}
    
		return new HashCodeBuilder(firstNumber, secondNumber)
<#if entity.hasExtends()>
             	.appendSuper(super.hashCode())
</#if>
<#list entity.getUniqueKeysWithoutPk() as uniqueKeys >
	<#list key_fields(uniqueKeys) as uniqueFields >
		<#if uniqueFields.type != "mref" && uniqueFields.type != "xref">
			<#if name(uniqueFields) != 'type_' >
				.append(${uniqueFields.name})
			</#if>
		</#if>
	</#list>
</#list>    
   			.toHashCode();
    }  
    
    	@Override
	public boolean equals(Object obj) {
   		if (obj == null) { return false; }
   		if (obj == this) { return true; }
   		if (obj.getClass() != getClass()) {
     		return false;
   		}
		${JavaName(entity)} rhs = (${JavaName(entity)}) obj;
   		return new EqualsBuilder()
<#if entity.hasExtends()>
             	.appendSuper(super.equals(obj))
</#if>
<#list entity.getUniques() as uniqueKeys >
	<#list key_fields(uniqueKeys) as uniqueFields >
		//${name(uniqueFields)}
		<#if uniqueFields.type != "mref" && uniqueFields.type != "xref">
			<#if name(uniqueFields) != "type_" >
				.append(${uniqueFields.name}, rhs.get${JavaName(uniqueFields)}())
			</#if>
		</#if>
	</#list>
</#list>                 
                .isEquals();
  	}		
</#if>
<#if !entity.abstract>
	public int size()
	{
		return ${entity.fields?size};
	}

	public String toString()
	{
		return "${JavaName(entity)}(<#list entity.allFields as f>${f.name}="+get${JavaName(f)}()+"<#if f_index &gt; 0>," 
		+"</#if></#list>)";
	}
</#if>

	@Override
	public String getDisplayValue(String attributeName)
	{
		AttributeMetaData attribute = getMetaData().getAttribute(attributeName);
		if (attribute != null)
		{
			return attribute.getDataType().toString(get(attributeName));
		}

		return null;
	}
}