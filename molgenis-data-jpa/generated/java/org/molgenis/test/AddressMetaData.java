package org.molgenis.test;

import org.molgenis.meta.EntityMetaData;
import org.molgenis.meta.FieldMetaData;

public class AddressMetaData extends EntityMetaData
{
	public  AddressMetaData()
	{
		this.setName("Address");
		this.setXrefLabel("FirstName");
		
		FieldMetaData id_field = new FieldMetaData();
		id_field.setName("Id");
		id_field.setType("autoid");
		id_field.setNillable(false);
		id_field.setAuto(true);
		id_field.setDescription("No description provided");
		this.addField(id_field);	
		FieldMetaData FirstName_field = new FieldMetaData();
		FirstName_field.setName("FirstName");
		FirstName_field.setType("string");
		FirstName_field.setNillable(false);
		FirstName_field.setAuto(false);
		FirstName_field.setDescription("No description provided");
		this.addField(FirstName_field);	
		FieldMetaData LastName_field = new FieldMetaData();
		LastName_field.setName("LastName");
		LastName_field.setType("string");
		LastName_field.setNillable(false);
		LastName_field.setAuto(false);
		LastName_field.setDescription("No description provided");
		this.addField(LastName_field);	
		FieldMetaData Birthday_field = new FieldMetaData();
		Birthday_field.setName("Birthday");
		Birthday_field.setType("date");
		Birthday_field.setNillable(false);
		Birthday_field.setAuto(false);
		Birthday_field.setDescription("No description provided");
		this.addField(Birthday_field);	
	}
}