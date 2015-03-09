package org.molgenis.googlespreadsheet;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

public class GoogleSpreadsheetRepositoryCollection implements RepositoryCollection,
		ApplicationListener<ContextRefreshedEvent>
{
	public static final String NAME = "GoogleSpreadsheet";
	private final Map<String, Repository> repositories = new LinkedHashMap<>();
	private final SpreadsheetService spreadsheetService;
	private final String spreadsheetKey;
	private final DataService dataService;

	public GoogleSpreadsheetRepositoryCollection(SpreadsheetService spreadsheetService, String spreadsheetKey,
			DataService dataService)
	{
		super();
		this.spreadsheetService = spreadsheetService;
		this.spreadsheetKey = spreadsheetKey;
		this.dataService = dataService;
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return repositories.values().iterator();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		// TODO
		return getRepository(entityMeta.getName());
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public boolean hasRepository(String name)
	{
		return repositories.containsKey(name);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		try
		{
			URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadsheetKey, "public", "full");
			WorksheetFeed feed = spreadsheetService.getFeed(url, WorksheetFeed.class);
			for (WorksheetEntry w : feed.getEntries())
			{
				System.out.println(w.getId());
				Repository repo = new GoogleSpreadsheetRepository(spreadsheetService, spreadsheetKey, "owsk89m");
				repositories.put(repo.getName(), repo);
				dataService.getMeta().addEntityMeta(repo.getEntityMetaData());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
