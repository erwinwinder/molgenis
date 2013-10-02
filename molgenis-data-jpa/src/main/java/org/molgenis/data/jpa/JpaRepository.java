package org.molgenis.data.jpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.molgenis.Entity;
import org.molgenis.EntityMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;

public abstract class JpaRepository<E extends Entity> implements CrudRepository<E>, Queryable<E>
{
	@PersistenceContext
	private EntityManager entityManager;
	private final Class<E> entityClass;

	@SuppressWarnings("unchecked")
	public JpaRepository()
	{
		Type t = getClass().getGenericSuperclass();
		ParameterizedType pt = (ParameterizedType) t;
		entityClass = (Class<E>) pt.getActualTypeArguments()[0];
	}

	public Class<E> getEntityClass()
	{
		return entityClass;
	}

	protected EntityManager getEntityManager()
	{
		return entityManager;
	}

	@Override
	public void create(E entity)
	{
		getEntityManager().persist(entity);
	}

	@Override
	public void create(Iterable<E> entities)
	{
		for (E e : entities)
			create(e);
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public abstract EntityMetaData getEntityMetaData();

	@Override
	public Iterator<E> iterator()
	{
		return findAll(new Query()).iterator();
	}

	@Override
	public long count()
	{
		return count(new Query());
	}

	@Override
	public long count(Query q)
	{
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		// gonna produce a number
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<E> from = cq.from(getEntityClass());
		cq.select(cb.count(from));

		// add filters
		createWhere(q, from, cq, cb);

		// execute the query
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public E findOne(Integer id)
	{
		return getEntityManager().find(getEntityClass(), id);
	}

	@Override
	public Iterable<E> findAll(Iterable<Integer> ids)
	{
		Query q = new Query().in(getEntityMetaData().getIdAttribute().getName(), ids);
		return findAll(q);
	}

	@Override
	public Iterable<E> findAll(Query q)
	{
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<E> cq = cb.createQuery(getEntityClass());
		Root<E> from = cq.from(getEntityClass());
		cq.select(from);

		// add filters
		createWhere(q, from, cq, cb);

		TypedQuery<E> tq = em.createQuery(cq);
		if (q.getLimit() > 0) tq.setMaxResults(q.getLimit());
		if (q.getPage() > 1) tq.setFirstResult((q.getPage() - 1) * q.getLimit());

		return tq.getResultList();

	}

	@Override
	public void update(E entity)
	{
		EntityManager em = getEntityManager();
		em.merge(entity);
		em.flush();
	}

	@Override
	public void update(Iterable<E> entities)
	{
		EntityManager em = getEntityManager();
		int batchSize = 500;
		int batchCount = 0;
		for (E r : entities)
		{
			em.merge(r);
			batchCount++;
			if (batchCount == batchSize)
			{
				em.flush();
				em.clear();
				batchCount = 0;
			}
		}
		em.flush();

	}

	@Override
	public void delete(Integer id)
	{
		delete(findOne(id));
	}

	@Override
	public void delete(E entity)
	{
		EntityManager em = getEntityManager();
		em.remove(entity);
		em.flush();
	}

	@Override
	public void delete(Iterable<E> entities)
	{
		EntityManager em = getEntityManager();

		for (E r : entities)
			em.remove(r);

		em.flush();
	}

	@Override
	public void deleteAll()
	{
		delete(this);
	}

	private void createWhere(Query q, Root<?> from, CriteriaQuery<?> cq, CriteriaBuilder cb)
	{
		List<Predicate> where = createPredicates(from, cb, q.getRules());
		if (where != null) cq.where(cb.and(where.toArray(new Predicate[where.size()])));
		List<Order> orders = createOrder(from, cb, q.getRules());
		if (orders != null && orders.size() > 0) cq.orderBy(orders);
	}

	/** Converts MOLGENIS query rules into JPA predicates */
	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	private List<Predicate> createPredicates(Root<?> from, CriteriaBuilder cb, List<QueryRule> rules)
	{
		// default Query links criteria based on 'and'
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		// optionally, subqueries can be formulated seperated by 'or'
		List<Predicate> orPredicates = new ArrayList<Predicate>();

		for (QueryRule r : rules)
		{
			switch (r.getOperator())
			{
				case NESTED:
					Predicate nested = cb.conjunction();
					for (Predicate p : createPredicates(from, cb, r.getNestedRules()))
					{
						nested.getExpressions().add(p);
					}
					andPredicates.add(nested);
					break;
				case OR:
					orPredicates.add(cb.and(andPredicates.toArray(new Predicate[andPredicates.size()])));
					andPredicates.clear();
					break;
				case EQUALS:
					andPredicates.add(cb.equal(from.get(r.getField()), r.getValue()));
					break;
				case LIKE:
					String like = "%" + r.getValue() + "%";
					String f = r.getField();
					andPredicates.add(cb.like(from.<String> get(f), like));
					break;
				default:
					// go into comparator based criteria, that need
					// conversion...
					Path<Comparable> field = from.get(r.getField());
					Object value = r.getValue();
					Comparable cValue = null;

					// convert to type
					if (field.getJavaType() == Integer.class)
					{
						if (value instanceof Integer) cValue = (Integer) value;
						else cValue = Integer.parseInt(value.toString());
					}
					else if (field.getJavaType() == Long.class)
					{
						if (value instanceof Long) cValue = (Long) value;
						else cValue = Long.parseLong(value.toString());
					}
					else if (field.getJavaType() == Date.class)
					{
						if (value instanceof Date) cValue = (Date) value;
						else cValue = Date.parse(value.toString());
					}
					else throw new RuntimeException("canno solve query rule:  " + r);

					// comparable values...
					switch (r.getOperator())
					{
						case GREATER:
							andPredicates.add(cb.greaterThan(field, cValue));
							break;
						case LESS:
							andPredicates.add(cb.lessThan(field, cValue));
							break;
						default:
							throw new RuntimeException("canno solve query rule:  " + r);
					}
			}
		}
		if (orPredicates.size() > 0)
		{
			if (andPredicates.size() > 0)
			{
				orPredicates.add(cb.and(andPredicates.toArray(new Predicate[andPredicates.size()])));
			}
			List<Predicate> result = new ArrayList<Predicate>();
			result.add(cb.or(orPredicates.toArray(new Predicate[andPredicates.size()])));
			return result;
		}
		else
		{
			if (andPredicates.size() > 0)
			{
				return andPredicates;
			}
			return new ArrayList<Predicate>();
		}
	}

	private List<Order> createOrder(Root<?> from, CriteriaBuilder cb, List<QueryRule> rules)
	{
		List<Order> orders = new ArrayList<Order>();

		for (QueryRule r : rules)
		{
			switch (r.getOperator())
			{
				case NESTED:
					orders.addAll(this.createOrder(from, cb, r.getNestedRules()));
					break;
				case SORTDESC:
					orders.add(cb.desc(from.get(r.getField())));
					break;
				case SORTASC:
					orders.add(cb.asc(from.get(r.getField())));
					break;
				default:
					break;
			}
		}

		return orders;
	}
}