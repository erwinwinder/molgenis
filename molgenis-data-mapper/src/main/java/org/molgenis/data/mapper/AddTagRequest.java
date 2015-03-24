package org.molgenis.data.mapper;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddTagRequest.class)
public abstract class AddTagRequest
{
	@NotNull
	public abstract String getEntityName();

	@NotNull
	public abstract String getAttributeName();

	@NotNull
	public abstract String getRelationIRI();

	@NotEmpty
	public abstract List<String> getOntologyTermIRIs();

}
