package org.molgenis.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.molgenis.data.QueryRule.Operator;

public class Query
{
	protected final Vector<List<QueryRule>> rules = new Vector<List<QueryRule>>();

	final ArrayList<QueryRule> order = new ArrayList<QueryRule>();

	int limit = 0;
	
	int page = 1;

	public Query()
	{
		this.rules.add(new ArrayList<QueryRule>());
	}

	public void addRule(QueryRule addRules)
	{
		this.rules.lastElement().add(addRules);
	}
	
	

	public List<QueryRule> getRules()
	{
		if (this.rules.size() > 1) throw new QueryException("Nested query not closed, use unnest() or unnestAll()");
		if (this.rules.size() > 0)
		{
			List<QueryRule> rules = this.rules.lastElement();

			return rules;
		}
		else return new ArrayList<QueryRule>();
	}

	public Query search(String searchTerms) throws QueryException
	{
		rules.lastElement().add(new QueryRule(Operator.SEARCH, searchTerms));
		return this;
	}

	public Query or()
	{
		rules.lastElement().add(new QueryRule(Operator.OR));
		return this;
	}

	public Query like(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.LIKE, value));
		return this;
	}

	public Query eq(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.EQUALS, value));
		return this;
	}

	public Query in(String field, Iterable<?> objectIterator)
	{
		List<Object> objectList = new ArrayList<Object>();
		for(Object o: objectIterator) objectList.add(o);
		rules.lastElement().add(new QueryRule(field, Operator.IN, objectList));
		return this;
	}

	public Query gt(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.GREATER, value));
		return this;
	}

	public Query ge(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.GREATER_EQUAL, value));
		return this;
	}

	public Query le(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.LESS_EQUAL, value));
		return this;
	}

	public Query lt(String field, Object value)
	{
		rules.lastElement().add(new QueryRule(field, Operator.LESS, value));
		return this;
	}

	public Query limit(int limit)
	{
		this.limit = limit;
		return this;
	}

	public Query page(int page)
	{
		this.page = page;
		return this;
	}

	public Query asc(String orderByField)
	{
		rules.lastElement().add(new QueryRule(Operator.SORTASC, orderByField));
		return this;
	}

	public Query sortDESC(String orderByField)
	{
		rules.lastElement().add(new QueryRule(Operator.SORTDESC, orderByField));
		return this;
	}

	public Query nest()
	{
		// add element to our nesting list...
		this.rules.add(new ArrayList<QueryRule>());
		return this;
	}

	public Query unnest()
	{
		if (this.rules.size() == 1) throw new QueryException("Cannot unnest root element of query");

		// remove last element and add to parent as nested rule
		QueryRule nested = new QueryRule(Operator.NESTED, this.rules.lastElement());
		this.rules.remove(this.rules.lastElement());
		this.rules.lastElement().add(nested);
		return this;
	}

	public Query unnestAll()
	{
		while (this.rules.size() > 1)
		{
			unnest();
		}
		return this;
	}

	public Query rng(String field, Object smaller, Object bigger)
	{
		this.gt(field, smaller);
		this.lt(field, bigger);
		return this;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}
}
