package com.remainer.redis.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

/**
 * redis BaseDao 依赖Gson，Jedis key用Id注解
 * 实现了增删查，redis添加同key就是改
 * @author 李东坡
 *
 * @param <T>
 */
public class BaseDao<T> {
	private Class<T> t;
	private Gson gson = new Gson();
	private Jedis jedis = RedisUtil.getJedis();
	@SuppressWarnings("unchecked")
	public BaseDao() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();  
        t = (Class<T>) type.getActualTypeArguments()[0];
	}
	//增
	public void add(T entity){
		jedis.set(getId(entity), gson.toJson(entity));	
	}
	//查
	public T get(String id){
		return new Gson().fromJson(jedis.get(id), t);
	}
	//删
	public void remove(String id){
		jedis.del(id);
	}
	
	private String getId(T entity){
		String id = null;
		Field[] fields = t.getDeclaredFields();
        for(Field f : fields){
        	Annotation annotation = f.getAnnotation(Id.class);
        	if(annotation != null){
        		try {
        			f.setAccessible(true);
					id = (String) f.get(entity);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
        	}
        }
        return id;
	}
}
