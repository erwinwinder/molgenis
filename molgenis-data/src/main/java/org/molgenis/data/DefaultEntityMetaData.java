package org.molgenis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.AttributeMetaData;
import org.molgenis.EntityMetaData;

public class DefaultEntityMetaData implements EntityMetaData
{
	private final String name;
	private final String role;
	private boolean visible = true;
	private final List<AttributeMetaData> attributes = new ArrayList<AttributeMetaData>();

	public DefaultEntityMetaData(String name, String role)
	{
		if (name == null) throw new IllegalArgumentException("Name is null");
		if (role == null) throw new IllegalArgumentException("Role is null");
		this.name = name;
		this.role = role;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void addAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		attributes.add(attributeMetaData);
	}

	@Override
	public List<AttributeMetaData> getAttributes()
	{
		return Collections.unmodifiableList(attributes);
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		for (AttributeMetaData attribute : attributes)
		{
			if (attribute.isIdAtrribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		for (AttributeMetaData attribute : attributes)
		{
			if (attribute.isLabelAttribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public String getRole()
	{
		return role;
	}

}
