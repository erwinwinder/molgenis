package org.molgenis;

public interface EntityMetaData
{
	public static final String ROLE_ENTITY = "entity";
	public static final String ROLE_PROTOCOL = "protocol";
	public static final String ROLE_DATASET = "dataset";

	/**
	 * Every Entity has a unique name
	 */
	String getName();

	/**
	 * The role this Entity plays, can be anything
	 */
	String getRole();

	boolean isVisible();

	Iterable<AttributeMetaData> getAttributes();

	AttributeMetaData getAttribute(String attributeName);

	AttributeMetaData getIdAttribute();

	AttributeMetaData getLabelAttribute();
}
