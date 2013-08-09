package org.molgenis.opal.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.opal.controller.OpalIntegrationController;

public class OpalIntegrationPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public OpalIntegrationPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return OpalIntegrationController.URI;
	}

}
