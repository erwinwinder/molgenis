package org.molgenis;

import java.io.Serializable;

public interface Entity extends Serializable
{
	/**
	 * Get meta model
	 */
	EntityMetaData getMetaData();

	/**
	 * Retrieves the value of the designated attribute as Object.
	 * 
	 * @param attributeName
	 * @return
	 */
	Object get(String attributeName);

	/**
	 * Get a representation of the value as it will be displayed to the user. (Nicely formatted)
	 * 
	 * @return
	 */
	String getDisplayValue(String attributeName);

	/**
	 * Set the value of an attribute
	 * 
	 * @param attributeName
	 * @param value
	 */
	void set(String attributeName, Object value);

	/**
	 * Set the properties of this entity using the values from another Entity.
	 * 
	 * @param values
	 * @throws ParseException
	 */
	void set(Entity values);

	/**
	 * Get the id value
	 * 
	 * @return id value
	 */
	Integer getIdValue();

	/**
	 * Get the string label
	 * 
	 * @return
	 */
	String getLabelValue();
}
