package org.molgenis.security;

import java.io.Serializable;
import java.util.List;

import org.molgenis.util.Entity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class RowLevelSecurityServiceImpl implements RowLevelSecurityService
{
	private final MutableAclService mutableAclService;
	private final PermissionEvaluator permissionEvaluator;

	public RowLevelSecurityServiceImpl(MutableAclService mutableAclService, PermissionEvaluator permissionEvaluator)
	{
		if (mutableAclService == null) throw new IllegalArgumentException("MutableAclService is null");
		this.mutableAclService = mutableAclService;
		this.permissionEvaluator = permissionEvaluator;
	}

	@Transactional
	@Override
	public void addPermissionToCurrentUser(Entity entity, EntityPermission permission)
	{
		UserDetails user = SecurityUtils.getCurrentUser();
		if (user == null)
		{
			throw new IllegalStateException("No current user logged in");
		}

		addPermission(user.getUsername(), entity, permission);
	}

	@Transactional
	@Override
	public void addPermission(String username, Entity entity, EntityPermission permission)
	{
		if (username == null) throw new IllegalArgumentException("Username is null");
		if (entity == null) throw new IllegalArgumentException("Entity is null");
		if (permission == null) throw new IllegalArgumentException("Permission is null");

		// TODO idValue should be serializable!!
		ObjectIdentity objectIdentity = new ObjectIdentityImpl(entity.getClass(), (Serializable) entity.getIdValue());
		Sid sid = new PrincipalSid(username);

		MutableAcl acl = null;
		try
		{
			acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
		}
		catch (NotFoundException nfe)
		{
			acl = mutableAclService.createAcl(objectIdentity);
		}

		switch (permission)
		{
			case OWNS:
				acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, sid, true);
				// no break
			case WRITE:
				acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, sid, true);
				acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, sid, true);
				acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, sid, true);
				// no break
			case READ:
				acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
				break;
		}

		mutableAclService.updateAcl(acl);
	}

	@Override
	public boolean currentUserHasPermission(Entity entity, EntityPermission entityPermission)
	{
		if (SecurityUtils.currentUserIsSu())
		{
			return true;
		}

		Permission permission = null;
		switch (entityPermission)
		{
			case OWNS:
				permission = BasePermission.ADMINISTRATION;
				break;
			case READ:
				permission = BasePermission.READ;
				break;
			case WRITE:
				permission = BasePermission.WRITE;
				break;
		}

		return permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity,
				permission);
	}

	@Override
	public <E extends Entity> List<E> filterList(List<E> entities, EntityPermission permission)
	{
		List<E> filtered = Lists.newArrayList();
		for (E entity : entities)
		{
			if (currentUserHasPermission(entity, permission))
			{
				filtered.add(entity);
			}
		}

		return filtered;
	}
}
