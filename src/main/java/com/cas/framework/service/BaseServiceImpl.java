package com.cas.framework.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.cas.framework.dao.GenericHibernateDAO;
import com.cas.framework.support.DAOException;
import com.cas.framework.support.EnhancedRule;
import com.cas.framework.support.Page;
import com.cas.framework.utils.DateUtil;
import com.cas.framework.utils.ReflectionUtil;

@SuppressWarnings({ "rawtypes" })
public class BaseServiceImpl<T> {
	protected static final Logger logger = LogManager.getLogger(BaseServiceImpl.class);
	private Class<T> entityClass = ReflectionUtil.getClassGenricType(this.getClass());
	@Autowired
	private GenericHibernateDAO<T> dao;

	/**
	 * 保存新增对象.
	 */
	public T save(T entity) {
		return dao._save(entity);
	}

	/**
	 * 批量保存新增对象.
	 */
	public void save(T... entities) {
		dao._save(entities);
	}

	/**
	 * persistget对象.
	 */
	public void persist(T... entities) {
		dao._persist(entities);
	}

	/**
	 * merge对象.
	 */
	public T merge(T entity) {
		return dao._merge(entity);
	}

	/**
	 * 保存新增或修改的对象.在不确定id的时候使用。
	 */
	public T saveOrUpdate(T entity) {
		return dao._saveOrUpdate(entity);
	}

	/**
	 * 保存修改的对象.
	 */
	public T update(T entity) {
		return dao._update(entity);
	}

	/**
	 * 保存修改的对象.
	 */
	public void update(T... entities) {
		dao._update(entities);
	}

	/**
	 * 按id获取get对象.
	 */
	public T get(Serializable id) {
		return dao._get(entityClass, id);
	}

	/**
	 * 按id获取get对象.
	 */
	public T[] get(Serializable... ids) {
		return dao._get(entityClass, ids);
	}

	/**
	 * load对象.如果不存在，抛出异常
	 */
	public T load(Serializable id) {
		return dao._load(entityClass, id);
	}

	/**
	 * load对象.如果不存在，抛出异常
	 */
	public T[] load(Serializable... ids) {
		return dao._load(entityClass, ids);
	}

	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public void delete(T entity) {
		dao._delete(entity);
	}

	/**
	 * 根据ID删除对象，true：真删除；false：假删除
	 * @param id
	 * @param real
	 */
	public void delete(Serializable id, Boolean real) {
		if (real) {
			dao._delete(entityClass, id);
		} else {
			dao._executeHQL("update " + entityClass.getSimpleName() + " set status=-1,updateTime='"
					+ DateUtil.formatDateToString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS) + "' where id=" + id);
		}
	}

	/**
	 * 删除对象.是否真删除
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public void delete(T entity, Boolean real) {
		dao._delete(entity);
	}

	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public void delete(T... entities) {
		dao._delete(entities);
	}

	/**
	 * 按id删除对象.
	 */
	public void delete(Serializable id) {
		dao._delete(get(entityClass, id));
	}

	/**
	 * 按id删除对象.
	 */
	public void delete(Serializable... ids) {
		dao._delete(entityClass, ids);
	}

	/**
	 * 根据指定属性的值获取对象.如果找到多个，抛出异常。
	 */
	public T getBy(final String propertyName, final Object value) {
		return dao._getBy(entityClass, propertyName, value);
	}

	/**
	 *	获取全部对象.
	 */
	public List<T> getAll() {
		return dao._getAll(entityClass);
	}

	/**
	 *	获取全部对象,支持排序.
	 */
	public List<T> getAll(String orderBy, boolean isAsc) {
		return dao._getAll(entityClass, orderBy, isAsc);
	}

	/**
	 * 按HQL查询对象列表.
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	public List<T> query(final String hql) {
		return dao._query(hql);
	}

	public List<T> query(final String hql, final int offset, final int pageSize) {
		return dao._query(hql, offset, pageSize);
	}

	/**
	 * 根据扩展的查询对象，从数据库搜索相应的数据。
	 * @param enhanceRule
	 * @return E
	 */
	public List<T> query(EnhancedRule enhanceRule) {
		return dao._query(entityClass, enhanceRule);
	}

	/**
	 * 根据EnhancedRule返回分页Page对象，EnhancedRule应该要设置firstResult和MaxResult
	 * 
	 * @param rule
	 * @return Page
	 * @throws DAOException
	 */
	public Page<T> queryForPage(String hql, int offset, int pageSize) throws DAOException {
		return dao._queryForPage(hql, offset, pageSize);
	}

	public Page<T> queryForPage(EnhancedRule enhanceRule) throws DAOException {
		return dao._queryForPage(entityClass, enhanceRule);
	}

	public long count(final EnhancedRule enhanceRule) {
		return dao._count(entityClass, enhanceRule);
	}

	/**
	 * 存储过程 并封装成一个对象
	 */
	public List<T> executeProcedure(String procedure) {
		return dao._executeProcedure(procedure, entityClass);
	}

	/**
	 *  根据sql查询对象，默认返回object数组。
	 * @param sql
	 * @param isMap 为true 返回map集合。
	 * @return
	 */
	public List executeSQL(String sql, Boolean isMap) {
		return dao._executeSQL(sql, isMap);
	}

	/**
	 * 执行SQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int executeSQL(final String sql) {
		return dao._executeSQL(sql);
	}

	/**
	 * 执行HQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int executeHQL(final String hql) {
		return dao._executeHQL(hql);
	}
}
