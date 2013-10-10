package org.molgenis.charts.data;

import java.util.List;

import org.molgenis.charts.ChartDataService;
import org.molgenis.charts.MolgenisChartException;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChartDataServiceImpl implements ChartDataService
{
	private final DataService dataService;

	@Autowired
	public ChartDataServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public XYDataSerie getXYDataSerie(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules)
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);

		if ((queryRules != null) && !(repo instanceof Queryable))
		{
			throw new MolgenisChartException("There a query rules defined but the " + entityName
					+ " repository is not queryable");
		}

		XYDataSerie serie = new XYDataSerie();

		if (queryRules == null)
		{
			for (AttributeMetaData attr : repo.getAttributes())
			{
				System.out.println(attr.getName());
			}

			for (Entity entity : repo)
			{
				Object x = entity.get(attributeNameXaxis);
				Object y = entity.get(attributeNameYaxis);

				serie.addData(new XYData(x, y));
			}
		}

		// TODO QueryRules

		return serie;
	}
}
