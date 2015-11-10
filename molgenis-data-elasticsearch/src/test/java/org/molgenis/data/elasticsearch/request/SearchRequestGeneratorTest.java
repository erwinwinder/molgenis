package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.mockito.Matchers;
import org.molgenis.data.Fetch;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchRequestGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
	}

	@Test
	public void testBuildSearchRequest()
	{
		SearchRequestGenerator gen = new SearchRequestGenerator();
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;

		gen.buildSearchRequest(searchRequestBuilderMock, entityName, searchType,
				new QueryImpl().search("test").fetch(new Fetch().field("field1").field("field2")), null, null, null,
				null);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setFetchSource(new String[]
		{ "field1", "field2" }, null);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder> anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}

	@Test
	public void testBuildSearchRequestNoFetch()
	{
		SearchRequestGenerator gen = new SearchRequestGenerator();
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;

		gen.buildSearchRequest(searchRequestBuilderMock, entityName, searchType, new QueryImpl().search("test"), null,
				null, null, null);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder> anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}
}
