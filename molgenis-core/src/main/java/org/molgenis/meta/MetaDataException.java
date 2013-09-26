/**
 * File: invengine.db.ResultSet <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-03-21; 1.0.0; RA Scheltema; Creation.
 * <li>2006-04-15; 1.0.0; MA Swertz Created.
 * </ul>
 */

package org.molgenis.meta;

import org.apache.log4j.Logger;




public class MetaDataException extends Exception
{
	private static final long serialVersionUID = -4778664169051832691L;

	private static transient final Logger logger = Logger.getLogger("DSLParser");


	public MetaDataException(String error)
	{
		super(error);
		logger.error(error);
	}
}
