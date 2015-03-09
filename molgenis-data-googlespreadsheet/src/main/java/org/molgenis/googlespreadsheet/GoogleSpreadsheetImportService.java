package org.molgenis.googlespreadsheet;

import java.net.URL;

import org.elasticsearch.common.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.XrefField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

@Service
public class GoogleSpreadsheetImportService
{
	private final DataService dataService;

	@Autowired
	public GoogleSpreadsheetImportService(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void importSpreadsheet(String spreadsheetKey)
	{
		try
		{
			SpreadsheetService spreadsheetService = new SpreadsheetService("molgenis");
			URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadsheetKey, "public", "full");
			WorksheetFeed feed = spreadsheetService.getFeed(url, WorksheetFeed.class);
			WorksheetEntry attributesWorksheet = getWorksheet(feed, "attributes");
			WorksheetEntry entitiesWorksheet = getWorksheet(feed, "entities");

			// Meta
			ListFeed entities = spreadsheetService.getFeed(entitiesWorksheet.getListFeedUrl(), ListFeed.class);
			for (ListEntry entity : entities.getEntries())
			{
				String entityName = entity.getCustomElements().getValue("name");
				DefaultEntityMetaData entityMeta = new DefaultEntityMetaData(entityName);
				entityMeta.setLabel(entity.getCustomElements().getValue("label"));

				ListQuery query = new ListQuery(attributesWorksheet.getListFeedUrl());
				query.setSpreadsheetQuery("entity = \"" + entityName + "\"");
				ListFeed listFeed = spreadsheetService.query(query, ListFeed.class);
				for (ListEntry listEntry : listFeed.getEntries())
				{
					CustomElementCollection row = listEntry.getCustomElements();
					DefaultAttributeMetaData attr = new DefaultAttributeMetaData(row.getValue("name"));

					String dataType = row.getValue("dataType");
					dataType = StringUtils.isBlank(dataType) ? "string" : dataType;
					attr.setDataType(MolgenisFieldTypes.getType(dataType));

					if (attr.getDataType() instanceof XrefField)
					{
						attr.setRefEntity(dataService.getMeta().getEntityMetaData(row.getValue("refEntity")));
					}

					String idAttr = row.getValue("idAttribute");
					idAttr = StringUtils.isBlank(idAttr) ? "false" : idAttr;
					if (idAttr.equalsIgnoreCase("AUTO"))
					{
						attr.setAuto(true);
						attr.setIdAttribute(true);
						attr.setVisible(false);
					}
					else if (idAttr.equalsIgnoreCase("true"))
					{
						attr.setIdAttribute(true);
					}
					else
					{
						attr.setIdAttribute(false);
					}

					String nillable = row.getValue("nillable");
					nillable = StringUtils.isBlank(nillable) ? "false" : nillable;
					attr.setNillable(nillable.equalsIgnoreCase("true"));

					String aggregateable = row.getValue("aggregateable");
					aggregateable = StringUtils.isBlank(aggregateable) ? "false" : aggregateable;
					attr.setAggregateable(aggregateable.equalsIgnoreCase("true"));

					entityMeta.addAttributeMetaData(attr);
				}
				dataService.getMeta().addEntityMeta(entityMeta);

				// Data
				WorksheetEntry dataWorksheet = getWorksheet(feed, entityName);
				listFeed = spreadsheetService.getFeed(dataWorksheet.getListFeedUrl(), ListFeed.class);
				for (ListEntry listEntry : listFeed.getEntries())
				{
					CustomElementCollection row = listEntry.getCustomElements();

					Entity e = new DefaultEntity(entityMeta, dataService);
					for (AttributeMetaData attr : entityMeta.getAtomicAttributes())
					{
						e.set(attr.getName(), attr.getDataType().convert(row.getValue(attr.getName())));
					}

					dataService.add(entityName, e);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private WorksheetEntry getWorksheet(WorksheetFeed feed, String name)
	{
		for (WorksheetEntry w : feed.getEntries())
		{
			if (w.getTitle().getPlainText().equalsIgnoreCase(name))
			{
				return w;
			}
		}

		return null;
	}

	// @Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importSpreadsheet("1k432kG-QkpTjlA1C0qOjasVzSIBgCoyOWRBYOdUWZ7A");
	}
}
