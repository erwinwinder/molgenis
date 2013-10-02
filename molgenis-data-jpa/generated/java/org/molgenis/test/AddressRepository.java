package org.molgenis.test;

import org.molgenis.data.jpa.JpaRepository;

import org.springframework.stereotype.Component;

@Component
public class AddressRepository extends JpaRepository<Address>
{
	@Override
	public AddressMetaData getEntityMetaData()
	{
		return new AddressMetaData();
	}

}