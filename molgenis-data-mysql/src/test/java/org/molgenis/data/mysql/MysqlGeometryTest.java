package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class MysqlGeometryTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("GeomTest").setLabel("Geom Test");
		emd.addAttribute("col0").setIdAttribute(true).setNillable(false);
		emd.addAttribute("col1").setDataType(MolgenisFieldTypes.GEOMETRY).setNillable(false);
		emd.addAttribute("col2").setDataType(MolgenisFieldTypes.GEOMETRY);
		emd.addAttribute("col3").setDataType(MolgenisFieldTypes.GEOMETRY).setDefaultValue("POINT(30 10)");
		return emd;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `GeomTest`(`col0` VARCHAR(255) NOT NULL, `col1` GEOMETRY NOT NULL, `col2` GEOMETRY, `col3` GEOMETRY, PRIMARY KEY (`col0`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col0", "1");
		e.set("col1", "POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7, 5 5))");
		e.set("col2", "LINESTRING(0 0,1 1,2 2)");
		return e;
	}

	@Override
	@Test
	public void test() throws Exception
	{
		super.test();
	}

}
