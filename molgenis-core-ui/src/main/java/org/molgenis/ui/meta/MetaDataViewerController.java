package org.molgenis.ui.meta;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(MetaDataViewerController.URI)
public class MetaDataViewerController extends MolgenisPluginController
{
	public static final String ID = "MetaDataViewer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public MetaDataViewerController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		return "view-metadata-viewer";
	}

}
