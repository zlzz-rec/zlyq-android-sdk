package com.zlyq.client.android.analytics.db.utils;

import com.zlyq.client.android.analytics.db.annotations.Id;
import com.zlyq.client.android.analytics.db.annotations.ZlyqTable;
import com.zlyq.client.android.analytics.db.exception.ZlyqDbExceptionZlyq;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqManyToOneLazyLoader;
import com.zlyq.client.android.analytics.db.table.ZlyqManyToOne;
import com.zlyq.client.android.analytics.db.table.ZlyqOneToMany;
import com.zlyq.client.android.analytics.db.table.ZlyqProperty;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ZlyqClassUtils {

	/**
	 * 根据实体类 获得 实体类对应的表名
	 * @return
	 */
	public static String getTableName(Class<?> clazz) {
		ZlyqTable zlyqTable = clazz.getAnnotation(ZlyqTable.class);
		if(zlyqTable == null || zlyqTable.name().trim().length() == 0 ){
			//当没有注解的时候默认用类的名称作为表名,并把点（.）替换为下划线(_)
			return clazz.getName().replace('.', '_');
		}
		return zlyqTable.name();
	}
	
	public static Object getPrimaryKeyValue(Object entity) {
		return ZlyqFieldUtils.getFieldValue(entity, ZlyqClassUtils.getPrimaryKeyField(entity.getClass()));
	}

	/**
	 * 根据实体类 获得 实体类对应的表名
	 * @return
	 */
	public static String getPrimaryKeyColumn(Class<?> clazz) {
		String primaryKey = null ;
		Field[] fields = clazz.getDeclaredFields();
		if(fields != null){
			Id idAnnotation = null ;
			Field idField = null ;

			for(Field field : fields){ //获取ID注解
				idAnnotation = field.getAnnotation(Id.class);
				if(idAnnotation != null){
					idField = field;
					break;
				}
			}

			if(idAnnotation != null){ //有ID注解
				primaryKey = idAnnotation.column();
				if(primaryKey == null || primaryKey.trim().length() == 0)
					primaryKey = idField.getName();
			}else{ //没有ID注解,默认去找 _id 和 id 为主键，优先寻找 _id
				for(Field field : fields){
					if("_id".equals(field.getName()))
						return "_id";
				}

				for(Field field : fields){
					if("id".equals(field.getName()))
						return "id";
				}
			}
		}else{
			throw new RuntimeException("this model["+clazz+"] has no field");
		}
		return primaryKey;
	}


	/**
	 * 根据实体类 获得 实体类对应的表名
	 * @return
	 */
	public static Field getPrimaryKeyField(Class<?> clazz) {
		Field primaryKeyField = null ;
		Field[] fields = clazz.getDeclaredFields();
		if(fields != null){

			for(Field field : fields){ //获取ID注解
				if(field.getAnnotation(Id.class) != null){
					primaryKeyField = field;
					break;
				}
			}

			if(primaryKeyField == null){ //没有ID注解
				for(Field field : fields){
					if("_id".equals(field.getName())){
						primaryKeyField = field;
						break;
					}
				}
			}

			if(primaryKeyField == null){ // 如果没有_id的字段
				for(Field field : fields){
					if("id".equals(field.getName())){
						primaryKeyField = field;
						break;
					}
				}
			}

		}else{
			throw new RuntimeException("this model["+clazz+"] has no field");
		}
		return primaryKeyField;
	}


	/**
	 * 根据实体类 获得 实体类对应的表名
	 * @return
	 */
	public static String getPrimaryKeyFieldName(Class<?> clazz) {
		Field f = getPrimaryKeyField(clazz);
		return f==null ? null:f.getName();
	}



	/**
	 * 将对象转换为ContentValues
	 *
	 * @return
	 */
	public static List<ZlyqProperty> getPropertyList(Class<?> clazz) {

		List<ZlyqProperty> plist = new ArrayList<ZlyqProperty>();
		try {
			Field[] fs = clazz.getDeclaredFields();
			String primaryKeyFieldName = getPrimaryKeyFieldName(clazz);
			for (Field f : fs) {
				//必须是基本数据类型和没有标瞬时态的字段
				if(!ZlyqFieldUtils.isTransient(f)){
					if (ZlyqFieldUtils.isBaseDateType(f)) {

						if(f.getName().equals(primaryKeyFieldName)) //过滤主键
							continue;

						ZlyqProperty zlyqProperty = new ZlyqProperty();

						zlyqProperty.setColumn(ZlyqFieldUtils.getColumnByField(f));
						zlyqProperty.setFieldName(f.getName());
						zlyqProperty.setDataType(f.getType());
						zlyqProperty.setDefaultValue(ZlyqFieldUtils.getPropertyDefaultValue(f));
						zlyqProperty.setSet(ZlyqFieldUtils.getFieldSetMethod(clazz, f));
						zlyqProperty.setGet(ZlyqFieldUtils.getFieldGetMethod(clazz, f));
						zlyqProperty.setField(f);

						plist.add(zlyqProperty);
					}
				}
			}
			return plist;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * 将对象转换为ContentValues
	 *
	 * @return
	 */
	public static List<ZlyqManyToOne> getManyToOneList(Class<?> clazz) {

		List<ZlyqManyToOne> mList = new ArrayList<ZlyqManyToOne>();
		try {
			Field[] fs = clazz.getDeclaredFields();
			for (Field f : fs) {
				if (!ZlyqFieldUtils.isTransient(f) && ZlyqFieldUtils.isManyToOne(f)) {

					ZlyqManyToOne mto = new ZlyqManyToOne();
                    //如果类型为ManyToOneLazyLoader则取第二个参数作为manyClass（一方实体） 2013-7-26
                    if(f.getType()== ZlyqManyToOneLazyLoader.class){
                        Class<?> pClazz = (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[1];
                        if(pClazz!=null)
                            mto.setManyClass(pClazz);
                    }else {
					    mto.setManyClass(f.getType());
                    }
					mto.setColumn(ZlyqFieldUtils.getColumnByField(f));
					mto.setFieldName(f.getName());
					mto.setDataType(f.getType());
					mto.setSet(ZlyqFieldUtils.getFieldSetMethod(clazz, f));
					mto.setGet(ZlyqFieldUtils.getFieldGetMethod(clazz, f));

					mList.add(mto);
				}
			}
			return mList;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * 将对象转换为ContentValues
	 *
	 * @return
	 */
	public static List<ZlyqOneToMany> getOneToManyList(Class<?> clazz) {

		List<ZlyqOneToMany> oList = new ArrayList<ZlyqOneToMany>();
		try {
			Field[] fs = clazz.getDeclaredFields();
			for (Field f : fs) {
				if (!ZlyqFieldUtils.isTransient(f) && ZlyqFieldUtils.isOneToMany(f)) {

					ZlyqOneToMany otm = new ZlyqOneToMany();

					otm.setColumn(ZlyqFieldUtils.getColumnByField(f));
					otm.setFieldName(f.getName());

					Type type = f.getGenericType();

					if(type instanceof ParameterizedType){
						ParameterizedType pType = (ParameterizedType) f.getGenericType();
                        //如果类型参数为2则认为是LazyLoader 2013-7-25
                        if(pType.getActualTypeArguments().length==1){
						    Class<?> pClazz = (Class<?>)pType.getActualTypeArguments()[0];
						    if(pClazz!=null)
							    otm.setOneClass(pClazz);
                        }else{
                            Class<?> pClazz = (Class<?>)pType.getActualTypeArguments()[1];
                            if(pClazz!=null)
                                otm.setOneClass(pClazz);
                        }
					}else{
						throw new ZlyqDbExceptionZlyq("getOneToManyList Exception:"+f.getName()+"'s type is null");
					}
					/*修正类型赋值错误的bug，f.getClass返回的是Filed*/
					otm.setDataType(f.getType());
					otm.setSet(ZlyqFieldUtils.getFieldSetMethod(clazz, f));
					otm.setGet(ZlyqFieldUtils.getFieldGetMethod(clazz, f));
					
					oList.add(otm);
				}
			}
			return oList;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}	
	
	
}
