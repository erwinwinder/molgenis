package org.molgenis.googlespreadsheet;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(GoogleSpreadsheetImportController.URI)
public class GoogleSpreadsheetImportController extends MolgenisPluginController
{
	public static final String ID = "googlespreadsheet";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final GoogleSpreadsheetImportService googleSpreadsheetImportService;

	@Autowired
	public GoogleSpreadsheetImportController(GoogleSpreadsheetImportService googleSpreadsheetImportService)
	{
		super(URI);
		this.googleSpreadsheetImportService = googleSpreadsheetImportService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showForm()
	{
		return "view-google-spreadsheet-importer";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String importSpreadsheet(@RequestParam("spreadsheetKey") String spreadsheetKey, Model model)
	{
		googleSpreadsheetImportService.importSpreadsheet(spreadsheetKey);
		model.addAttribute("successMessage", "Spreadsheet imported");
		return "view-google-spreadsheet-importer";
	}
}
