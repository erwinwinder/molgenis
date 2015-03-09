package org.molgenis.googlespreadsheet;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gdata.client.spreadsheet.SpreadsheetService;

//@Configuration
public class GoogleSpreadsheetConfig
{
	@Autowired
	DataService dataService;

	@Autowired
	SearchService searchService;

	// @Bean
	public RepositoryCollection googleSpreadsheetRepositoryCollection()
	{
		try
		{
			SpreadsheetService spreadsheetService = new SpreadsheetService("molgenis");
			return new GoogleSpreadsheetRepositoryCollection(spreadsheetService,
					"1k432kG-QkpTjlA1C0qOjasVzSIBgCoyOWRBYOdUWZ7A", dataService);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
