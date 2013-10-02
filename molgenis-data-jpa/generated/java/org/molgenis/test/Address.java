package org.molgenis.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.molgenis.Entity;
import java.util.List;

import org.molgenis.EntityMetaData;
import org.molgenis.meta.FieldMetaData;
import org.molgenis.AttributeMetaData;

//data
import org.molgenis.Entity;
import org.molgenis.meta.types.TypeUtils;

//constraints
import javax.validation.constraints.*;

/**
 * address:
 * This is a test
 */
@javax.persistence.Entity
@javax.persistence.Inheritance(strategy=javax.persistence.InheritanceType.JOINED)
//@javax.persistence.DiscriminatorColumn(name="__Type", discriminatorType=javax.persistence.DiscriminatorType.STRING)
@javax.persistence.Table(name = "address")
public class Address  implements Entity
{
	// fieldname constants
	public final static String ID = "id";
	public final static String FIRSTNAME = "FirstName";
	public final static String LASTNAME = "LastName";
	public final static String BIRTHDAY = "Birthday";

	private static final long serialVersionUID = 1L;

	//properties for class
	//FieldModel(entity=address, name=id, type=autoid, auto=true, nillable=false, readonly=false, unique=false, default=null)
    @javax.persistence.Id 
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
	@javax.persistence.Column( name = "id")
	private Integer id = null;

	//FieldModel(entity=address, name=FirstName, type=null, auto=false, nillable=false, readonly=false, unique=false, default=null)
	@javax.validation.constraints.NotNull
	@Size(min=1,max=255,message="must consist of 1-255 characters")	
	@javax.persistence.Column( name = "FirstName")
	private String FirstName = null;

	//FieldModel(entity=address, name=LastName, type=null, auto=false, nillable=false, readonly=false, unique=false, default=null)
	@javax.validation.constraints.NotNull
	@Size(min=1,max=255,message="must consist of 1-255 characters")	
	@javax.persistence.Column( name = "LastName")
	private String LastName = null;

	//FieldModel(entity=address, name=Birthday, type=date, auto=false, nillable=false, readonly=false, unique=false, default=null)
	@javax.validation.constraints.NotNull
		
	@javax.persistence.Column( name = "Birthday")
	@javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private java.util.Date Birthday = null;
	
	//constructor
	public Address()
	{
		//set the type for a new instance
		//set__Type(this.getClass().getSimpleName());	
	}

	public Integer getIdValue()
	{	
		return this.getId();
	}
	
	public String getIdField()
	{	
		return "id";
	}
	
	public String getLabelValue()
	{	
		return TypeUtils.toString(this.getFirstName());
	}
	
	public String getLabelField()
	{	
		return "FirstName";
	}
	
	//getters & setters for class
	/** Get id
	 *  No description provided
	 */
	public Integer getId()
	{
		return this.id;
	}
	
	/** Set id
	 * No description provided
	 */
	public void setId(Integer id)
	{
		this.id = id;
	}
	/** Get FirstName
	 *  No description provided
	 */
	public String getFirstName()
	{
		return this.FirstName;
	}
	
	/** Set FirstName
	 * No description provided
	 */
	public void setFirstName(String firstName)
	{
		this.FirstName = firstName;
	}
	/** Get LastName
	 *  No description provided
	 */
	public String getLastName()
	{
		return this.LastName;
	}
	
	/** Set LastName
	 * No description provided
	 */
	public void setLastName(String lastName)
	{
		this.LastName = lastName;
	}
	/** Get Birthday
	 *  No description provided
	 */
	public java.util.Date getBirthday()
	{
		return this.Birthday;
	}
	
	/** Set Birthday
	 * No description provided
	 */
	public void setBirthday(java.util.Date birthday)
	{
		this.Birthday = birthday;
	}

	@Override
	public void set(Entity entity)
	{
		if(entity.get("id") != null)
		{
			this.set("id", entity.get("id"));
		}
		if(entity.get("FirstName") != null)
		{
			this.set("FirstName", entity.get("FirstName"));
		}
		if(entity.get("LastName") != null)
		{
			this.set("LastName", entity.get("LastName"));
		}
		if(entity.get("Birthday") != null)
		{
			this.set("Birthday", entity.get("Birthday"));
		}
	}
	
	@Override
	public void set(String field, Object value)
	{		
		if("id".equalsIgnoreCase(field))
		{
			this.setId(TypeUtils.toInteger(value));
		}
		if("FirstName".equalsIgnoreCase(field))
		{
			this.setFirstName(TypeUtils.toString(value));
		}
		if("LastName".equalsIgnoreCase(field))
		{
			this.setLastName(TypeUtils.toString(value));
		}
		if("Birthday".equalsIgnoreCase(field))
		{
			this.setBirthday(TypeUtils.toDate(value));
		}
	}		

	@Override
	public Object get(String colName)
	{
		if(colName == null) return null;
		if(colName.toLowerCase().equals("id"))
			return this.getId();
		if(colName.toLowerCase().equals("firstname"))
			return this.getFirstName();
		if(colName.toLowerCase().equals("lastname"))
			return this.getLastName();
		if(colName.toLowerCase().equals("birthday"))
			return this.getBirthday();
		return null;
	}

	@Override @javax.persistence.Transient
	public EntityMetaData getMetaData()
	{
		return new AddressMetaData();
	}
	
  	@Override
    public int hashCode() {
    	int firstNumber = this.getClass().getName().hashCode();
    	int secondNumber = this.getClass().getSimpleName().hashCode();
    	if(firstNumber % 2 == 0) {
    	  firstNumber += 1;
    	}
    	if(secondNumber % 2 == 0) {
    		secondNumber += 1;
    	}
    
		return new HashCodeBuilder(firstNumber, secondNumber)
   			.toHashCode();
    }  
    
    	@Override
	public boolean equals(Object obj) {
   		if (obj == null) { return false; }
   		if (obj == this) { return true; }
   		if (obj.getClass() != getClass()) {
     		return false;
   		}
		Address rhs = (Address) obj;
   		return new EqualsBuilder()
		//id
				.append(id, rhs.getId())
                .isEquals();
  	}		
	public int size()
	{
		return 4;
	}

	public String toString()
	{
		return "Address(id="+getId()+"FirstName="+getFirstName()+"," 
		+"LastName="+getLastName()+"," 
		+"Birthday="+getBirthday()+"," 
		+")";
	}

	@Override
	public String getDisplayValue(String attributeName)
	{
		AttributeMetaData attribute = getMetaData().getAttribute(attributeName);
		if (attribute != null)
		{
			return attribute.getDataType().toString(get(attributeName));
		}

		return null;
	}
}