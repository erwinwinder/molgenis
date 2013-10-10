package org.molgenis.charts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.molgenis.data.DataService;
import org.molgenis.data.excel.ExcelEntitySourceFactory;
import org.molgenis.data.support.DataServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;

@Configuration
public class ChartConfig
{
	@Bean
	public DataService dataService() throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/weather.xls");
		File xlsFile = File.createTempFile("molgenis", ".xls");
		FileCopyUtils.copy(in, new FileOutputStream(xlsFile));
		String url = "excel://" + xlsFile.getAbsolutePath();

		DataService dataService = new DataServiceImpl();
		dataService.registerFactory(new ExcelEntitySourceFactory());
		dataService.registerEntitySource(url);

		return dataService;
	}
}
