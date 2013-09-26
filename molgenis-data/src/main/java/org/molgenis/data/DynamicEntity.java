package org.molgenis.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.EntityMetaData;

public class DynamicEntity extends AbstractEntity
{
	private static final long serialVersionUID = 3903867703074103300L;
	private final Map<String, Object> values = new LinkedHashMap<String, Object>();

	public DynamicEntity(EntityMetaData entityMetaData)
	{
		super(entityMetaData);
	}

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DynamicEntity other = (DynamicEntity) obj;
		if (values == null)
		{
			if (other.values != null) return false;
		}
		else if (!values.equals(other.values)) return false;
		return true;
	}

}
