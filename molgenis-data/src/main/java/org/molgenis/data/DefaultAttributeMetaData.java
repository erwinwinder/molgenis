package org.molgenis.data;

import org.molgenis.AttributeMetaData;
import org.molgenis.FieldTypes;
import org.molgenis.meta.types.DataType;

public class DefaultAttributeMetaData implements AttributeMetaData
{
	private final String name;
	private final String entityName;
	private String description;
	private DataType dataType = FieldTypes.getType("string");
	private boolean nillable = true;
	private boolean visible = true;
	private boolean unique = false;
	private boolean idAttribute = false;
	private boolean labelAttribute = false;
	private String xrefEntityName = null;
	private String mrefEntityName = null;
	private String mrefRemoteAttributeName = null;

	public DefaultAttributeMetaData(String name, String entityName)
	{
		if (name == null) throw new IllegalArgumentException("Name is null");
		if (entityName == null) throw new IllegalArgumentException("EntityName is null");
		this.name = name;
		this.entityName = entityName;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public DataType getDataType()
	{
		return dataType;
	}

	@Override
	public boolean isNillable()
	{
		return nillable;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public boolean isUnique()
	{
		return unique;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return idAttribute;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return labelAttribute;
	}

	@Override
	public String getXrefEntityName()
	{
		return xrefEntityName;
	}

	@Override
	public String getMrefEntityName()
	{
		return mrefEntityName;
	}

	@Override
	public String getMrefRemoteAttributeName()
	{
		return mrefRemoteAttributeName;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDataType(DataType dataType)
	{
		this.dataType = dataType;
	}

	public void setNillable(boolean nillable)
	{
		this.nillable = nillable;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	public void setIdAttribute(boolean idAttribute)
	{
		this.idAttribute = idAttribute;
		if (idAttribute)
		{
			setUnique(true);
			setNillable(false);
		}
	}

	public void setLabelAttribute(boolean labelAttribute)
	{
		this.labelAttribute = labelAttribute;
	}

	public void setXrefEntityName(String xrefEntityName)
	{
		this.xrefEntityName = xrefEntityName;
	}

	public void setMrefEntityName(String mrefEntityName)
	{
		this.mrefEntityName = mrefEntityName;
	}

	public void setMrefRemoteAttributeName(String mrefRemoteAttributeName)
	{
		this.mrefRemoteAttributeName = mrefRemoteAttributeName;
	}

}
