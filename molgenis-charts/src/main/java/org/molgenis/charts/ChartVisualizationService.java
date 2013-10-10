package org.molgenis.charts;

import java.io.OutputStream;
import java.util.List;

import org.molgenis.charts.Chart.ChartType;

/**
 * Renders a chart and writes it to the OutputStream
 */
public interface ChartVisualizationService
{
	/**
	 * Returns what chart types this visualization service is capable of rendering
	 */
	List<ChartType> getCapabilities();

	/**
	 * Render the Chart and write if to the OutputStream.
	 * 
	 * When this service is not capable of rendering the requested chart type it throws a MolgenisChartException.
	 * 
	 * @param chart
	 * @param out
	 */
	void renderChart(Chart chart, OutputStream out) throws MolgenisChartException;
}
