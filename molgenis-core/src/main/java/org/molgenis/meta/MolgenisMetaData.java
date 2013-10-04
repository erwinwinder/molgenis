package org.molgenis.meta;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.molgenis.meta.types.MrefField;
import org.molgenis.meta.types.XrefField;
import org.molgenis.meta.utils.MolgenisModelValidator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MolgenisMetaData
{
	private List<ModuleMetaData> modules = new ArrayList<ModuleMetaData>();
	private final String dataSourceName;

	public MolgenisMetaData(String dataSourceName)
	{
		this.dataSourceName = dataSourceName;
	}

	public String getDataSourceName()
	{
		return dataSourceName;
	}

	public List<EntityMetaData> getEntities()
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();
		for (ModuleMetaData m : this.getModules())
		{
			result.addAll(m.getEntities());
		}
		return result;
	}

	// added function addModule to add module to model
	public void addModule(ModuleMetaData e)
	{
		modules.add(e);
	}

	public EntityMetaData getEntity(String name)
	{
		for (ModuleMetaData module : modules)
		{
			EntityMetaData entity = module.getEntity(name);
			if (entity != null && entity.getName().equals(name)) return entity;
		}
		return null;
	}

	public String findModuleNameForEntity(String name)
	{
		for (ModuleMetaData module : modules)
		{
			for (EntityMetaData entity : module.getEntities())
			{
				if (entity.getName().equalsIgnoreCase(name)) return module.getName();
			}
		}
		return null;
	}

	/**
	 * find entity across local entities, and the ones contained in modules
	 * 
	 * @param name
	 * @return
	 */
	public EntityMetaData findEntity(String name)
	{
		for (ModuleMetaData module : modules)
		{
			for (EntityMetaData entity : module.getEntities())
			{
				if (entity.getName().toLowerCase().equals(name.toLowerCase())) return entity;
			}
		}
		return null;
	}

	public ModuleMetaData getModule(String name)
	{
		for (ModuleMetaData module : modules)
		{
			if (module.getName().toLowerCase().equals(name.toLowerCase())) return module;
		}

		return null;
	}

	/**
	 * Remove module, and return the index of the module that came before this one in the list (for GUI select
	 * purposes). If it is the last module, return null.
	 * 
	 * @param name
	 * @return
	 */
	public Integer removeModule(String name)
	{
		for (int i = 0; i < modules.size(); i++)
		{
			if (modules.get(i).getName().toLowerCase().equals(name.toLowerCase()))
			{
				modules.remove(i);
				if (modules.size() > 0)
				{
					return (i == 0 ? 0 : i - 1);
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}

	public synchronized List<ModuleMetaData> getModules()
	{
		return modules;
	}

	public synchronized void setModules(List<ModuleMetaData> modules)
	{
		this.modules = modules;
	}

	/**
	 * Find and remove an entity from either root or a module. If there are entities in the module or root left, jump to
	 * the previous one in the list. If there are no entities left in the root, return the name of the root. If there
	 * are no entities left in the module, return the name of the module.
	 * 
	 * @param string
	 * @return
	 */
	public String findRemoveEntity(String name)
	{
		for (ModuleMetaData module : modules)
		{
			for (int i = 0; i < module.getEntities().size(); i++)
			{
				if (module.getEntities().get(i).getName().toLowerCase().equals(name.toLowerCase()))
				{
					module.getEntities().remove(i);

					if (module.getEntities().size() > 0)
					{
						if (i == 0)
						{
							return module.getEntities().get(0).getName();
						}
						else
						{
							return module.getEntities().get(i - 1).getName();
						}
					}
					else
					{
						return null;
					}
				}
			}
		}
		return null;
	}

	public void parse(InputStream xml) throws MetaDataException
	{
		JAXBContext context;
		try
		{
			// parse xml
			context = JAXBContext.newInstance(ModuleMetaData.class);
			Unmarshaller um = context.createUnmarshaller();
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			URL url = MolgenisMetaData.class.getResource("molgenis.xsd");
			um.setSchema(sf.newSchema(url));
			ModuleMetaData module = (ModuleMetaData) um.unmarshal(xml);

			// link module to this model
			this.addModule(module);
			module.setModel(this);

			// create reverese links
			for (EntityMetaData entity : module.getEntities())
			{
				entity.setModule(module);

				for (FieldMetaData field : entity.getFields())
				{
					field.setEntity(entity);
				}

				for (UniqueMetaData unique : entity.getUniques())
				{
					unique.setEntity(entity);
				}
			}
		}
		catch (JAXBException e)
		{
			Throwable le = e.getLinkedException();

			if (le instanceof SAXParseException)
			{
				System.out.println("Error on line " + ((SAXParseException) le).getLineNumber() + "\n" + le.getMessage()
						+ "\n");
			}
			else e.printStackTrace();
			throw new MetaDataException("parsing failed");
		}
		catch (SAXException e)
		{
			e.printStackTrace();
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new MetaDataException("parsing failed");
		}
	}

	public static List<EntityMetaData> sortEntitiesByDependency(List<EntityMetaData> entityList, MolgenisMetaData model)
			throws MetaDataException
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();

		boolean found = true;
		List<EntityMetaData> toBeMoved = new ArrayList<EntityMetaData>();
		while (entityList.size() > 0 && found)
		{
			found = false;
			for (EntityMetaData entity : entityList)
			{
				List<String> deps = getDependencies(entity, model);

				// check if all deps are there
				boolean missing = false;
				for (String dep : deps)
				{
					if (indexOf(result, dep) < 0)
					{
						missing = true;
						break;
					}
				}

				if (!missing)
				{
					toBeMoved.add(entity);
					result.add(entity);
					found = true;
					break;
				}
			}

			for (EntityMetaData e : toBeMoved)
				entityList.remove(e);
			toBeMoved.clear();
		}

		// list not empty, cyclic?
		for (EntityMetaData e : entityList)
		{
			Logger.getLogger(MolgenisMetaData.class).error(
					"cyclic relations to '" + e.getName() + "' depends on " + getDependencies(e, model));
			result.add(e);
		}

		return result;
	}

	private static List<String> getDependencies(EntityMetaData currentEntity, MolgenisMetaData model)
			throws MetaDataException
	{
		Set<String> dependencies = new HashSet<String>();

		for (FieldMetaData field : currentEntity.getAllFields())
		{
			if (field.getType() instanceof XrefField)
			{
				dependencies.add(field.getXrefEntity().getName());

				EntityMetaData xrefEntity = field.getXrefEntity();

				// also all subclasses have this xref!!!!
				for (EntityMetaData e : xrefEntity.getAllDescendants())
				{
					if (!dependencies.contains(e.getName())) dependencies.add(e.getName());
				}
			}
			if (field.getType() instanceof MrefField)
			{
				dependencies.add(field.getXrefEntity().getName()); // mref
				// fields
				// including super classes and extends
				for (EntityMetaData entity : model.getEntity(field.getXrefEntity().getName()).getAllExtends())
				{
					dependencies.add(entity.getName());
				}
			}
		}

		dependencies.remove(currentEntity.getName());
		return new ArrayList<String>(dependencies);
	}

	private static int indexOf(List<EntityMetaData> entityList, String entityName)
	{
		for (int i = 0; i < entityList.size(); i++)
		{
			if (entityList.get(i).getName().equals(entityName)) return i;
		}
		return -1;
	}

	public int getNumberOfReferencesTo(EntityMetaData e) throws MetaDataException
	{
		int count = 0;

		for (EntityMetaData entity : this.getEntities())
		{
			for (FieldMetaData field : entity.getImplementedFields())
			{
				if (field.getType() instanceof XrefField || field.getType() instanceof MrefField)
				{
					String xrefEntity = field.getXrefEntity().getName();
					if (xrefEntity != null && xrefEntity.equals(e.getName())) count++;
				}
			}
		}
		return count;
	}

	public void validate() throws MetaDataException
	{
		MolgenisModelValidator.validate(this);
	}
}
