package org.molgenis.charts;

import static org.molgenis.charts.ChartController.URI;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.molgenis.charts.charttypes.LineChart;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.data.QueryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class ChartController
{
	public static final String URI = "/charts";
	private final ChartDataService chartDataService;

	@Autowired
	public ChartController(ChartDataService chartDataService)
	{
		if (chartDataService == null) throw new IllegalArgumentException("chartDataService is null");
		this.chartDataService = chartDataService;
	}

	@RequestMapping("/test")
	public String test(@RequestParam(value = "x", required = false)
	String x, @RequestParam(value = "y", required = false)
	String y, Model model)
	{
		if (x == null)
		{
			x = "YYYYMMDD";
		}

		if (y == null)
		{
			y = "TG";
		}

		model.addAttribute("x", x);
		model.addAttribute("y", y);

		return "test";
	}

	@RequestMapping("/highchart")
	public String chart()
	{
		return "highchart";
	}

	@RequestMapping("/line")
	public String renderLineChart(@Valid
	LineChartRequest request, Model model)
	{
		List<QueryRule> queryRules = null;// TODO
		XYDataSerie data = chartDataService.getXYDataSerie(request.getEntity(), request.getX(), request.getY(),
				queryRules);

		Chart chart = new LineChart(data);
		chart.setWidth(request.getWidth());
		chart.setHeight(request.getHeight());
		model.addAttribute("chart", chart);

		return "highchart";
	}

	public static class LineChartRequest
	{
		@NotNull
		private String entity;

		@NotNull
		private String x;

		@NotNull
		private String y;

		private int width = Chart.DEFAULT_WITH;
		private int height = Chart.DEFAULT_HEIGHT;

		public String getEntity()
		{
			return entity;
		}

		public void setEntity(String entity)
		{
			this.entity = entity;
		}

		public String getX()
		{
			return x;
		}

		public void setX(String x)
		{
			this.x = x;
		}

		public String getY()
		{
			return y;
		}

		public void setY(String y)
		{
			this.y = y;
		}

		public int getWidth()
		{
			return width;
		}

		public void setWidth(int width)
		{
			this.width = width;
		}

		public int getHeight()
		{
			return height;
		}

		public void setHeight(int height)
		{
			this.height = height;
		}

	}
}
