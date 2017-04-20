package com.cas.framework.base;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * entity的基类，实现equals和hash，作为后续实现cache的入口点。
 */
public  class BaseEntity  implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4550525133350154394L;

	public static final Logger logger = LogManager.getLogger(GenericEntity.class);
	
	protected Long id;
	/**
	 * 额外扩展的属性，支持在po上直接put,get
	 */
	public Map<String,Object> extra=new HashMap<String, Object>();
	
	public void put(String key,Object value) {
		extra.put(key, value);
		
	}
	public Object get(String key){
		return extra.get(key);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj.getClass().isInstance(this) || this.getClass().isInstance(obj)))
			return false;
		try {
			BaseEntity entity = (BaseEntity) obj;
			if(entity.getId() != null && getId() != null){
				return entity.getId().equals(getId());
			}else{
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
		} catch (Exception e) {
			logger.error("can not reflect the Entity for: "+obj.getClass().getName());
			return false;
		}
	}
	/**
	 * 返回該類的hashCode
	 * @return int
	 */
	@Override
	public int hashCode() {
		if(getId()==null){
			return 0;
		}
		int result = 1;
		int idHash = (int)(getId() ^ (getId() >>> 32));
        result = 31 * result + idHash;
        return result;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
