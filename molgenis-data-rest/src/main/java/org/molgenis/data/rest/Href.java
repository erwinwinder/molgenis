package org.molgenis.data.rest;

import java.io.UnsupportedEncodingException;

import org.molgenis.data.DataConverter;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.springframework.web.util.UriUtils;

public class Href
{
	private final String href;

	public Href(String href)
	{
		super();
		this.href = href;
	}

	public String getHref()
	{
		return href;
	}

	/**
	 * Create an encoded href for an attribute
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @param attributeName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatAttributeHref(String qualifiedEntityName, Object entityIdValue, String attributeName)
	{
		try
		{
			return String.format(RestController.BASE_URI + "/%s/%s/%s",
					UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"),
					UriUtils.encodePathSegment(DataConverter.toString(entityIdValue), "UTF-8"),
					UriUtils.encodePathSegment(attributeName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	/**
	 * Create an encoded href for an attribute meta
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @param attributeName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatMetaAttributeHref(String entityParentName, String attributeName)
	{
		try
		{
			return String.format(RestController.BASE_URI + "/%s/meta/%s",
					UriUtils.encodePathSegment(entityParentName, "UTF-8"),
					UriUtils.encodePathSegment(attributeName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	/**
	 * Create an encoded href for an entity
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatEntityHref(String qualifiedEntityName, Object entityIdValue)
	{
		if (null == qualifiedEntityName)
		{
			qualifiedEntityName = "";
		}

		try
		{
			return String.format(RestController.BASE_URI + "/%s/%s",
					UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"),
					UriUtils.encodePathSegment(DataConverter.toString(entityIdValue), "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownEntityException(qualifiedEntityName);
		}
	}

	/**
	 * Create an encoded href for an entity meta
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatMetaEntityHref(String qualifiedEntityName)
	{
		try
		{
			return String.format(RestController.BASE_URI + "/%s/meta",
					UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownEntityException(qualifiedEntityName);
		}
	}
}
