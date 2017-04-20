package com.cas.framework.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cas.framework.support.DAOException;
import com.cas.framework.support.EnhancedRule;
import com.cas.framework.support.Page;
import com.cas.framework.utils.ReflectionUtil;

/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 该类可以直接在任何dao代码中使用.每个方法参数加入了Class<T> entityClass,批量
 */
public class GenericHibernateDAO<T> extends SimpleHibernateDao{
	protected static final Logger logger = LogManager.getLogger(GenericHibernateDAO.class);
	private  Class<T> entityClass=ReflectionUtil.getClassGenricType(this.getClass());
	/**
	 * 保存新增对象.
	 */
	public T save(T entity) throws DAOException{
		return _save(entity);
	}
	/**
	 * 批量保存新增对象.
	 */
	public void save(T... entities)throws DAOException{
		_save(entities);
	}
	/**
	 * persistget对象.
	 */
	public void persist(T... entities)throws DAOException{
		_persist(entities);
	}
	/**
	 * merge对象.
	 */
	public  T merge(T entity) throws DAOException{
		return _merge(entity);
	}
	/**
	 * 保存新增或修改的对象.在不确定id的时候使用。
	 */
	public T saveOrUpdate(T entity) throws DAOException{
		return _saveOrUpdate(entity);
	}
	/**
	 * 保存修改的对象.
	 */
	public T update(T entity) throws DAOException{
		return _update(entity);
	}
	/**
	 * 保存修改的对象.
	 */
	public void update(T... entities) throws DAOException{
		_update(entities);
	}
	
	/**
	 * 按id获取get对象.
	 */
	public T get( Serializable id) throws DAOException{
		return _get(entityClass, id);
	}
	/**
	 * 按id获取get对象.
	 */
	public T[] get( Serializable... ids) throws DAOException{
		return _get(entityClass, ids);
	}
	/**
	 * load对象.如果不存在，抛出异常
	 */
	public  T load( Serializable id) throws DAOException{
		return _load(entityClass, id);
	}
	/**
	 * load对象.如果不存在，抛出异常
	 */
	public  T[] load( Serializable... ids)throws DAOException{
		return _load(entityClass, ids);
	}
	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public  void delete(T entity) throws DAOException{
		_delete(entity);
	}
	/**
	 * 删除对象.是否真删除
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public  void delete(T entity, Boolean real) throws DAOException{
		_delete(entity);
	}
	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public  void delete(T... entities) throws DAOException{
		_delete(entities);
	}
	/**
	 * 按id删除对象.
	 */
	public  void delete( Serializable id) throws DAOException{
		_delete(get(entityClass,id));
	}
	/**
	 * 按id删除对象.
	 */
	public  void delete( Serializable... ids) throws DAOException{
		_delete(entityClass, ids);
	}
	/**
	 * 根据指定属性的值获取对象.如果找到多个，抛出异常。
	 */
	public T getBy( final String propertyName, final Object value)throws DAOException{
		return _getBy(entityClass, propertyName, value);
	}
	/**
	 *	获取全部对象.
	 */
	public  List<T> getAll() throws DAOException{
		return _getAll(entityClass);
	}
	/**
	 *	获取全部对象,支持排序.
	 */
	public  List<T> getAll(String orderBy, boolean isAsc) throws DAOException{
		return _getAll(entityClass, orderBy, isAsc);
	}
	/**
	 * 按HQL查询对象列表.
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	public   List<T> query(final String hql) throws DAOException{
		return _query(hql);
	}
	public   List<T> queryByNoCache(final String hql) throws DAOException{
		return _queryByNoCache(hql);
	}
	public   List<T> query(final String hql,final int offset,final int pageSize) throws DAOException{
		return _query(hql, offset, pageSize);
	}
	/**
	 * 根据扩展的查询对象，从数据库搜索相应的数据。
	 * @param enhanceRule
	 * @return E
	 */
	public  List<T> query(EnhancedRule enhanceRule) throws DAOException{
		return _query(entityClass, enhanceRule);
	}
	/**
	 * 根据EnhancedRule返回分页Page对象，EnhancedRule应该要设置firstResult和MaxResult
	 * 
	 * @param rule
	 * @return Page
	 * @throws DAOException
	 */
	public  Page<T> queryForPage(String hql,int offset,int pageSize) throws DAOException{
		return _queryForPage(hql, offset, pageSize);
	}
	public  Page<T> queryForPage(EnhancedRule enhanceRule) throws DAOException{
		return _queryForPage(entityClass, enhanceRule);
	}
	public  long count(final EnhancedRule enhanceRule) throws DAOException{
		return _count(entityClass, enhanceRule);
	}
	/**
	 * 存储过程 并封装成一个对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  List executeProcedure(String procedure,Class clzss) throws DAOException{
		return _executeProcedure(procedure, clzss);
	}
	/**
	 *  根据sql查询对象，默认返回object数组。
	 * @param sql
	 * @param isMap 为true 返回map集合。
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public  List executeSQL(String sql,Boolean isMap) throws DAOException{
		return _executeSQL(sql,isMap);
	}
	/**
	 * 执行SQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int executeSQL(final String sql)throws DAOException{
		return _executeSQL(sql);
	}
	/**
	 * 执行HQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int executeHQL(final String hql) throws DAOException{
		return _executeHQL(hql);
	}
}
