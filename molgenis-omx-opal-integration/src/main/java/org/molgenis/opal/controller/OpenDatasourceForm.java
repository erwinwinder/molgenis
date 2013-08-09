package org.molgenis.opal.controller;

import org.hibernate.validator.constraints.NotBlank;

public class OpenDatasourceForm
{

	@NotBlank(message = "Opal server url is required")
	private String url = "http://demo.obiba.org:8080";

	@NotBlank(message = "Username is required")
	private String username = "administrator";

	@NotBlank(message = "Password is required")
	private String password;

	@NotBlank(message = "Local datasourcename is required")
	private String name;

	@NotBlank(message = "Remote opal datasourcename is required")
	private String remoteName;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRemoteName()
	{
		return remoteName;
	}

	public void setRemoteName(String remoteName)
	{
		this.remoteName = remoteName;
	}

}
