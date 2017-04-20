package com.cas.framework.dao;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.cas.framework.support.DAOException;
import com.cas.framework.support.EnhancedRule;
import com.cas.framework.support.Page;
import com.cas.framework.utils.ReflectionUtil;
/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 *该类可以直接在任何dao代码中使用.每个方法参数加入了Class<T> entityClass
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SimpleHibernateDao {
	
	protected static final Logger logger = LogManager.getLogger(SimpleHibernateDao.class);
	@Autowired
    private SessionFactory sessionFactory;

	public Session getSession() {
    	return sessionFactory.getCurrentSession();
    } 
	/**
	 * 保存新增对象.
	 */
	public <T>T _save(T entity) {
		Assert.notNull(entity, "entity not null");
		getSession().save(entity);
		return entity;
	}
	/**
	 * 批量保存新增对象.
	 */
	public <T>void _save(T... entities) {
		for (int i = 0; i < entities.length; i++) {
			_save(entities[i]);
		}
		flush();
		clear();
	}
	/**
	 * persistget对象.
	 */
	public <T>void _persist(T... entities) {
		for (T entity : entities) {
			getSession().persist(entity);
		}
	}
	/**
	 * _merge对象.
	 */
	public <T> T _merge(T entity) {
		return (T) getSession().merge(entity);
	}
	/**
	 * 保存新增或修改的对象.在不确定id的时候使用。
	 */
	public <T>T _saveOrUpdate(T entity) {
		Assert.notNull(entity, "entity not null");
		getSession().saveOrUpdate(entity);
		return entity;
	}
	/**
	 * 保存修改的对象.
	 */
	public <T>T _update(T entity) {
		Assert.notNull(entity, "entity not null");
		getSession().update(entity);
		return entity;
	}
	/**
	 * 保存修改的对象.
	 */
	public <T>void _update(T... entities) {
		Assert.notNull(entities, "entity not null");
		for (T entity : entities) {
			_update(entity);
		}
	}
	
	/**
	 * 按id获取get对象.
	 */
	public <T>T _get(Class<T> entityClass, Serializable id) {
		Assert.notNull(id, "id not null");
		return (T) getSession().get(entityClass, id);
	}
	/**
	 * 按id获取get对象.
	 */
	public <T>T[] _get(Class<T> entityClass, Serializable... ids) {
		Assert.notNull(ids, "id not null");
		Criteria c = getSession().createCriteria(entityClass);
		c.add(Restrictions.in("id", ids));
		Object[] retVal = (Object[]) Array.newInstance(entityClass, ids.length);
		for (Object entity : c.list()) {
			Serializable id =(Serializable)ReflectionUtil.getFieldValue(entity, "id");//注意对象的主键属性必须和id对应。
			for (int i = 0; i < ids.length; i++) {
				if (id.equals(ids[i])) {
					retVal[i] = entity;
					break;
				}
			}
		}
		return (T[]) retVal;
	}
	/**
	 * load对象.如果不存在，抛出异常
	 */
	public <T> T _load(Class<T> entityClass, Serializable id) {
		return (T) getSession().load(entityClass, id);
	}
	/**
	 * load对象.如果不存在，抛出异常
	 */
	public <T> T[] _load(Class<T> entityClass, Serializable... ids) {
		Object[] retVal = (Object[]) Array.newInstance(entityClass, ids.length);
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] != null)
				retVal[i] = _load(entityClass, ids[i]);
		}
		return (T[]) retVal;
	}
	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public <T> void _delete(T entity) {
		Assert.notNull(entity, "entity not null");
		getSession().delete(entity);
	}
	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public <T> void _delete(T... entities) {
		Assert.notNull(entities, "entity not null");
		for (T entity : entities) {
			if (entity != null)
				_delete(entity);
		}
	}
	/**
	 * 按id删除对象.
	 */
	public <T> void _delete(Class<T> entityClass, Serializable id) {
		Assert.notNull(id, "id not null");
		_delete(_get(entityClass,id));
	}
	/**
	 * 按id删除对象.
	 */
	public <T> void _delete(Class<T> entityClass, Serializable... ids) {
		Assert.notNull(ids, "id not null");
		Criteria c = getSession().createCriteria(entityClass);
		c.add(Restrictions.in("id", ids));
		for (Object entity : c.list()) {
			_delete(entity);
		}
	}
	/**
	 * 根据指定属性的值获取对象.如果找到多个，抛出异常。
	 */
	public <T>T _getBy(Class<T> entityClass, final String propertyName, final Object value) {
		Assert.hasText(propertyName, "propertyName not null");
		Assert.notNull(value, "value not null");
		Criterion criterion = Restrictions.eq(propertyName, value);
		return (T) createCriteria(entityClass,criterion).uniqueResult();
	}
	/**
	 *	获取全部对象.
	 */
	public <T> List<T> _getAll(Class<T> entityClass) {
		return getSession().createCriteria(entityClass).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}
	/**
	 *	获取全部对象,支持排序.
	 */
	public <T> List<T> _getAll(Class<T> entityClass,String orderBy, boolean isAsc) {
		Criteria c = createCriteria(entityClass).setCacheable(true);
		if (isAsc) {
			c.addOrder(Order.asc(orderBy));
		} else {
			c.addOrder(Order.desc(orderBy));
		}
		return c.list();
	}
	/**
	 * 按HQL查询对象列表.
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	public <T>  List<T> _query(final String hql) {
		return createHQLQuery(hql).list();
	}
	public <T>  List<T> _query(final String hql,Map<String, Object> parameters) {
		Query query=createHQLQuery(hql);
		for(String key:parameters.keySet()){
			if(parameters.get(key) instanceof Collection){
				Collection vals=(Collection)parameters.get(key);
				query.setParameterList(key,vals);
			}else{
				query.setParameter(key,parameters.get(key));
			}
			
		}
		return query.list();
	}
	public <T> List<T> _queryByNoCache(final String hql,Map<String, Object> parameters) {
		Query query = getSession().createQuery(hql).setCacheable(false);
		for(String key:parameters.keySet()){
			query.setParameter(key,parameters.get(key));
		}
		return query.list();
	}
	public <T> List<T> _queryByNoCache(final String hql) {
		Query query = getSession().createQuery(hql).setCacheable(false);
		return query.list();
	}
	public <T>  List<T> _query(final String hql,final int offset,final int pageSize,Map<String, Object> parameters) {
		Query query=createHQLQuery(hql);
		for(String key:parameters.keySet()){
			query.setParameter(key,parameters.get(key));
		}
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		return query.list();
	}
	public <T>  List<T> _query(final String hql,final int offset,final int pageSize) {
		Query query=createHQLQuery(hql);
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		return query.list();
	}
	/**
	 * 根据扩展的查询对象，从数据库搜索相应的数据。
	 * @param enhanceRule
	 * @return E
	 */
	public <T> List<T> _query(Class<T> entityClass,EnhancedRule enhanceRule) {
		Criteria criteria = enhanceRule.getCriteria(entityClass,getSession());
		return criteria.list();
	}
	/**
	 * 根据EnhancedRule返回分页Page对象，EnhancedRule应该要设置firstResult和MaxResult
	 * 
	 * @param rule
	 * @return Page<T>
	 * @throws DAOException
	 */
	public <T> Page<T> _queryForPage(String hql,int offset,int pageSize) throws DAOException{
		Query query = createHQLQuery(hql);
		int total = query.list().size();
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		List list = query.list();
		Page page = new Page(offset, pageSize);
		page.setTotal(total);
		page.setRows(list);
		return page;
	}
	public <T> Page<T> _queryForPage(String hql,int offset,int pageSize,Map<String, Object> parameters) throws DAOException{
		Query query = createHQLQuery(hql);
		for(String key:parameters.keySet()){
			if(parameters.get(key) instanceof Collection){
				Collection vals=(Collection)parameters.get(key);
				query.setParameterList(key,vals);
			}else{
				query.setParameter(key,parameters.get(key));
			}
		}
		int total = query.list().size();
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		List list = query.list();
		Page page = new Page(offset, pageSize);
		page.setTotal(total);
		page.setRows(list);
		return page;
	}
	public <T> Page<T> _queryForPage(Class<T> entityClass,EnhancedRule enhanceRule) throws DAOException{
		Page<T> page = new Page<T>(enhanceRule.getOffset(), enhanceRule.getPageSize());
		List<T> list = _query(entityClass, enhanceRule);
		int total = (int) _count(entityClass, enhanceRule);
		page.setTotal(total);
		page.setRows(list);
		return page;
	}
	public <T> long _count(final Class<T> clazz, final EnhancedRule enhanceRule) {
		if (enhanceRule == null) {
			return getCount(_query(" select count(*) from " + clazz.getName()).get(0));
		}
		return getCount(enhanceRule.getCountCriteria(clazz, getSession()).setCacheable(true).uniqueResult());
	}
	/**
	 * 存储过程 并封装成一个对象
	 */
	public <T> List<T> _executeProcedure(String procedure,Class<T> clzss) {
		SQLQuery query =createSQLQuery(procedure); 
		return query.addEntity(clzss).list();
	}
	/**
	 *  根据sql查询对象，返回分页Page对象
	 * @param sql
	 * @param isMap 为true 返回map集合。
	 * @param parameters 参数集合。
	 * @param offset 起始页。
	 * @param pageSize 返回页。
	 * @return
	 */
	public <T> Page<T> _executeSQLForPage(String sql, Boolean isMap, Map<String, Object> parameters,int offset,int pageSize) throws DAOException{
		SQLQuery query = getSession().createSQLQuery(sql);
		for(String key:parameters.keySet()){
			if(parameters.get(key) instanceof Collection){
				Collection vals=(Collection)parameters.get(key);
				query.setParameterList(key,vals);
			}else{
				query.setParameter(key,parameters.get(key));
			}
		}
		if(isMap){
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		}else{
			query.setResultTransformer(Transformers.TO_LIST);
		}
		
		int total = query.list().size();
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		List list = query.list();
		Page page = new Page(offset, pageSize);
		page.setTotal(total);
		page.setRows(list);
		return page;
	}
	/**
	 *  根据sql查询对象，默认返回object数组。
	 * @param sql
	 * @param isMap 为true 返回map集合。
	 * @return
	 */
	public  List  _executeSQL(String sql,Boolean isMap) {
		SQLQuery query = getSession().createSQLQuery(sql);
		if(isMap){
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		}else{
			query.setResultTransformer(Transformers.TO_LIST);
		}
		return query.list();
	}
	/**
	 * 执行SQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int _executeSQL(final String sql) {
		return createSQLQuery(sql).executeUpdate();
	}
	/**
	 *  根据sql查询对象，默认返回object数组。
	 * @param sql
	 * @param isMap 为true 返回map集合。
	 * @param parameters 参数集合。
	 * @return
	 */
	public  List  _executeSQL(String sql,Boolean isMap,Map<String, Object> parameters) {
		SQLQuery query = getSession().createSQLQuery(sql);
		for(String key:parameters.keySet()){
			if(parameters.get(key) instanceof Collection){
				Collection vals=(Collection)parameters.get(key);
				query.setParameterList(key,vals);
			}else{
				query.setParameter(key,parameters.get(key));
			}
		}
		if(isMap){
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		}else{
			query.setResultTransformer(Transformers.TO_LIST);
		}
		return query.list();
	}
	/**
	 * 执行HQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int _executeHQL(final String hql) {
		return createHQLQuery(hql).executeUpdate();
	}
	/**
	 * 创建Criteria对象.
	 * 
	 * @param criterions
	 *            可变的Restrictions条件列表,见{@link #createQuery(String,Object...)}
	 */
	private <T> Criteria createCriteria(Class<T> entityClass,Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(entityClass).setCacheable(true);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}
	/**
	 * 根据查询HQL创建Query对象.setParameter
	 */
	private Query createHQLQuery(final String hql) {
		Assert.hasText(hql, "queryString not null");
		Query query = getSession().createQuery(hql).setCacheable(true);
		return query;
	}
	private SQLQuery createSQLQuery(final String sql) {
		Assert.hasText(sql, "queryString not null");
		SQLQuery query = getSession().createSQLQuery(sql);
		query.setCacheable(true);
		return query;
	}
	private void flush() {
		getSession ().flush ();
	}
	private void clear() {
		getSession ().clear();
	}
	/**
	 * @param obj
	 * @return int
	 */
	private int getCount(Object obj) {
		if (obj instanceof Long) {

			Long value = (Long) obj;
			return value.intValue();
		} else if (obj instanceof Integer) {

			Integer value = (Integer) obj;
			return value.intValue();
		}
		return 0;
	}
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
