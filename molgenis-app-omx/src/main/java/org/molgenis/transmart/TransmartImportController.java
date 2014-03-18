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
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.datasetdeleter.DataSetDeleterService;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.EntityImportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping(URI)
@SessionAttributes("JSESSIONID")
public class TransmartImportController extends MolgenisPluginController
{
	public static final String ID = "transmart";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String TRANSMART_API_URL = "http://test-api.thehyve.net/transmart";
	private final Gson gson;
	private final HttpClient httpClient;
	private final DataService dataService;
	private final EntityValidator entityValidator;
	private final SearchService searchService;
	private final DataSetDeleterService dataSetDeleterService;

	@Autowired
	public TransmartImportController(DataService dataService, EntityValidator entityValidator,
			SearchService searchService, DataSetDeleterService dataSetDeleterService)
	{
		super(URI);

		this.dataService = dataService;
		this.entityValidator = entityValidator;
		this.searchService = searchService;
		this.dataSetDeleterService = dataSetDeleterService;
		GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
		gson = builder.create();
		httpClient = new HttpClient();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws HttpException, URISyntaxException, IOException
	{
		String jsessionId = login();
		List<String> studies = getStudies(jsessionId);
		model.addAttribute("studies", studies);
		model.addAttribute("JSESSIONID", jsessionId);

		return "view-transmart";
	}

	// @Transactional
	@RequestMapping(method = RequestMethod.POST)
	public String importStudy(String study, @ModelAttribute("JSESSIONID")
	String sessionId, Model model) throws HttpException, URISyntaxException, IOException
	{
		if (dataService.hasRepository(study))
		{
			dataService.removeRepository(study);
		}

		if (dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, study)) != null)
		{
			dataSetDeleterService.deleteData(study, true);
		}

		Concepts concepts = getConcepts(study, sessionId);
		DataSet dataSet = createDataSet(study, concepts);

		OmxRepository omxRepository = new OmxRepository(dataService, searchService, dataSet.getIdentifier(),
				entityValidator);
		dataService.addRepository(omxRepository);

		for (Subject subject : getSubjects(study, sessionId))
		{
			Observation[] observations = getSubjectObservations(study, subject.getId(), sessionId);

			Entity entity = new MapEntity();
			entity.set("Subject-" + study, subject.getId());

			for (Observation observation : observations)
			{

				correctDataType(observation.getConcept().getConceptPath(), observation.getValue());
				entity.set(observation.getConcept().getConceptPath(), observation.getValue());
			}

			omxRepository.add(entity);
		}

		// Index the repo
		ApplicationContextProvider.getApplicationContext().publishEvent(
				new EntityImportedEvent(this, DataSet.ENTITY_NAME, dataSet.getId()));

		return init(model);
	}

	private void correctDataType(String observableFeatureIdentifier, Object value)
	{
		if (value != null)
		{
			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, observableFeatureIdentifier),
					ObservableFeature.class);

			FieldType fieldType = MolgenisFieldTypes.getType(feature.getDataType());
			if ((value instanceof Double) && (fieldType.getEnumType() != FieldTypeEnum.DECIMAL))
			{
				feature.setDataType("decimal");
				dataService.update(ObservableFeature.ENTITY_NAME, feature);
			}
		}
	}

	private DataSet createDataSet(String study, Concepts concepts)
	{
		Protocol protocol = new Protocol();
		protocol.setIdentifier(study + "-protocol");
		protocol.setName(study);
		dataService.add(Protocol.ENTITY_NAME, protocol);

		DataSet dataSet = new DataSet();
		dataSet.setIdentifier(study);
		dataSet.setName(study);
		dataSet.setProtocolUsed(protocol);
		dataService.add(DataSet.ENTITY_NAME, dataSet);

		ObservableFeature subject = new ObservableFeature();
		subject.setIdentifier("Subject-" + study);
		subject.setName("Subject");
		dataService.add(ObservableFeature.ENTITY_NAME, subject);
		protocol.getFeatures().add(subject);

		for (OntologyTerm ot : concepts.getOntology_terms())
		{
			ObservableFeature feature = new ObservableFeature();
			feature.setIdentifier(ot.getFullName());
			feature.setName(ot.getName());
			dataService.add(ObservableFeature.ENTITY_NAME, feature);

			protocol.getFeatures().add(feature);
		}
		dataService.update(Protocol.ENTITY_NAME, protocol);

		return dataSet;
	}

	private String login() throws HttpException, IOException
	{
		HttpMethod method = new PostMethod(TRANSMART_API_URL
				+ "/j_spring_security_check?j_username=admin&j_password=admin");
		try
		{
			httpClient.executeMethod(method);
			Header[] cookies = method.getResponseHeaders("Set-Cookie");

			// Set-Cookie=JSESSIONID=3B607328203A3144CDD6E8A576530CDD; Path=/transmart
			String jsessionId = cookies[0].getValue().split("=")[1].split(";")[0];

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
			Study[] studies = gson.fromJson(new InputStreamReader(in), Study[].class);
			List<String> studyNames = Lists.newArrayListWithCapacity(studies.length);
			for (Study study : studies)
			{
				studyNames.add(study.getName());
			}

			return studyNames;
		}
		finally
		{
			method.releaseConnection();
		}

	}

	private Observation[] getSubjectObservations(String study, Long subjectId, String jsessionId) throws HttpException,
			IOException
	{
		String url = String.format("%s/studies/%s/subjects/%d/observations", TRANSMART_API_URL, study, subjectId);
		System.out.println(url);

		HttpMethod method = new GetMethod(url);
		method.setRequestHeader("Cookie", "JSESSIONID=" + jsessionId);

		try
		{
			int responseCode = httpClient.executeMethod(method);
			if (responseCode != 200)
			{
				throw new RuntimeException("TransMART api returned responsecode " + responseCode);
			}

			InputStream in = method.getResponseBodyAsStream();
			Observation[] observations = gson.fromJson(new InputStreamReader(in), Observation[].class);

			return observations;
		}
		finally
		{
			method.releaseConnection();
		}
	}

	private Concepts getConcepts(String study, String jsessionId) throws HttpException, IOException
	{
		String url = String.format("%s/studies/%s/concepts", TRANSMART_API_URL, study);
		HttpMethod method = new GetMethod(url);
		method.setRequestHeader("Cookie", "JSESSIONID=" + jsessionId);

		try
		{
			int responseCode = httpClient.executeMethod(method);
			if (responseCode != 200)
			{
				throw new RuntimeException("TransMART api returned responsecode " + responseCode);
			}

			InputStream in = method.getResponseBodyAsStream();
			Concepts concepts = gson.fromJson(new InputStreamReader(in), Concepts.class);

			return concepts;
		}
		finally
		{
			method.releaseConnection();
		}
	}

	private Subject[] getSubjects(String study, String jsessionId) throws HttpException, IOException
	{
		String url = String.format("%s/studies/%s/subjects", TRANSMART_API_URL, study);
		HttpMethod method = new GetMethod(url);
		method.setRequestHeader("Cookie", "JSESSIONID=" + jsessionId);

		try
		{
			int responseCode = httpClient.executeMethod(method);
			if (responseCode != 200)
			{
				throw new RuntimeException("TransMART api returned responsecode " + responseCode);
			}

			InputStream in = method.getResponseBodyAsStream();
			Subject[] subjects = gson.fromJson(new InputStreamReader(in), Subject[].class);

			return subjects;
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
	}

	private static class Observation
	{
		private Subject subject;
		private Concept concept;
		private Object value;

		@SuppressWarnings("unused")
		public Subject getSubject()
		{
			return subject;
		}

		public Concept getConcept()
		{
			return concept;
		}

		public Object getValue()
		{
			return value;
		}

		@Override
		public String toString()
		{
			return "Observation [subject=" + subject + ", concept=" + concept + ", value=" + value + "]";
		}

	}

	private static class Subject
	{
		private Long id;

		public Long getId()
		{
			return id;
		}

		@Override
		public String toString()
		{
			return "Subject [id=" + id + "]";
		}

	}

	private static class Concept
	{
		private String conceptCode;
		private String label;
		private String conceptPath;

		@SuppressWarnings("unused")
		public String getConceptCode()
		{
			return conceptCode;
		}

		@SuppressWarnings("unused")
		public String getLabel()
		{
			return label;
		}

		public String getConceptPath()
		{
			return conceptPath;
		}

	}

	private static class OntologyTerm
	{
		private String name;
		private String key;
		private String fullName;

		public String getName()
		{
			return name;
		}

		@SuppressWarnings("unused")
		public String getKey()
		{
			return key;
		}

		public String getFullName()
		{
			return fullName;
		}

	}

	private static class Concepts
	{
		private OntologyTerm[] ontology_terms;

		public OntologyTerm[] getOntology_terms()
		{
			return ontology_terms;
		}
	}

}
