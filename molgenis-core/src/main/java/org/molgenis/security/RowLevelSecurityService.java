package org.molgenis.security;

import java.util.List;

import org.molgenis.util.Entity;

public interface RowLevelSecurityService
{
	void addPermission(String username, Entity entity, EntityPermission permission);

	void addPermissionToCurrentUser(Entity entity, EntityPermission permission);

	boolean currentUserHasPermission(Entity entity, EntityPermission permission);

	public <E extends Entity> List<E> filterList(List<E> entities, EntityPermission permission);
}
