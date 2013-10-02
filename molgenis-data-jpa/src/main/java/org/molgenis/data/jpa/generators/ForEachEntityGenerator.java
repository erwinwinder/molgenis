package org.molgenis.data.jpa.generators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.meta.EntityMetaData;
import org.molgenis.meta.MolgenisMetaData;

import freemarker.template.Template;

/**
 * This generator applies the template to each entity. It uses defaults for template name, package name and classname:
 * <li>template name is this.getClass() + ".java.ftl" <li>package is {model.name}.{own package name}. For example,
 * org.molgenis.generate.foo.bar will be generated to {model.name}.foo.bar. <li>class name is own class name without
 * traling "Gen". For example: FooBarGen will generate {EntityName}FooBar.java files.
 * 
 */
public abstract class ForEachEntityGenerator extends MolgenisGenerator
{
	public final transient Logger logger = Logger.getLogger(this.getClass());

	private boolean includeAbstract = false;

	public ForEachEntityGenerator()
	{
		this(false);
	}

	public ForEachEntityGenerator(boolean includeAbstract)
	{
		this.includeAbstract = includeAbstract;
	}

	public abstract String getPackageName(EntityMetaData entity);

	@Override
	public void generate(MolgenisMetaData model, MolgenisOptions options) throws Exception
	{
		Template template = this.createTemplate(this.getClass().getSimpleName() + getExtension() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		// apply generator to each entity
		for (EntityMetaData entity : model.getEntities())
		{
			String packageName = getPackageName(entity);
			// calculate package from its own package
			File targetDir = new File(this.getSourcePath(options) + "/" + packageName.replace(".", "/"));

			try
			{
				if ((!entity.getAbstract() || this.includeAbstract) && (!this.skipSystem() || !entity.getSystem()))
				{
					File targetFile = new File(targetDir + "/" + GeneratorHelper.getJavaName(entity.getName())
							+ getType() + getExtension());
					if (!targetFile.exists())
					{
						boolean created = targetDir.mkdirs();
						if (!created && !targetDir.exists())
						{
							throw new IOException("could not create " + targetDir);
						}

						// logger.debug("trying to generated "+targetFile);
						templateArgs.put("entity", entity);
						templateArgs.put("model", model);
						templateArgs.put("db_driver", options.db_driver);
						templateArgs.put("template", template.getName());
						templateArgs.put("file", targetDir + "/" + GeneratorHelper.getJavaName(entity.getName())
								+ getType() + getExtension());
						templateArgs.put("package", packageName);

						OutputStream targetOut = new FileOutputStream(targetFile);

						template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
						targetOut.close();

						// logger.info("generated " +
						// targetFile.getAbsolutePath());
						logger.info("generated " + targetFile);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("problem generating for " + entity.getName());
				throw e;
			}
		}
	}

	/**
	 * Calculate class name from its own name.
	 * 
	 * 
	 * @return Name(this.getClass()) - "Gen"
	 */
	public abstract String getType();

	/**
	 * Skip system entities.
	 */
	public Boolean skipSystem()
	{
		return Boolean.FALSE;
	}
}
