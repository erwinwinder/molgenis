package org.molgenis.charts.charttypes;

import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.data.XYDataSerie;

public class LineChart extends XYDataChart
{
	public LineChart(XYDataSerie data)
	{
		super(ChartType.LINE_CHART, data);
	}

	@Override
	public String toString()
	{
		return "LineChart [data=" + getData() + ", width=" + getWidth() + ", height=" + getHeight() + "]";
	}

}
