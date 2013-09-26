package org.molgenis.meta;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "molgenis")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleMetaData
{
	@XmlAttribute
	private String name;
	
	@XmlElement
	private String description;
	
	@XmlTransient
	private MolgenisMetaData model;
	
	@XmlElement(name="entity")
	private List<XmlEntityMetaData> entities = new ArrayList<XmlEntityMetaData>();
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public synchronized List<XmlEntityMetaData> getEntities()
	{
		return entities;
	}

	public synchronized void setEntities(List<XmlEntityMetaData> entities)
	{
		this.entities = entities;
	}
	
	public XmlEntityMetaData getEntity(String name)
	{
		if(name != null) for (XmlEntityMetaData entity : entities)
		{
			if (entity.getName().toLowerCase().equals(name.toLowerCase()))
				return entity;
		}
		return null;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public MolgenisMetaData getModel()
	{
		return model;
	}

	public void setModel(MolgenisMetaData model)
	{
		this.model = model;
	}
}
