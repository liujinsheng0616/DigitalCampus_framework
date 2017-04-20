package com.cas.framework.base;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.persistence.*;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *@Creat 2017年04月8日
 *@Author:kingson·liu
 *@todo 所有Entity的基類
 */
@MappedSuperclass
public class GenericEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6704011638970077677L;
	private static Logger logger = LoggerFactory.getLogger(GenericEntity.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
 	//@GenericGenerator(name="idGenerator", strategy="uuid") //这个是hibernate的注解
 	//@GeneratedValue(generator="idGenerator") //使用uuid的生成策略
	@Column(name = "id", unique = true, nullable = false)
	protected Long id;
	/**
	 * 覆蓋equals方法
	 * @param obj 
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj.getClass().isInstance(this) || this.getClass().isInstance(obj)))
			return false;
		try {
			GenericEntity entity = (GenericEntity) obj;
			if (entity.getId() == null && getId() == null) {
				boolean equals = true;
				Field[] fields = this.getClass().getDeclaredFields();
				for (Field field : fields) {
					Object value1 = field.get(this);
					Object value2 = field.get(entity);
					if (value1 == null && value2 == null)
						continue;
					if (value1 != null)
						equals = equals && value1.equals(value2); 
					if (!equals)
						break;
				}
				return equals;
			}
			if (entity.getId() == null || getId() == null)
				return false;
			return entity.getId().equals(getId());
		} catch (Exception e) {
			logger.error("can not reflect the Entity for:{} Exception",obj.getClass().getName());
			return false;
		}
	}
	/**
	 * 返回該類的hashCode
	 * @return int
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(-1785541825, 1605299849).appendSuper(
				super.hashCode()).append(this.getClass()).append(this.id).toHashCode();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
