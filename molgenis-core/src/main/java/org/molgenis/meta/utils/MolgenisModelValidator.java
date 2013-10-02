package org.molgenis.meta.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.molgenis.meta.EntityMetaData;
import org.molgenis.meta.FieldMetaData;
import org.molgenis.meta.ModuleMetaData;
import org.molgenis.meta.MolgenisMetaData;
import org.molgenis.meta.MetaDataException;
import org.molgenis.meta.UniqueMetaData;
import org.molgenis.meta.types.EnumField;
import org.molgenis.meta.types.IntField;
import org.molgenis.meta.types.MrefField;
import org.molgenis.meta.types.StringField;
import org.molgenis.meta.types.XrefField;

public class MolgenisModelValidator
{
	private static final Logger logger = Logger.getLogger(MolgenisModelValidator.class.getSimpleName());

	public static void validate(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("validating model and adding defaults:");

		// validate the model
		convertTypeIsAutoidToIntField(model);

		validateNamesAndReservedWords(model);
		validateExtendsAndImplements(model);

		//addTypeFieldInSubclasses(model);

		validateKeys(model);
		addXrefLabelsToEntities(model);
		validatePrimaryKeys(model);
		validateForeignKeys(model);
		validateOveride(model);

		// enhance the model
		correctXrefCaseSensitivity(model);
		// if(!options.mapper_implementation.equals(MolgenisOptions.MapperImplementation.JPA))
		// {
		moveMrefsFromInterfaceAndCopyToSubclass(model);
		createLinkTablesForMrefs(model);
		// }
		copyDefaultXrefLabels(model);
		copyDecoratorsToSubclass(model);

		copyFieldsToSubclassToEnforceConstraints(model);

		validateNameSize(model);

	}

	public static void convertTypeIsAutoidToIntField(MolgenisMetaData model)
	{
		for (EntityMetaData e : model.getEntities())
		{
			for (FieldMetaData f : e.getFields())
			{
				if ("autoid".equals(f.getType()))
				{
					f.setType("int");
					f.setAuto(true);

					UniqueMetaData u = new UniqueMetaData();
					u.setFields(f.getName());
					u.setEntity(e);

					e.getUniques().add(0, u);
				}
				else if (f.getUnique())
				{
					UniqueMetaData u = new UniqueMetaData();
					u.setFields(f.getName());
					u.setEntity(e);

					e.getUniques().add(u);
				}

			}
		}

	}

	/**
	 * Copy fields to subclasses (redundantly) so this field can be part of an
	 * extra constraint. E.g. a superclass has non-unique field 'name'; in the
	 * subclass it is said to be unique and a copy is made to capture this
	 * constraint in the table for the subclass.
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	static public void copyFieldsToSubclassToEnforceConstraints(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("copy fields to subclass for constrain checking...");
		for (EntityMetaData e : model.getEntities())
		{
			// copy keyfields to subclasses to ensure that keys can be
			// enforced (if the key includes superclass fields).
			if (e.getExtends() != null)
			{
				for (UniqueMetaData aKey : e.getUniques())
				{
					for (FieldMetaData f : aKey.getFields())
					{
						if (e.getField(f.getName()) == null)
						{
							// copy the field
							FieldMetaData copy = new FieldMetaData(f);
							copy.setEntity(e);
							copy.setAuto(f.getAuto());
							copy.setSystem(true);
							e.addField(copy);

							logger.warn(aKey.toString() + " cannot be enforced on " + e.getName() + ", copying "
									+ f.getEntity().getName() + "." + f.getName() + " to subclass as " + copy.getName());
						}
					}
				}

			}
		}
	}

	/**
	 * As mrefs are a linking table between to other tables, interfaces cannot
	 * be part of mrefs (as they don't have a linking table). To solve this
	 * issue, mrefs will be removed from interface class and copied to subclass.
	 * 
	 * @throws MetaDataException
	 */
	public static void moveMrefsFromInterfaceAndCopyToSubclass(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("copy fields to subclass for constrain checking...");

		// copy mrefs from interfaces to implementing entities
		// also rename the target from interface to entity
		for (EntityMetaData entity : model.getEntities())
		{
			for (EntityMetaData iface : entity.getImplements())
			{
				for (FieldMetaData mref : iface.getFieldsOf(new MrefField()))
				{
					FieldMetaData f = new FieldMetaData(mref);
					f.setEntity(entity);

					String mrefName = entity.getName() + "_" + f.getName();
					if (mrefName.length() > 30)
					{
						mrefName = mrefName.substring(0, 25) + Integer.toString(mrefName.hashCode()).substring(0, 5);
					}
					f.setMrefName(mrefName);
					entity.addField(0, f);
				}
			}
		}

		// remove interfaces from entities
		for (EntityMetaData entity : model.getEntities())
		{
			if (entity.getAbstract()) for (FieldMetaData mref : entity.getFieldsOf(new MrefField()))
			{
				entity.removeField(mref);
			}
		}
	}

	/**
	 * Subclasses can override fields of superclasses. This should only be used
	 * with caution! Only good motivation is to limit xref type.
	 */
	public static void validateOveride(MolgenisMetaData model)
	{
		// TODO

	}

	public static void validateNameSize(MolgenisMetaData model) throws MetaDataException
	{
		for (EntityMetaData e : model.getEntities())
		{
			// maximum num of chars in oracle table name of column is 30
			if (e.getName().length() > 30)
			{
				throw new MetaDataException(String.format("table name %s is longer than %d", e.getName(), 30));
			}
			for (FieldMetaData f : e.getFields())
			{
				if (f.getName().length() > 30)
				{
					throw new MetaDataException(String.format("field name %s is longer than %d", f.getName(), 30));
				}
			}
		}
	}

	public static void addXrefLabelsToEntities(MolgenisMetaData model) throws MetaDataException
	{
		for (EntityMetaData e : model.getEntities())
		{
			if (e.getXrefLabel() == null)
			{
				// still empty then construct from secondary key
				List<FieldMetaData> result = new ArrayList<FieldMetaData>();
				if (e.getAllUniques().size() > 1)
				{
					for (FieldMetaData f : e.getAllUniques().get(1).getFields())
						result.add(f);
					e.setXrefLabel(result);
				}

				// otherwise use primary key
				else if (e.getAllUniques().size() > 0)
				{
					for (FieldMetaData f : e.getAllUniques().get(0).getFields())
						result.add(f);
					e.setXrefLabel(result);
				}

				logger.debug("added default xref_label=" + e.getXrefLabel() + " to entity=" + e.getName());

			}
		}

	}

	public static void validatePrimaryKeys(MolgenisMetaData model) throws MetaDataException
	{
		for (EntityMetaData e : model.getEntities())
			if (!e.getAbstract())
			{
				if (e.getAllUniques().size() == 0) throw new MetaDataException("entity '" + e.getName()
						+ " doesn't have a primary key defined ");
			}
	}

	/**
	 * Default xref labels can come from: - the xref_entity (or one of its
	 * superclasses)
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public static void copyDefaultXrefLabels(MolgenisMetaData model) throws MetaDataException
	{
		for (EntityMetaData e : model.getEntities())
		{
			for (FieldMetaData f : e.getFields())
			{
				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					if (f.getXrefLabel() != null && !f.getXrefLabel().equals(f.getXrefFieldName()))
					{
						EntityMetaData xref_entity = f.getXrefEntity();
						if (xref_entity.getXrefLabel() != null)
						{
							logger.debug("copying xref_label " + xref_entity.getXrefLabel() + " from "
									+ f.getXrefEntity() + " to field " + f.getEntity().getName() + "." + f.getName());
							f.setXrefLabel(xref_entity.getXrefLabel());
						}
					}
				}
			}
		}

	}

	/**
	 * In each entity of an entity subclass hierarchy a 'type' field is added to
	 * enable filtering. This method adds this type as 'enum' field such that
	 * all subclasses are an enum option.
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public static void addTypeFieldInSubclasses(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("add a 'type' field in subclasses to enable instanceof at database level...");
		for (EntityMetaData e : model.getEntities())
		{
			if (e.getExtends() == null)
			{
				List<EntityMetaData> subclasses = e.getAllDescendants();
				List<String> enumOptions = new ArrayList<String>();
				enumOptions.add(firstToUpper(e.getName()));
				for (EntityMetaData subclass : subclasses)
				{
					enumOptions.add(firstToUpper(subclass.getName()));
				}
				if (e.getField(FieldMetaData.TYPE_FIELD) == null)
				{
					FieldMetaData type_field = new FieldMetaData();
					type_field.setEntity(e);
					type_field.setType(new EnumField());
					type_field.setName(FieldMetaData.TYPE_FIELD);
					type_field.setHidden(true);
					type_field.setSystem(true);

					type_field.setDescription("Subtypes have to be set to allow searching");
					type_field.setSystem(true);
					type_field.setHidden(true);
					e.addField(0, type_field);
				}
				e.getField(FieldMetaData.TYPE_FIELD).setEnumOptions(enumOptions);
			}
			else
			{
				e.removeField(e.getField(FieldMetaData.TYPE_FIELD));
			}
		}

	}

	/**
	 * Add link tables for many to many relationships
	 * <ul>
	 * <li>A link table entity will have the name of [from_entity]_[to_entity]
	 * <li>A link table has two xrefs to the from/to entity respectively
	 * <li>The column names are those of the respective fields
	 * <li>In case of a self reference, the second column name is '_self'
	 * </ul>
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public static void createLinkTablesForMrefs(MolgenisMetaData model) throws MetaDataException
	{
		// renamed mrefs
		// Map<String, String> renamedMrefs = new LinkedHashMap<String,
		// String>();

		logger.debug("add linktable entities for mrefs...");
		// find the multi-ref fields
		for (EntityMetaData xref_entity_from : model.getEntities())
		{

			// iterate through all fields including those inherited from
			// interfaces
			for (FieldMetaData xref_field_from : xref_entity_from.getImplementedFieldsOf(new MrefField()))
			{
				try
				{
					// retrieve the references to the entity+field
					EntityMetaData xref_entity_to = xref_field_from.getXrefEntity();
					FieldMetaData xref_field_to = xref_field_from.getXrefField();

					// TODO: check whether this link is already present

					// create the new entity for the link, if explicitly named
					String mref_name = xref_field_from.getMrefName(); // explicit

					// if mref_name longer than 30 throw error
					if (mref_name.length() > 30)
					{
						throw new MetaDataException("mref_name cannot be longer then 30 characters, found: "
								+ mref_name);
					}

					// check if the mref already exists
					EntityMetaData mrefEntityModel = null;
					try
					{
						mrefEntityModel = model.getEntity(mref_name);
					}
					catch (Exception e)
					{
					}

					// if mref entity doesn't exist: create
					if (mrefEntityModel == null)
					{
						mrefEntityModel = new EntityMetaData();
						mrefEntityModel.setName(mref_name);
						mrefEntityModel.setLabel(mref_name);
						mrefEntityModel.setModule(xref_entity_from.getModule());
						//mrefEntityModel.setNamespace(xref_entity_from.getNamespace());
						mrefEntityModel.setAssociation(true);
						mrefEntityModel.setDescription("Link table for many-to-many relationship '"
								+ xref_entity_from.getName() + "." + xref_field_from.getName() + "'.");
						mrefEntityModel.setSystem(true);

						// create id field to ensure ordering
						FieldMetaData idField = new FieldMetaData();
						idField.setEntity(mrefEntityModel);
						idField.setType(new IntField());
						idField.setName("autoid");
						idField.setAuto(true);
						idField.setHidden(true);
						idField.setDescription("automatic id field to ensure ordering of mrefs");
						mrefEntityModel.addField(idField);
						mrefEntityModel.addUnique(idField.getName(), "unique auto key to ensure ordering of mrefs");

						// create the fields for the linktable
						FieldMetaData field;
						Vector<String> unique = new Vector<String>();

						field = new FieldMetaData();
						field.setEntity(mrefEntityModel);
						field.setType(new XrefField());
						field.setName(xref_field_from.getMrefRemoteid());

						field.setXrefEntity(xref_entity_to.getName());
						field.setXrefLabel(xref_field_from.getXrefLabel().getName());
						if (xref_field_from.getXrefCascade()) field.setXrefCascade(true);
						mrefEntityModel.addField(field);

						unique.add(field.getName());

						// add all the key-fields of xref_entity_from
						for (@SuppressWarnings("unused") FieldMetaData key : xref_entity_from.getKeyFields(EntityMetaData.PRIMARY_KEY))
						{
							field = new FieldMetaData();
							field.setEntity(mrefEntityModel);
							field.setType(new XrefField());
							field.setName(xref_field_from.getMrefLocalid());

							// null xreflabel
							field.setXrefEntity(xref_entity_from.getName());

							mrefEntityModel.addField(field);
							unique.add(field.getName());
						}

						// create the unique combination
						mrefEntityModel.addUnique(unique);

					}
					// if mrefEntityModel does not exist, check xref_labels
					else
					{
						// field is xref_field, does it have label(s)?
						FieldMetaData xrefField = mrefEntityModel.getAllField(xref_field_to.getName());

						// verify xref_label
						if (xrefField != null)
						{
							// logger.debug("adding xref_label "+xref_field_to.getXrefLabelNames()+"'back' for "+xrefField.getName());
							xrefField.setXrefLabel(xref_field_from.getXrefLabel());

						}
					}

					// set the linktable reference in the xref-field
					xref_field_from.setMrefName(mrefEntityModel.getName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

	}

	/**
	 * Validate foreign key relationships: <li>
	 * <ul>
	 * Do the xref_field and xref_label refer to fields actually exist
	 * <ul>
	 * Is the entity refered to non-abstract
	 * <ul>
	 * Does the xref_field refer to a unique field (i.e. foreign key)</li>
	 * 
	 * @param model
	 * @throws MetaDataException
	 * @throws DatabaseException
	 */
	public static void validateForeignKeys(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("validate xref_field and xref_label references...");

		// validate foreign key relations
		for (EntityMetaData entity : model.getEntities())
		{
			String entityname = entity.getName();

			for (FieldMetaData field : entity.getFields())
			{
				String fieldname = field.getName();
				if (field.getType() instanceof XrefField || field.getType() instanceof MrefField)
				{

					EntityMetaData xref_entity = field.getXrefEntity();

					//List<String> xref_label_names = field.getXrefLabelNames();

					if (!xref_entity.equals(field.getXrefEntity())) throw new MetaDataException("xref entity '"
							+ xref_entity.getName() + "' does not exist for field " + entityname + "." + fieldname
							+ " (note: entity names are case-sensitive)");

					if (xref_entity.getAbstract())
					{
						throw new MetaDataException("cannot refer to abstract xref entity '"
								+ xref_entity.getName() + "' from field " + entityname + "." + fieldname);
					}

//					for (String xref_label_name : xref_label_names)
//					{
//						FieldModel xref_label = null;
//
//						xref_label = xref_entity.getAllField(xref_label_name);
//
//						// if null, check if a path to another xref_label:
//						// 'fieldname_xreflabel'
//						if (xref_label == null)
//						{
//							String validFields = "";
//							Map<String, List<FieldModel>> candidates = field.allPossibleXrefLabels();
//
//							if (candidates.size() == 0)
//							{
//								throw new MolgenisModelException(
//										"xref label '"
//												+ xref_label_name
//												+ "' does not exist for field "
//												+ entityname
//												+ "."
//												+ fieldname
//												+ ". \nCouldn't find suitable secondary keys to use as xref_label. \nDid you set a unique=\"true\" or <unique fields=\" ...>?");
//							}
//
//							for (String validLabel : candidates.keySet())
//							{
//								// logger.debug("Checking: "+validLabel);
//								if (xref_label_name.equals(validLabel))
//								{
//									xref_label = candidates.get(validLabel).get(candidates.get(validLabel).size() - 1);
//								}
//								validFields += "," + validLabel;
//							}
//
//							// still null, must be error
//							if (xref_label == null)
//							{
//								throw new MolgenisModelException("xref label '" + xref_label_name
//										+ "' does not exist for field " + entityname + "." + fieldname
//										+ ". Valid labels include " + validFields);
//							}
//
//						}
//						else
//						{
//							if (!xref_label_name.equals(field.getXrefField().getName())
//									&& !field.allPossibleXrefLabels().keySet().contains(xref_label_name))
//							{
//
//								// validate the label
//								String validLabels = "";
//								for (String label : field.allPossibleXrefLabels().keySet())
//								{
//									validLabels += label + ", ";
//								}
//
//								throw new MolgenisModelException("xref label '" + xref_label_name + "' for "
//										+ entityname + "." + fieldname
//										+ " is not part a secondary key. Valid labels are " + validLabels
//										+ "\nDid you set a unique=\"true\" or <unique fields=\" ...>?");
//							}
//						}
//
//					}
				}
			}
		}
	}

	/**
	 * Validate the unique constraints
	 * <ul>
	 * <li>Do unique field names refer to existing fields?
	 * <li>Is there a unique column id + unique label?
	 * </ul>
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public static void validateKeys(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("validate the fields used in 'unique' constraints...");

		// copy all 'unique' marked fields into proper UniqueModel
		for (EntityMetaData entity : model.getEntities())
		{
			int index = 0;
			for (FieldMetaData field : entity.getFields())
			{

				if (field.getUnique() && field.getEntity().equals(entity))
				{
					UniqueMetaData m = new UniqueMetaData();
					m.setFields(field.getName());
					m.setEntity(entity);
					entity.addUnique(index++, m);
				}
			}
		}

		// validate the keys
		for (EntityMetaData entity : model.getEntities())
		{
			String entityname = entity.getName();
			int autocount = 0;

			for (FieldMetaData field : entity.getAllFields())
			{
				String fieldname = field.getName();
				if (field.getType() instanceof IntField && field.getAuto())
				{
					autocount++;

					boolean iskey = false;

					for (UniqueMetaData unique : entity.getAllUniques())
					{
						for (FieldMetaData keyfield : unique.getFields())
						{
							if (keyfield.getName() == null) throw new MetaDataException("unique field '"
									+ fieldname + "' is not known in entity " + entityname);
							if (keyfield.getName().equals(field.getName())) iskey = true;
						}
					}

					if (!iskey) throw new MetaDataException(
							"there can be only one auto column and it must be the primary key for field '" + entityname
									+ "." + fieldname + "', found: " + autocount);
				}

				if (field.getType() instanceof EnumField)
				{
					if (field.getDefaultValue() != null && !"".equals(field.getDefaultValue())) if (!field
							.getEnumOptions().contains(field.getDefaultValue()))
					{
						throw new MetaDataException("default value '" + field.getDefaultValue()
								+ "' is not in enum_options for field '" + entityname + "." + fieldname + "'");
					}
				}
			}

			if (autocount > 1) throw new MetaDataException(
					"there should be only one auto column and it must be the primary key for entity '" + entityname
							+ "', found: " + autocount);

			// should have secondary key if there is a reference to this entity
			if (!entity.getAbstract() && autocount >= 1)
			{
				if (entity.getAllUniques().size() < 2)
				{
					// check references
					//boolean ref = false;
					for (EntityMetaData otherEntityModel : model.getEntities())
					{
						for (FieldMetaData f : otherEntityModel.getAllFields())
						{
							if ((f.getType() instanceof XrefField || f.getType() instanceof MrefField)
									&& entity.getName().equals(f.getXrefEntity()))
							{
								throw new MetaDataException(
										"if primary key is autoid, there should be secondary key for entity '"
												+ entityname + "' because of reference by "
												+ otherEntityModel.getName() + "." + f.getName());
							}
						}
					}
				}
			}

			// to strict, the unique field may be non-automatic
			if (!entity.getAbstract() && autocount < 1)
			{
				throw new MetaDataException(
						"there should be one auto column for each root entity and it must be the primary key for entity '"
								+ entityname + "'");
			}
		}

	}

	/**
	 * Validate extends and implements relationships:
	 * <ul>
	 * <li>Do superclasses actually exist
	 * <li>Do 'implements' refer to abstract superclasses (interfaces)
	 * <li>Do 'extends' refer to non-abstract superclasses
	 * <li>Copy primary key to subclass to form parent/child relationships
	 * </ul>
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	public static void validateExtendsAndImplements(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("validate 'extends' and 'implements' relationships...");
		// validate the extends and implements relations
		for (EntityMetaData entity : model.getEntities())
		{

			List<EntityMetaData> ifaces = entity.getAllImplements();
			for (EntityMetaData iface : ifaces)
			{
				if (!iface.getAbstract()) throw new MetaDataException(entity.getName() + " cannot implement "
						+ iface.getName() + " because it is not abstract");

				// copy primary key and xref_label from interface to subclass,
				// a primary key can have only one field.
				// usually it is a auto_number int
				// composite keys are ignored
				try
				{
					FieldMetaData pkeyField = null;
					if (iface.getKeyFields(EntityMetaData.PRIMARY_KEY).size() == 1)
					{
						pkeyField = iface.getKeyFields(EntityMetaData.PRIMARY_KEY).get(0);
						// if not already exists
						if (entity.getField(pkeyField.getName()) == null)
						{
							FieldMetaData field = new FieldMetaData(pkeyField);
							field.setEntity(entity);
							field.setAuto(pkeyField.getAuto());
							field.setNillable(pkeyField.getNillable());
							field.setReadonly(pkeyField.getReadonly());

							field.setSystem(true);
							field.setXrefEntity(iface.getName());
							field.setHidden(true);

							logger.debug("copy primary key " + field.getName() + " from interface " + iface.getName()
									+ " to " + entity.getName());
							entity.addField(field);

						}
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new MetaDataException(e.getMessage());
				}

			}

			if (entity.getExtends() != null)
			{
				EntityMetaData parent = entity.getExtends();
				if (parent == null) throw new MetaDataException("superclass '" + parent + "' for '"
						+ entity.getName() + "' is missing");
				if (parent.getAbstract()) throw new MetaDataException(entity.getName() + " cannot extend "
						+ entity.getName() + " because superclas " + parent.getName() + " is abstract (use implements)");
				if (entity.getAbstract()) throw new MetaDataException(entity.getName() + " cannot extend "
						+ parent.getName() + " because " + entity.getName() + " itself is abstract");
			}
		}
	}

	/**
	 * Add interfaces as artificial entities to the model
	 * 
	 * @param model
	 * @throws MetaDataException
	 * @throws Exception
	 */
	public static void addInterfaces(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("add root entities for interfaces...");
		for (EntityMetaData entity : model.getEntities())
		{
			// Generate the interface if rootAncestor (so has subclasses) and
			// itself is not an interface...
			if (entity.getExtends() != null)
			{
				EntityMetaData rootAncestor = entity;
				if (!entity.getAbstract())
				{

					// generate a new interface
					rootAncestor = new EntityMetaData();
					rootAncestor.setName("_" + entity.getName() + "Interface");
					rootAncestor
							.setDescription("Identity map table for "
									+ entity.getName()
									+ " and all its subclasses. "
									+ "For each row that is added to "
									+ entity.getName()
									+ " or one of its subclasses, first a row must be added to this table to get a valid primary key value.");
					// rootAncestor.setAbstract( true );

					// copy key fields to interface and unset auto key in child
					List<FieldMetaData> keyfields = entity.getUniques().get(0).getFields();
					List<String> keyfields_copy = new ArrayList<String>();
					for (FieldMetaData f : keyfields)
					{
						FieldMetaData key_field = new FieldMetaData();
						key_field.setEntity(rootAncestor);
						key_field.setType(f.getType());
						key_field.setName(f.getName());
						key_field.setAuto(f.getAuto());

						key_field.setNillable(f.getNillable());
						key_field.setReadonly(f.getReadonly());
						key_field.setDefaultValue(f.getDefaultValue());
						key_field.setDescription("Primary key field unique in " + entity.getName()
								+ " and its subclasses.");
						if (key_field.getType() instanceof StringField) key_field.setLength(key_field.getLength());
						rootAncestor.addField(key_field);
						keyfields_copy.add(key_field.getName());

						if (f.getAuto())
						{
							// unset auto key in original, but

							// SOLVED BY TRIGGERS Field autoField =
							// entity.getField(f.getName());
							// SOLVED BY TRIGGERS autoField.setAuto(false);

						}
					}
					rootAncestor.addUnique(keyfields_copy, entity.getUniques().get(0).getSubclass());

					// Vector<String> parents = new Vector<String>();
					// parents.add(rootAncestor.getName());
					// entity.setParents(parents);
				}

				// add the type enum to the root element
				List<EntityMetaData> subclasses = entity.getAllDescendants();
				List<String> enumOptions = new ArrayList<String>();
				enumOptions.add(entity.getName());
				for (EntityMetaData subclass : subclasses)
				{
					enumOptions.add(subclass.getName());
				}
				FieldMetaData type_field = new FieldMetaData();

				type_field.setEntity(rootAncestor);
				type_field.setType(new EnumField());
				type_field.setName(FieldMetaData.TYPE_FIELD);
				type_field.setAuto(true);

				type_field.setDescription("Subtypes of " + entity.getName() + ". Have to be set to allow searching");
				type_field.setEnumOptions(enumOptions);
				type_field.setHidden(true);
				rootAncestor.addField(0, type_field);
			}
		}
	}

	public static void validateNamesAndReservedWords(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("check for JAVA and SQL reserved words...");
		List<String> keywords = new ArrayList<String>();
		keywords.addAll(Arrays.asList(MOLGENIS_KEYWORDS));
		keywords.addAll(Arrays.asList(JAVA_KEYWORDS));
		keywords.addAll(Arrays.asList(JAVASCRIPT_KEYWORDS));
		keywords.addAll(Arrays.asList(ORACLE_KEYWORDS));
		keywords.addAll(Arrays.asList(MYSQL_KEYWORDS));
		keywords.addAll(Arrays.asList(HSQL_KEYWORDS));

		for (ModuleMetaData m : model.getModules())
		{
			if (m.getName().contains(" "))
			{
				throw new MetaDataException("module name '" + m.getName()
						+ "' illegal: it cannot contain spaces. Use 'label' if you want to show a name with spaces.");
			}
		}

		for (EntityMetaData e : model.getEntities())
		{
			if (e.getName().contains(" "))
			{
				throw new MetaDataException("entity name '" + e.getName()
						+ "' cannot contain spaces. Use 'label' if you want to show a name with spaces.");
			}

			if (keywords.contains(e.getName().toUpperCase()) || keywords.contains(e.getName().toLowerCase()))
			{
				throw new MetaDataException("entity name '" + e.getName() + "' illegal:" + e.getName()
						+ " is a reserved JAVA and/or SQL word and cannot be used for entity name");
			}
			for (FieldMetaData f : e.getFields())
			{
				if (f.getName().contains(" "))
				{
					throw new MetaDataException("field name '" + e.getName() + "." + f.getName()
							+ "' cannot contain spaces. Use 'label' if you want to show a name with spaces.");
				}

				if (keywords.contains(f.getName().toUpperCase()) || keywords.contains(f.getName().toLowerCase()))
				{
					throw new MetaDataException("field name '" + e.getName() + "." + f.getName() + "' illegal: "
							+ f.getName() + " is a reserved JAVA and/or SQL word");
				}

				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					EntityMetaData xref_entity = f.getXrefEntity();
					if (xref_entity != null
							&& (keywords.contains(xref_entity.getName().toUpperCase()) || keywords.contains(xref_entity
									.getName().toLowerCase())))
					{
						throw new MetaDataException("xref_entity reference from field '" + e.getName() + "."
								+ f.getName() + "' illegal: " + xref_entity + " is a reserved JAVA and/or SQL word");
					}

					if (f.getType() instanceof MrefField)
					{
						// default mref name is entityname+"_"+xreffieldname
						if (f.getMrefName() == null)
						{
							String mrefEntityModelName = f.getEntity().getName() + "_" + f.getName();

							// check if longer than 30 characters, then truncate
							if (mrefEntityModelName.length() > 30)
							{
								mrefEntityModelName = mrefEntityModelName.substring(0, 25)
										+ Integer.toString(mrefEntityModelName.hashCode()).substring(0, 5);
							}

							// paranoia check on uniqueness
							EntityMetaData mrefEntityModel = null;
							try
							{
								mrefEntityModel = model.getEntity(mrefEntityModelName);
							}
							catch (Exception exc)
							{
								throw new MetaDataException("mref name for " + f.getEntity().getName() + "."
										+ f.getName() + " not unique. Please use explicit mref_name=name setting");
							}

							if (mrefEntityModel != null)
							{
								mrefEntityModelName += "_mref";
								if (model.getEntity(mrefEntityModelName) != null)
								{
									mrefEntityModelName += "_" + Math.random();
								}
							}

							f.setMrefName(mrefEntityModelName);
						}
						if (f.getMrefLocalid() == null)
						{
							// default to entity name
							f.setMrefLocalid(f.getEntity().getName());
						}
						if (f.getMrefRemoteid() == null)
						{
							// default to xref entity name
							f.setMrefRemoteid(f.getName());
						}
					}
				}
			}
		}
	}

	/** test for case sensitivity */
	public static void correctXrefCaseSensitivity(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("correct case of names in xrefs...");
		for (EntityMetaData e : model.getEntities())
		{
			for (FieldMetaData f : e.getFields())
			{
				// f.setName(f.getName().toLowerCase());

				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					try
					{
						// correct for uppercase/lowercase typo's
						EntityMetaData xrefEntityModel = f.getXrefEntity();
						f.setXrefEntity(xrefEntityModel.getName());
						f.setXrefLabel(f.getXrefLabel().getName());
					}
					catch (Exception exception)
					{
						// exception.printStackTrace();
						// logger.error(exception);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @throws MetaDataException
	 */
	static public void copyDecoratorsToSubclass(MolgenisMetaData model) throws MetaDataException
	{
		logger.debug("copying decorators to subclasses...");
		for (EntityMetaData e : model.getEntities())
		{
			if (e.getDecorator() == null)
			{
				for (EntityMetaData superClass : e.getImplements())
				{
					if (superClass.getDecorator() != null)
					{
						e.setDecorator(superClass.getDecorator());
					}
				}
				for (EntityMetaData superClass : e.getAllExtends())
				{
					if (superClass.getDecorator() != null)
					{
						e.setDecorator(superClass.getDecorator());
					}
				}
			}
		}
	}

	public static final String[] MOLGENIS_KEYWORDS =
	{ "entity", "field", "form", "menu", "screen", "plugin" };

	public static final String[] HSQL_KEYWORDS =
	{ "ALIAS", "ALTER", "AUTOCOMMIT", "CALL", "CHECKPOINT", "COMMIT", "CONNECT", "CREATE", "COLLATION", "COUNT",
			"DATABASE", "DEFRAG", "DELAY", "DELETE", "DISCONNECT", "DROP", "END", "EXPLAIN", "EXTRACT", "GRANT",
			"IGNORECASE", "INDEX", "INSERT", "INTEGRITY", "LOGSIZE", "PASSWORD", "POSITION", "PLAN", "PROPERTY",
			"READONLY", "REFERENTIAl", "REVOKE", "ROLE", "ROLLBACK", "SAVEPOINT", "SCHEMA", "SCRIPT", "SCRIPTFORMAT",
			"SELECT", "SEQUENCE", "SET", "SHUTDOWN", "SOURCE", "TABLE", "TRIGGER", "UPDATE", "USER", "VIEW", "WRITE" };
	/**
	 * http://dev.mysql.com/doc/refman/5.0/en/reserved-words.html
	 */
	private static final String[] MYSQL_KEYWORDS =
	{ "Type", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT",
			"BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
			"COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS",
			"CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES",
			"DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
			"DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE",
			"DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE",
			"FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX",
			"INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8",
			"INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT",
			"LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT",
			"LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT",
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL",
			"NUMERIC", "ON", "OPTIMIZE", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PRECISION",
			"PRIMARY", "PROCEDURE", "PURGE", "RAID0", "READ", "READS", "REAL", "REFERENCES", "REGEXP", "RELEASE",
			"RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA",
			"SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SMALLINT", "SONAME",
			"SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT",
			"SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED",
			"THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE",
			"UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES",
			"VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "X509",
			"XOR", "YEAR_MONTH", "ZEROFILL" };
	/**
	 * https://cis.med.ucalgary.ca/http/java.sun.com/docs/books/tutorial/java/
	 * nutsandbolts/_keywords.html
	 */
	protected static final String[] JAVA_KEYWORDS =
	{ "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
			"boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
			"else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch",
			"extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally",
			"long", "strictfp", "volatile", "const", "float", "native", "super", "while" };

	protected static final String[] JAVASCRIPT_KEYWORDS =
	{ "function" };

	protected static String[] ORACLE_KEYWORDS =
	{

	"ACCESS", "ELSE", "MODIFY", "START", "ADD", "EXCLUSIVE", "NOAUDIT", "SELECT", "ALL", "EXISTS", "NOCOMPRESS",
			"SESSION", "ALTER", "FILE", "NOT", "SET", "AND", "FLOAT", "NOTFOUND", "SHARE", "ANY", "FOR", "NOWAIT",
			"SIZE", "ARRAYLEN", "FROM", "NULL", "SMALLINT", "AS", "GRANT", "NUMBER", "SQLBUF", "ASC", "GROUP", "OF",
			"SUCCESSFUL", "AUDIT", "HAVING", "OFFLINE", "SYNONYM", "BETWEEN", "IDENTIFIED", "ON", "SYSDATE", "BY",
			"IMMEDIATE", "ONLINE", "TABLE", "CHAR", "IN", "OPTION", "THEN", "CHECK", "INCREMENT", "OR", "TO",
			"CLUSTER", "INDEX", "ORDER", "TRIGGER", "COLUMN", "INITIAL", "PCTFREE", "UID", "COMMENT", "INSERT",
			"PRIOR", "UNION", "COMPRESS", "INTEGER", "PRIVILEGES", "UNIQUE", "CONNECT", "INTERSECT", "PUBLIC",
			"UPDATE", "CREATE", "INTO", "RAW", "USER", "CURRENT", "IS", "RENAME", "VALIDATE", "DATE", "LEVEL",
			"RESOURCE", "VALUES", "DECIMAL", "LIKE", "REVOKE", "VARCHAR", "DEFAULT", "LOCK", "ROW", "VARCHAR2",
			"DELETE", "LONG", "ROWID", "VIEW", "DESC", "MAXEXTENTS", "ROWLABEL", "WHENEVER", "DISTINCT", "MINUS",
			"ROWNUM", "WHERE", "DROP", "MODE", "ROWS", "WITH" };

	private static String firstToUpper(String string)
	{
		if (string == null) return " NULL ";
		if (string.length() > 0) return string.substring(0, 1).toUpperCase() + string.substring(1);
		else
			return " ERROR[STRING EMPTY] ";
	}

}