package org.molgenis.data.omx.generators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.molgenis.MolgenisOptions;
import org.molgenis.data.jpa.generators.MolgenisGenerator;
import org.molgenis.meta.MolgenisMetaData;

import freemarker.template.Template;

public class OmxDataSourceGenerator extends MolgenisGenerator
{

	@Override
	public void generate(MolgenisMetaData model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getSourcePath(options) + "/org/molgenis/data/omx/OmxDataSource.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("options", options);
		templateArgs.put("model", model);

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();
	}

	@Override
	public String getDescription()
	{
		return "Generates omx datasource";
	}

}
