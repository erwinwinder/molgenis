package org.molgenis.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.molgenis.AttributeMetaData;
import org.molgenis.meta.types.DataType;

import com.google.common.collect.Iterables;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity")
public class EntityMetaData implements org.molgenis.EntityMetaData
{
	@XmlTransient
	Logger logger = Logger.getLogger(EntityMetaData.class);

	@XmlTransient
	ModuleMetaData module;

	public static final int PRIMARY_KEY = 0;

	@XmlAttribute(required = true)
	private String name;

	@XmlAttribute
	private String label;

	@XmlElement
	private String description = "No description provided";

	@XmlElement(name = "field")
	private List<FieldMetaData> fields = new ArrayList<FieldMetaData>();

	@XmlElement(name = "unique")
	private List<UniqueMetaData> uniques = new ArrayList<UniqueMetaData>();

	@XmlElement(name = "index")
	private List<IndexMetaData> indices = new ArrayList<IndexMetaData>();

	@XmlAttribute(name = "extends")
	private String _extends = null;

	@XmlAttribute(name = "implements")
	private String _implements = null;

	@XmlAttribute(name = "decorator")
	private String _decorator = null;

	@XmlAttribute(name = "abstract")
	private Boolean _abstract = false;

	@XmlAttribute
	private String xref_label = null;

	@XmlAttribute
	private Boolean association = false;

	@XmlAttribute
	private Boolean system = false;

	public void setSystem(Boolean system)
	{
		this.system = system;
	}

	// CONSTRUCTORS
	public EntityMetaData()
	{

	}

	// HELPER METHODS
	public FieldMetaData getField(String name)
	{
		for (FieldMetaData f : fields)
		{
			if (f.getName().trim().equals(name.trim())) return f;
		}
		return null;
	}

	public void addField(FieldMetaData e)
	{
		e.setEntity(this);
		fields.add(e);
	}

	public void removeField(int index)
	{
		fields.remove(index);
	}

	public String getNamespace()
	{
		return getModule().getName();
	}

	public List<FieldMetaData> getAllFields()
	{
		Map<String, FieldMetaData> result = new LinkedHashMap<String, FieldMetaData>();

		if (this.getExtends() != null)
		{
			for (FieldMetaData f : this.getExtends().getAllFields())
			{
				result.put(f.getName(), f);
			}
		}

		for (EntityMetaData iface : this.getImplements())
		{
			for (FieldMetaData f : iface.getAllFields())
			{
				result.put(f.getName(), f);
			}
		}

		for (FieldMetaData f : this.getFields())
		{
			result.put(f.getName(), f);
		}
		return new ArrayList<FieldMetaData>(result.values());
	}

	public EntityMetaData getExtends()
	{
		return this.getModel().getEntity(this._extends);
	}

	public List<EntityMetaData> getImplements()
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();
		if (this._implements != null) for (String name : this._implements.split(","))
		{
			if (!"".equals(name.trim())) result.add(this.module.getModel().getEntity(name));
		}
		return result;

	}

	public List<FieldMetaData> getFieldsOf(DataType typeField)
	{
		List<FieldMetaData> result = new ArrayList<FieldMetaData>();
		for (FieldMetaData f : this.getFields())
		{
			if (f.getType().equals(typeField.getName())) result.add(f);
		}
		return result;
	}

	public FieldMetaData getAllField(String fieldName)
	{
		FieldMetaData f = getField(fieldName);
		if (f == null && this.hasExtends()) return this.getExtends().getAllField(fieldName);
		else return f;
	}

	public List<FieldMetaData> getImplementedFieldsOf(DataType fieldType)
	{
		List<FieldMetaData> result = new ArrayList<FieldMetaData>();
		for (EntityMetaData em : this.getAllExtends())
		{
			result.addAll(em.getFieldsOf(fieldType));
		}
		return result;
	}

	public FieldMetaData getPrimaryKey()
	{
		if (this.getAllUniques().size() > 0) return this.getAllUniques().get(0).getFields().get(0);
		return null;
	}

	public List<EntityMetaData> getAllImplements()
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();

		for (EntityMetaData impl : this.getImplements())
		{
			result.add(impl);
			result.addAll(impl.getAllImplements());
		}

		return result;
	}

	public List<EntityMetaData> getAllExtends()
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();

		if (this.getExtends() != null)
		{
			result.add(this.getExtends());
			result.addAll(this.getExtends().getAllExtends());
		}

		return result;
	}

	public void addField(int i, FieldMetaData f)
	{
		this.getFields().add(i, f);
	}

	public void removeField(FieldMetaData field)
	{
		this.fields.remove(field);
	}

	public List<UniqueMetaData> getAllUniques()
	{
		// uniques may be multiple so check uniqueness of fields
		Set<UniqueMetaData> result = new LinkedHashSet<UniqueMetaData>();

		for (EntityMetaData m : this.getAllExtends())
		{
			for (UniqueMetaData u : m.getAllUniques())
			{
				if (!result.contains(u)) result.add(u);
			}
		}

		for (EntityMetaData m : this.getAllImplements())
		{
			for (UniqueMetaData u : m.getAllUniques())
			{
				if (!result.contains(u)) result.add(u);
			}
		}

		for (UniqueMetaData u : this.getUniques())
		{
			if (!result.contains(u)) result.add(u);
		}

		return new ArrayList<UniqueMetaData>(result);
	}

	public void setXrefLabel(List<FieldMetaData> labels)
	{
		String label_string = "";
		for (FieldMetaData label : labels)
		{
			if (!label.equals(labels.get(0)))
			{
				label_string += ",";
			}
			label_string += label.getName();
		}
		this.setXrefLabel(label_string);
	}

	public List<EntityMetaData> getAllDescendants()
	{
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();

		for (EntityMetaData em : this.getModel().getEntities())
		{
			if (this.getName().equals(em.getExtends()))
			{
				result.add(em);
				result.addAll(em.getAllDescendants());
			}
		}

		return result;
	}

	public List<FieldMetaData> getKeyFields(int keyIndex)
	{
		if (this.getUniques().size() > keyIndex) return this.getUniques().get(keyIndex).getFields();
		return new ArrayList<FieldMetaData>();
	}

	// GETTERS and SETTERS
	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLabel()
	{
		if (label == null) return getName();
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public boolean getSystem()
	{
		return this.system;
	}

	public List<FieldMetaData> getFields()
	{
		return fields;
	}

	public void setFields(List<FieldMetaData> fields)
	{
		this.fields = fields;
	}

	public List<UniqueMetaData> getUniques()
	{
		List<UniqueMetaData> result = new ArrayList<UniqueMetaData>();

		if (hasImplements())
		{
			for (EntityMetaData e : getImplements())
			{
				// we need to rewrite the uniques to point to the right entity
				for (UniqueMetaData u : e.getUniques())
				{
					UniqueMetaData copy = new UniqueMetaData(u);
					u.setEntity(this);
					if (!result.contains(copy))
					{
						result.add(copy);
					}
				}
			}
		}

		for (UniqueMetaData u : this.uniques)
		{
			result.add(u);
		}
		return result;
	}

	public void setUniques(List<UniqueMetaData> uniques)
	{
		this.uniques = uniques;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Boolean getAbstract()
	{
		return _abstract;
	}

	public void setAbstract(Boolean _abstract)
	{
		this._abstract = _abstract;
	}

	public void setExtends(String _extends)
	{
		this._extends = _extends;
	}

	public String getDecorator()
	{
		return _decorator;
	}

	public void setDecorator(String _decorator)
	{
		this._decorator = _decorator;
	}

	public Boolean getAssociation()
	{
		return association;
	}

	public void setAssociation(Boolean association)
	{
		this.association = association;
	}

	public String getXrefLabel()
	{
		if (xref_label == null)
		{
			// return this.getPrimaryKey().getName();
		}
		return xref_label;
	}

	public void setXrefLabel(String xref_label)
	{
		this.xref_label = xref_label;
	}

	public List<FieldMetaData> getImplementedFields()
	{
		Map<String, FieldMetaData> result = new LinkedHashMap<String, FieldMetaData>();

		for (EntityMetaData i : this.getImplements())
		{
			for (FieldMetaData f : i.getAllFields())
			{
				if (!result.containsKey(f.getName())) result.put(f.getName(), f);
			}
		}

		for (FieldMetaData f : this.getFields())
		{
			if (!result.containsKey(f.getName())) result.put(f.getName(), f);
		}

		return new ArrayList<FieldMetaData>(result.values());
	}

	public void setImplements(String _implements)
	{
		this._implements = _implements;
	}

	public void validateExtendsAndImplements(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("validate 'extends' and 'implements' relationships...");

		List<EntityMetaData> ifaces = this.getAllImplements();
		for (EntityMetaData iface : ifaces)
		{
			if (!iface.getAbstract()) throw new MetaDataException(this.getName() + " cannot implement "
					+ iface.getName() + " because it is not abstract");

			// copy primary key and xref_label from interface to subclass,
			// a primary key can have only one field.
			// usually it is a auto_number int
			// composite keys are ignored
			try
			{
				FieldMetaData pkeyField = null;
				if (iface.getUniques().size() > 0 && iface.getUniques().get(0).getFields().size() == 1)
				{
					pkeyField = iface.getUniques().get(0).getFields().get(0);
					// if not already exists
					if (this.getField(pkeyField.getName()) == null)
					{
						FieldMetaData field = new FieldMetaData(pkeyField);
						field.setEntity(this);
						field.setAuto(pkeyField.getAuto());
						field.setNillable(pkeyField.getNillable());
						field.setReadonly(pkeyField.getReadonly());

						field.setSystem(true);
						field.setXrefEntity(iface.getName());
						field.setHidden(true);

						logger.debug("copy primary key " + field.getName() + " from interface " + iface.getName()
								+ " to " + this.getName());
						this.addField(field);
					}
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new MetaDataException(e.getMessage());
			}
		}

		EntityMetaData parent = this.getExtends();

		if (parent == null) throw new MetaDataException("superclass '" + this.getExtends() + "' for '" + this.getName()
				+ "' is missing");

		if (parent != null)
		{
			if (parent.getAbstract()) throw new MetaDataException(this.getName() + " cannot extend " + parent.getName()
					+ " because superclas " + parent.getName() + " is abstract (use implements)");
			if (this.getAbstract()) throw new MetaDataException(this.getName() + " cannot extend " + parent.getName()
					+ " because " + this.getName() + " itself is abstract");
		}
	}

	/**
	 * Copy fields to subclasses (redundantly) so this field can be part of an extra constraint. E.g. a superclass has
	 * non-unique field 'name'; in the subclass it is said to be unique and a copy is made to capture this constraint in
	 * the table for the subclass.
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public void copyFieldsToSubclassToEnforceConstraints(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("copy fields to subclass for constrain checking...");

		// copy keyfields to subclasses to ensure that keys can be
		// enforced (if the key includes superclass fields).
		if (this.getExtends() != null)
		{
			for (UniqueMetaData aKey : this.getUniques())
			{
				for (FieldMetaData f : aKey.getFields())
				{
					if (this.getField(f.getName()) == null)
					{
						// copy the field
						FieldMetaData copy = new FieldMetaData(f);
						copy.setEntity(this);
						copy.setAuto(f.getAuto());
						copy.setSystem(true);
						this.addField(copy);

						logger.warn(aKey.toString() + " cannot be enforced on " + this.getName() + ", copying "
								+ f.getEntity().getName() + "." + f.getName() + " to subclass as " + copy.getName());
					}
				}
			}
		}
	}

	public void addUnique(List<String> unique, Boolean isSubclass)
	{
		UniqueMetaData u = new UniqueMetaData();
		u.setSubclass(isSubclass);
		String unique_fields = "";
		for (String key : unique)
		{
			if (!key.equals(unique.get(0))) unique_fields += ",";
			unique_fields += key;
		}
		u.setFields(unique_fields);
		this.getUniques().add(u);
	}

	public void addUnique(String field, String description)
	{
		UniqueMetaData u = new UniqueMetaData();
		u.setDescription(description);
		u.setFields(field);
		getUniques().add(u);
	}

	public void addUnique(Vector<String> unique)
	{
		this.addUnique(unique, false);
	}

	public MolgenisMetaData getModel()
	{
		return this.getModule().getModel();
	}

	public boolean hasImplements()
	{
		return this.getImplements().size() > 0;
	}

	public Boolean hasExtends()
	{
		return this.getExtends() != null;
	}

	/**
	 * Returns the root of the entity hierarchy this entity belongs to.
	 * 
	 * @return Entity
	 */
	public EntityMetaData getRootAncestor()
	{
		if (this.getExtends() != null)
		{
			return this.getExtends().getRootAncestor();
		}
		else
		{
			return this;
		}
	}

	public ModuleMetaData getModule()
	{
		return module;
	}

	public void setModule(ModuleMetaData module)
	{
		this.module = module;
	}

	public boolean isRootAncestor()
	{
		return this.equals(this.getRootAncestor());
	}

	public void addUnique(int i, UniqueMetaData m)
	{
		this.uniques.add(i, m);
	}

	public List<FieldMetaData> getInheritedFields()
	{
		if (this.hasExtends()) return getExtends().getAllFields();
		return new ArrayList<FieldMetaData>();
	}

	public List<UniqueMetaData> getUniqueKeysWithoutPk() throws MetaDataException
	{
		List<UniqueMetaData> result = new ArrayList<UniqueMetaData>();

		if (hasImplements())
		{
			for (EntityMetaData e : getImplements())
			{
				// we need to rewrite the uniques to point to the right entity
				for (UniqueMetaData u : e.getUniques())
				{
					if (u.getFields().get(0).getAuto())
					{
						continue;
					}
					UniqueMetaData copy = new UniqueMetaData(u);
					u.setEntity(this);
					if (!result.contains(copy))
					{
						result.add(copy);
					}
				}
			}
		}

		for (UniqueMetaData u : this.getUniques())
		{
			if (u.getFields().get(0).getAuto())
			{
				continue;
			}
			result.add(u);
		}
		return result;
	}

	public boolean hasDescendants()
	{
		return getDescendants().size() > 0;
	}

	/**
	 * Get the subclasses of this entity.
	 */
	public List<EntityMetaData> getDescendants()
	{
		Vector<EntityMetaData> descendants = new Vector<EntityMetaData>();

		// get the model
		for (EntityMetaData e : getModel().getEntities())
		{
			if (e.hasExtends() && e.getExtends().getName().equals(getName()))
			{
				descendants.add(e);
			}
		}

		return descendants;
	}

	public List<IndexMetaData> getIndices()
	{
		return indices;
	}

	public void setIndices(List<IndexMetaData> indices)
	{
		this.indices = indices;
	}

	public Iterable<String> getFieldNames()
	{
		List<String> result = new ArrayList<String>();
		for (FieldMetaData f : this.getFields())
		{
			result.add(f.getName());
		}
		return result;
	}

	@Override
	public String toString()
	{
		return String.format("entity(name=%s)", this.getName());
	}

	@Override
	public String getRole()
	{
		return org.molgenis.EntityMetaData.ROLE_ENTITY;
	}

	@Override
	public boolean isVisible()
	{
		return !_abstract && !system;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return Arrays.asList(Iterables.toArray(fields, AttributeMetaData.class));
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		return getField(attributeName);
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		return getPrimaryKey();
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return getAttribute(label);
	}
}
