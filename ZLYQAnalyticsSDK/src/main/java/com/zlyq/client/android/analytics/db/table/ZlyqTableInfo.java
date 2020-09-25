package com.zlyq.client.android.analytics.db.table;

import com.zlyq.client.android.analytics.db.exception.ZlyqDbExceptionZlyq;
import com.zlyq.client.android.analytics.db.utils.ZlyqClassUtils;
import com.zlyq.client.android.analytics.db.utils.ZlyqFieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class ZlyqTableInfo {

	private String className;
	private String tableName;
	
	private Id id;
	
	public final HashMap<String, ZlyqProperty> propertyMap = new HashMap<String, ZlyqProperty>();
	public final HashMap<String, ZlyqOneToMany> oneToManyMap = new HashMap<String, ZlyqOneToMany>();
	public final HashMap<String, ZlyqManyToOne> manyToOneMap = new HashMap<String, ZlyqManyToOne>();
	
	private boolean checkDatabese;//在对实体进行数据库操作的时候查询是否已经有表了，只需查询一遍，用此标示
	
	
	private static final HashMap<String, ZlyqTableInfo> tableInfoMap = new HashMap<String, ZlyqTableInfo>();
	
	private ZlyqTableInfo(){}
	
	@SuppressWarnings("unused")
	public static ZlyqTableInfo get(Class<?> clazz){
		if(clazz == null) 
			throw new ZlyqDbExceptionZlyq("table info get error,because the clazz is null");
		
		ZlyqTableInfo zlyqTableInfo = tableInfoMap.get(clazz.getName());
		if( zlyqTableInfo == null ){
			zlyqTableInfo = new ZlyqTableInfo();
			
			zlyqTableInfo.setTableName(ZlyqClassUtils.getTableName(clazz));
			zlyqTableInfo.setClassName(clazz.getName());
			
			Field idField = ZlyqClassUtils.getPrimaryKeyField(clazz);
			if(idField != null){
				Id id = new Id();
				id.setColumn(ZlyqFieldUtils.getColumnByField(idField));
				id.setFieldName(idField.getName());
				id.setSet(ZlyqFieldUtils.getFieldSetMethod(clazz, idField));
				id.setGet(ZlyqFieldUtils.getFieldGetMethod(clazz, idField));
				id.setDataType(idField.getType());
				
				zlyqTableInfo.setId(id);
			}else{
				throw new ZlyqDbExceptionZlyq("the class["+clazz+"]'s idField is null , \n you can define _id,id property or use annotation @id to solution this exception");
			}
			
			List<ZlyqProperty> pList = ZlyqClassUtils.getPropertyList(clazz);
			if(pList!=null){
				for(ZlyqProperty p : pList){
					if(p!=null)
						zlyqTableInfo.propertyMap.put(p.getColumn(), p);
				}
			}
			
			List<ZlyqManyToOne> mList = ZlyqClassUtils.getManyToOneList(clazz);
			if(mList!=null){
				for(ZlyqManyToOne m : mList){
					if(m!=null)
						zlyqTableInfo.manyToOneMap.put(m.getColumn(), m);
				}
			}
			
			List<ZlyqOneToMany> oList = ZlyqClassUtils.getOneToManyList(clazz);
			if(oList!=null){
				for(ZlyqOneToMany o : oList){
					if(o!=null)
						zlyqTableInfo.oneToManyMap.put(o.getColumn(), o);
				}
			}
			
			
			tableInfoMap.put(clazz.getName(), zlyqTableInfo);
		}
		
		if(zlyqTableInfo == null )
			throw new ZlyqDbExceptionZlyq("the class["+clazz+"]'s table is null");
		
		return zlyqTableInfo;
	}
	
	
	public static ZlyqTableInfo get(String className){
		try {
			return get(Class.forName(className));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public boolean isCheckDatabese() {
		return checkDatabese;
	}

	public void setCheckDatabese(boolean checkDatabese) {
		this.checkDatabese = checkDatabese;
	}

	
	
}
