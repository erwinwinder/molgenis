package org.molgenis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.AttributeMetaData;
import org.molgenis.EntityMetaData;

public class DefaultEntityMetaData implements EntityMetaData
{
	private final String name;
	private final String role;
	private boolean visible = true;
	private final Map<String, AttributeMetaData> attributes = new LinkedHashMap<String, AttributeMetaData>();

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
		attributes.put(attributeMetaData.getName(), attributeMetaData);
	}

	@Override
	public List<AttributeMetaData> getAttributes()
	{
		return Collections.unmodifiableList(new ArrayList<AttributeMetaData>(attributes.values()));
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		for (AttributeMetaData attribute : attributes.values())
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
		for (AttributeMetaData attribute : attributes.values())
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

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		return attributes.get(attributeName);
	}
}
