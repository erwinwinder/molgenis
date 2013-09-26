package org.molgenis;

import org.molgenis.meta.types.DataType;

public interface AttributeMetaData
{
	/**
	 * Unique per Entity
	 */
	String getName();

	String getDescription();

	DataType getDataType();

	boolean isNillable();

	boolean isVisible();

	boolean isUnique();

	boolean isIdAtrribute();

	boolean isLabelAttribute();

	String getXrefEntityName();

	String getMrefEntityName();

	String getMrefRemoteAttributeName();
}
