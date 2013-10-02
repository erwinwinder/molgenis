package org.molgenis;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.molgenis.meta.FieldMetaData;
import org.molgenis.meta.MetaDataException;
import org.molgenis.meta.types.AutoidField;
import org.molgenis.meta.types.BoolField;
import org.molgenis.meta.types.DataType;
import org.molgenis.meta.types.DateField;
import org.molgenis.meta.types.DatetimeField;
import org.molgenis.meta.types.DecimalField;
import org.molgenis.meta.types.EmailField;
import org.molgenis.meta.types.EnumField;
import org.molgenis.meta.types.FreemarkerField;
import org.molgenis.meta.types.IntField;
import org.molgenis.meta.types.LongField;
import org.molgenis.meta.types.MrefField;
import org.molgenis.meta.types.RichtextField;
import org.molgenis.meta.types.StringField;
import org.molgenis.meta.types.TextField;
import org.molgenis.meta.types.XrefField;

/**
 * Singleton class that holds all known field types in MOLGENIS. For each FieldType it can be defined how to behave in
 * jdbc, jave <br>
 * 
 * @see DataType interface
 */
public class FieldTypes
{
	private static Map<String, DataType> types = new TreeMap<String, DataType>();
	private static Logger logger = Logger.getLogger(FieldTypes.class);
	private static boolean init = false;

	/** Initialize default field types */
	private static void init()
	{
		if (!init)
		{
			addType(new AutoidField());
			addType(new StringField());
			addType(new BoolField());
			addType(new IntField());
			addType(new DecimalField());
			addType(new DateField());
			addType(new DatetimeField());
			addType(new EnumField());
			addType(new XrefField());
			addType(new MrefField());
			addType(new TextField());
			addType(new EmailField());
			addType(new RichtextField());
			addType(new FreemarkerField());

			init = true;
		}

	}

	public static void addType(DataType ft)
	{
		types.put(ft.getClass().getSimpleName().toLowerCase(), ft);
	}

	public static DataType getType(String name)
	{
		init();
		try
		{
			return types.get(name + "field").getClass().newInstance();
		}
		catch (final Exception e)
		{
			logger.warn("couldn't get type for name '" + name + "'");
			return new StringField();
		}
	}

	public static DataType get(FieldMetaData f) throws MetaDataException
	{
		init();
		try
		{
			final DataType ft = f.getType().getClass().newInstance();
			ft.setField(f);
			return ft;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw new MetaDataException(e.getMessage());
		}
	}

	public static DataType getTypeBySqlTypesCode(int sqlCode)
	{
		switch (sqlCode)
		{
			case java.sql.Types.BIGINT:
				return new LongField();

			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.TINYINT:
				return new IntField();

			case java.sql.Types.BOOLEAN:
			case java.sql.Types.BIT:
				return new BoolField();

			case java.sql.Types.DATE:
				return new DateField();

			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.FLOAT:
			case java.sql.Types.REAL:
				return new DecimalField();

			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.NVARCHAR:
			case java.sql.Types.BLOB:
			case java.sql.Types.CLOB:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGNVARCHAR:
				return new StringField();

			case java.sql.Types.TIME:
			case java.sql.Types.TIMESTAMP:
				return new DatetimeField();

			default:
				logger.error("UNKNOWN sql code: " + sqlCode);
				return new StringField();
		}
	}

	public static Map<String, DataType> getTypes()
	{
		return types;
	}
}
