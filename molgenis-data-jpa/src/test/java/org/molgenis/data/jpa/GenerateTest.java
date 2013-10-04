package org.molgenis.data.jpa;

import org.molgenis.MolgenisOptions;
import org.molgenis.data.jpa.generators.EntityGenerator;
import org.molgenis.data.jpa.generators.EntityMetaDataGenerator;
import org.molgenis.data.jpa.generators.JpaDataSourceGenerator;
import org.molgenis.data.jpa.generators.JpaRepositoryGenerator;
import org.molgenis.meta.MolgenisMetaData;
import org.testng.annotations.Test;

public class GenerateTest
{
	@Test
	public void generate() throws Exception
	{
		MolgenisOptions options = new MolgenisOptions();

		MolgenisMetaData model = new MolgenisMetaData("TestDataSource");
		model.parse(this.getClass().getResourceAsStream("/org/molgenis/model/test.xml"));

		EntityMetaDataGenerator entityMetaDataGenerator = new EntityMetaDataGenerator();
		entityMetaDataGenerator.generate(model, options);

		EntityGenerator entityGenerator = new EntityGenerator();
		entityGenerator.generate(model, options);

		JpaRepositoryGenerator jpaRepositoryGenerator = new JpaRepositoryGenerator();
		jpaRepositoryGenerator.generate(model, options);

		JpaDataSourceGenerator jpaDataSourceGenerator = new JpaDataSourceGenerator();
		jpaDataSourceGenerator.generate(model, options);
	}
}
