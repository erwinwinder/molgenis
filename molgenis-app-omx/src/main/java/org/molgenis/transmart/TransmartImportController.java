package org.molgenis.transmart;

import static org.molgenis.transmart.TransmartImportController.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping(URI)
public class TransmartImportController extends MolgenisPluginController
{
	private static final String TRANSMART_API_URL = "http://test-api.thehyve.net/transmart";
	private final Gson gson;
	private final HttpClient httpClient;

	public TransmartImportController()
	{
		super(URI);

		GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting();
		gson = builder.create();

		httpClient = new HttpClient();
	}

	public static final String ID = "transmart";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws HttpException, URISyntaxException, IOException
	{
		String jsessionId = login();
		getStudies(jsessionId);
		return "view-transmart";
	}

	private String login() throws HttpException, IOException
	{
		HttpMethod method = new PostMethod(TRANSMART_API_URL
				+ "/j_spring_security_check?j_username=admin&j_password=admin");
		// HttpMethod method = new PostMethod("http://localhost:8080/login?username=admin&password=admin");
		try
		{
			httpClient.executeMethod(method);
			Header[] cookies = method.getResponseHeaders("Set-Cookie");

			// Set-Cookie=JSESSIONID=3B607328203A3144CDD6E8A576530CDD; Path=/transmart
			String jsessionId = cookies[0].getValue().split("=")[1].split(";")[0];
			System.out.println(jsessionId);

			return jsessionId;
		}
		finally
		{
			method.releaseConnection();
		}

	}

	private List<String> getStudies(String jsessionId) throws URISyntaxException, HttpException, IOException
	{
		HttpMethod method = new GetMethod(TRANSMART_API_URL + "/studies");
		method.setRequestHeader("Cookie", "JSESSIONID=" + jsessionId);

		try
		{
			int responseCode = httpClient.executeMethod(method);
			if (responseCode != 200)
			{
				throw new RuntimeException("TransMART api returned responsecode " + responseCode);
			}

			InputStream in = method.getResponseBodyAsStream();
			String json = FileCopyUtils.copyToString(new InputStreamReader(in));
			System.out.println(json);

			Study[] studies = gson.fromJson(new InputStreamReader(in), Study[].class);
			List<String> studyNames = Lists.newArrayListWithCapacity(studies.length);
			for (Study study : studies)
			{
				studyNames.add(study.getName());
			}

			System.out.println(studyNames);

			return studyNames;
		}
		finally
		{
			method.releaseConnection();
		}

	}

	private static class Study
	{
		private String name;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

	}
}
