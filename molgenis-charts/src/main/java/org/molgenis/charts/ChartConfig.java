package org.molgenis.charts;

import org.molgenis.data.DataService;
import org.molgenis.data.excel.ExcelEntitySourceFactory;
import org.molgenis.data.support.DataServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChartConfig
{
	@Bean
	public DataService dataService()
	{
		DataService dataService = new DataServiceImpl();
		dataService.registerFactory(new ExcelEntitySourceFactory());
		dataService
				.registerEntitySource("excel:///Users/erwin/projects/molgenis/molgenis-charts/src/main/resources/weather.xls");

		return dataService;
	}
}
