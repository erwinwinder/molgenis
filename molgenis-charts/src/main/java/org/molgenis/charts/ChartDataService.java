package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.data.QueryRule;

/**
 * Retrieves chart data for rendering
 */
public interface ChartDataService
{
	/**
	 * Get XYDataSerie chart data based on a entityname and a query
	 */
	XYDataSerie getXYDataSerie(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);
}
