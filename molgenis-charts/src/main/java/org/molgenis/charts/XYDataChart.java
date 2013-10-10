package org.molgenis.charts;

import org.molgenis.charts.data.XYDataSerie;

/**
 * A chart that uses xy data points like a LineChart or BarChart.
 */
public class XYDataChart extends Chart
{
	private final XYDataSerie data;

	protected XYDataChart(ChartType type, XYDataSerie data)
	{
		super(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		this.data = data;
	}

	public XYDataSerie getData()
	{
		return data;
	}

}
