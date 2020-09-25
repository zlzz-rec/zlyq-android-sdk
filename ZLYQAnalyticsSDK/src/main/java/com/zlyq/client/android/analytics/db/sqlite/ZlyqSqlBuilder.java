package com.zlyq.client.android.analytics.db.sqlite;

import android.text.TextUtils;

import com.zlyq.client.android.analytics.db.exception.ZlyqDbExceptionZlyq;
import com.zlyq.client.android.analytics.db.table.Id;
import com.zlyq.client.android.analytics.db.table.ZlyqKeyValue;
import com.zlyq.client.android.analytics.db.table.ZlyqManyToOne;
import com.zlyq.client.android.analytics.db.table.ZlyqProperty;
import com.zlyq.client.android.analytics.db.table.ZlyqTableInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZlyqSqlBuilder {
	
	/**
	 * 获取插入的sql语句
	 * @return
	 */
	public static ZlyqSqlInfo buildInsertSql(Object entity){

		List<ZlyqKeyValue> zlyqKeyValueList = getSaveKeyValueListByEntity(entity);

		StringBuffer strSQL=new StringBuffer();
		ZlyqSqlInfo zlyqSqlInfo = null;
		if(zlyqKeyValueList !=null && zlyqKeyValueList.size()>0){

			zlyqSqlInfo = new ZlyqSqlInfo();

			strSQL.append("INSERT INTO ");
			strSQL.append(ZlyqTableInfo.get(entity.getClass()).getTableName());
			strSQL.append(" (");
			for(ZlyqKeyValue kv : zlyqKeyValueList){
				strSQL.append(kv.getKey()).append(",");
				zlyqSqlInfo.addValue(kv.getValue());
			}
			strSQL.deleteCharAt(strSQL.length() - 1);
			strSQL.append(") VALUES ( ");

			int length = zlyqKeyValueList.size();
			for(int i =0 ; i < length;i++){
				strSQL.append("?,");
			}
			strSQL.deleteCharAt(strSQL.length() - 1);
			strSQL.append(")");

			zlyqSqlInfo.setSql(strSQL.toString());
		}

		return zlyqSqlInfo;
	}

	public static List<ZlyqKeyValue> getSaveKeyValueListByEntity(Object entity){

		List<ZlyqKeyValue> zlyqKeyValueList = new ArrayList<ZlyqKeyValue>();

		ZlyqTableInfo table= ZlyqTableInfo.get(entity.getClass());
		Object idvalue = table.getId().getValue(entity);

		if(!(idvalue instanceof Integer)){ //用了非自增长,添加id , 采用自增长就不需要添加id了
			if(idvalue instanceof String && idvalue != null){
				ZlyqKeyValue kv = new ZlyqKeyValue(table.getId().getColumn(),idvalue);
				zlyqKeyValueList.add(kv);
			}
		}

		//添加属性
		Collection<ZlyqProperty> zlyqProperties = table.propertyMap.values();
		for(ZlyqProperty zlyqProperty : zlyqProperties){
			ZlyqKeyValue kv = property2KeyValue(zlyqProperty,entity) ;
			if(kv!=null)
				zlyqKeyValueList.add(kv);
		}

		//添加外键（多对一）
		Collection<ZlyqManyToOne> zlyqManyToOnes = table.manyToOneMap.values();
		for(ZlyqManyToOne many: zlyqManyToOnes){
			ZlyqKeyValue kv = manyToOne2KeyValue(many,entity);
			if(kv!=null) zlyqKeyValueList.add(kv);
		}

		return zlyqKeyValueList;
	}


	private static String getDeleteSqlBytableName(String tableName){
		return "DELETE FROM "+ tableName;
	}


	public static ZlyqSqlInfo buildDeleteSql(Object entity){
		ZlyqTableInfo table= ZlyqTableInfo.get(entity.getClass());

		Id id = table.getId();
		Object idvalue = id.getValue(entity);

		if(idvalue == null ){
			throw new ZlyqDbExceptionZlyq("getDeleteSQL:"+entity.getClass()+" id value is null");
		}
		StringBuffer strSQL = new StringBuffer(getDeleteSqlBytableName(table.getTableName()));
		strSQL.append(" WHERE ").append(id.getColumn()).append("=?");

		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		zlyqSqlInfo.setSql(strSQL.toString());
		zlyqSqlInfo.addValue(idvalue);

		return zlyqSqlInfo;
	}



	public static ZlyqSqlInfo buildDeleteSql(Class<?> clazz , Object idValue){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);
		Id id =table.getId();

		if(null == idValue) {
			throw new ZlyqDbExceptionZlyq("getDeleteSQL:idValue is null");
		}

		StringBuffer strSQL = new StringBuffer(getDeleteSqlBytableName(table.getTableName()));
		strSQL.append(" WHERE ").append(id.getColumn()).append("=?");

		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		zlyqSqlInfo.setSql(strSQL.toString());
		zlyqSqlInfo.addValue(idValue);

		return zlyqSqlInfo;
	}

	/**
	 * 根据条件删除数据 ，条件为空的时候将会删除所有的数据
	 * @param clazz
	 * @param strWhere
	 * @return
	 */
	public static String buildDeleteSql(Class<?> clazz , String strWhere){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);
		StringBuffer strSQL = new StringBuffer(getDeleteSqlBytableName(table.getTableName()));

		if(!TextUtils.isEmpty(strWhere)){
			strSQL.append(" WHERE ");
			strSQL.append(strWhere);
		}

		return strSQL.toString();
	}


	////////////////////////////select sql start///////////////////////////////////////


	private static String getSelectSqlByTableName(String tableName){
		return new StringBuffer("SELECT * FROM ").append(tableName).toString();
	}


	public static String getSelectSQL(Class<?> clazz, Object idValue){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);

		StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));
		strSQL.append(" WHERE ");
		strSQL.append(getPropertyStrSql(table.getId().getColumn(), idValue));

		return strSQL.toString();
	}

	public static ZlyqSqlInfo getSelectSqlAsSqlInfo(Class<?> clazz, Object idValue){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);

		StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));
		strSQL.append(" WHERE ").append(table.getId().getColumn()).append("=?");

		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		zlyqSqlInfo.setSql(strSQL.toString());
		zlyqSqlInfo.addValue(idValue);

		return zlyqSqlInfo;
	}



	public static ZlyqSqlInfo getSelectSqlByLimit(Class<?> clazz, int start, int end){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);

		StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));
		strSQL.append(" LIMIT ");
		strSQL.append(start);
		strSQL.append(",");

		strSQL.append(end);
		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		zlyqSqlInfo.setSql(strSQL.toString());
		return zlyqSqlInfo;
	}



	public static String  deleteSelectSqlByLimit(Class<?> clazz, int start,int end){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);
//delete from db limit 0,30
		StringBuffer strSQL = new StringBuffer(getDeleteSqlBytableName(table.getTableName()));
		strSQL.append(" LIMIT ");
		strSQL.append(start);
		strSQL.append(",");

		strSQL.append(end);
		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		zlyqSqlInfo.setSql(strSQL.toString());
		return strSQL.toString();
	}





	public static String getSelectSQL(Class<?> clazz){
		return getSelectSqlByTableName(ZlyqTableInfo.get(clazz).getTableName());
	}

	public static String getSelectSQLByWhere(Class<?> clazz, String strWhere){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);

		StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));

		if(!TextUtils.isEmpty(strWhere)){
			strSQL.append(" WHERE ").append(strWhere);
		}

		return strSQL.toString();
	}

	//////////////////////////////update sql start/////////////////////////////////////////////

	public static ZlyqSqlInfo getUpdateSqlAsSqlInfo(Object entity){

		ZlyqTableInfo table= ZlyqTableInfo.get(entity.getClass());
		Object idvalue=table.getId().getValue(entity);

		if(null == idvalue ) {//主键值不能为null，否则不能更新
			throw new ZlyqDbExceptionZlyq("this entity["+entity.getClass()+"]'s id value is null");
		}

		List<ZlyqKeyValue> zlyqKeyValueList = new ArrayList<ZlyqKeyValue>();
		//添加属性
		Collection<ZlyqProperty> zlyqProperties = table.propertyMap.values();
		for(ZlyqProperty zlyqProperty : zlyqProperties){
			ZlyqKeyValue kv = property2KeyValue(zlyqProperty,entity) ;
			if(kv!=null)
				zlyqKeyValueList.add(kv);
		}

		//添加外键（多对一）
		Collection<ZlyqManyToOne> zlyqManyToOnes = table.manyToOneMap.values();
		for(ZlyqManyToOne many: zlyqManyToOnes){
			ZlyqKeyValue kv = manyToOne2KeyValue(many,entity);
			if(kv!=null) zlyqKeyValueList.add(kv);
		}

		if(zlyqKeyValueList == null || zlyqKeyValueList.size()==0) return null ;

		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		StringBuffer strSQL=new StringBuffer("UPDATE ");
		strSQL.append(table.getTableName());
		strSQL.append(" SET ");
		for(ZlyqKeyValue kv : zlyqKeyValueList){
			strSQL.append(kv.getKey()).append("=?,");
			zlyqSqlInfo.addValue(kv.getValue());
		}
		strSQL.deleteCharAt(strSQL.length() - 1);
		strSQL.append(" WHERE ").append(table.getId().getColumn()).append("=?");
		zlyqSqlInfo.addValue(idvalue);
		zlyqSqlInfo.setSql(strSQL.toString());
		return zlyqSqlInfo;
	}




	public static ZlyqSqlInfo getUpdateSqlAsSqlInfo(Object entity, String strWhere){

		ZlyqTableInfo table= ZlyqTableInfo.get(entity.getClass());

		List<ZlyqKeyValue> zlyqKeyValueList = new ArrayList<ZlyqKeyValue>();

		//添加属性
		Collection<ZlyqProperty> zlyqProperties = table.propertyMap.values();
		for(ZlyqProperty zlyqProperty : zlyqProperties){
			ZlyqKeyValue kv = property2KeyValue(zlyqProperty,entity) ;
			if(kv!=null) zlyqKeyValueList.add(kv);
		}

		//添加外键（多对一）
		Collection<ZlyqManyToOne> zlyqManyToOnes = table.manyToOneMap.values();
		for(ZlyqManyToOne many: zlyqManyToOnes){
			ZlyqKeyValue kv = manyToOne2KeyValue(many,entity);
			if(kv!=null) zlyqKeyValueList.add(kv);
		}

		if(zlyqKeyValueList == null || zlyqKeyValueList.size()==0) {
			throw new ZlyqDbExceptionZlyq("this entity["+entity.getClass()+"] has no property");
		}

		ZlyqSqlInfo zlyqSqlInfo = new ZlyqSqlInfo();
		StringBuffer strSQL=new StringBuffer("UPDATE ");
		strSQL.append(table.getTableName());
		strSQL.append(" SET ");
		for(ZlyqKeyValue kv : zlyqKeyValueList){
			strSQL.append(kv.getKey()).append("=?,");
			zlyqSqlInfo.addValue(kv.getValue());
		}
		strSQL.deleteCharAt(strSQL.length() - 1);
		if(!TextUtils.isEmpty(strWhere)){
			strSQL.append(" WHERE ").append(strWhere);
		}
		zlyqSqlInfo.setSql(strSQL.toString());
		return zlyqSqlInfo;
	}
	
	
	
	public static String getCreatTableSQL(Class<?> clazz){
		ZlyqTableInfo table= ZlyqTableInfo.get(clazz);
		
		Id id =table.getId();
		StringBuffer strSQL = new StringBuffer();
		strSQL.append("CREATE TABLE IF NOT EXISTS ");
		strSQL.append(table.getTableName());
		strSQL.append(" ( ");
		
		Class<?> primaryClazz = id.getDataType();
		if( primaryClazz == int.class || primaryClazz==Integer.class
				|| primaryClazz == long.class || primaryClazz == Long.class){
			strSQL.append(id.getColumn()).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		}else{
			strSQL.append(id.getColumn()).append(" TEXT PRIMARY KEY,");
		}
			
		
		
		Collection<ZlyqProperty> zlyqProperties = table.propertyMap.values();
		for(ZlyqProperty zlyqProperty : zlyqProperties){
			strSQL.append(zlyqProperty.getColumn());
			Class<?> dataType =  zlyqProperty.getDataType();
			if( dataType== int.class || dataType == Integer.class
			   || dataType == long.class || dataType == Long.class){
				strSQL.append(" INTEGER");
			}else if(dataType == float.class ||dataType == Float.class
					||dataType == double.class || dataType == Double.class){
				strSQL.append(" REAL");
			}else if (dataType == boolean.class || dataType == Boolean.class) {
				strSQL.append(" NUMERIC");
			}
			strSQL.append(",");
		}
		
		Collection<ZlyqManyToOne> zlyqManyToOnes = table.manyToOneMap.values();
		for(ZlyqManyToOne zlyqManyToOne : zlyqManyToOnes){
			strSQL.append(zlyqManyToOne.getColumn())
			.append(" INTEGER")
			.append(",");
		}
		strSQL.deleteCharAt(strSQL.length() - 1);
		strSQL.append(" )");
		return strSQL.toString();
	}
	
	
	/**
	 * @param key
	 * @param value
	 * @return eg1: name='afinal'  eg2: id=100
	 */
	private static String getPropertyStrSql(String key, Object value){
		StringBuffer sbSQL = new StringBuffer(key).append("=");
		if(value instanceof String || value instanceof java.util.Date || value instanceof java.sql.Date){
			sbSQL.append("'").append(value).append("'");
		}else{
			sbSQL.append(value);
		}
		return sbSQL.toString();
	}
	
	
	
	private static ZlyqKeyValue property2KeyValue(ZlyqProperty zlyqProperty, Object entity){
		ZlyqKeyValue kv = null ;
		String pcolumn= zlyqProperty.getColumn();
		Object value = zlyqProperty.getValue(entity);
		if(value!=null){
			kv = new ZlyqKeyValue(pcolumn, value);
		}else{
			if(zlyqProperty.getDefaultValue()!=null && zlyqProperty.getDefaultValue().trim().length()!=0)
				kv = new ZlyqKeyValue(pcolumn, zlyqProperty.getDefaultValue());
		}
		return kv;
	}
	
	
	private static ZlyqKeyValue manyToOne2KeyValue(ZlyqManyToOne many , Object entity){
		ZlyqKeyValue kv = null ;
		String manycolumn=many.getColumn();
		Object manyobject=many.getValue(entity);
		if(manyobject!=null){
			Object manyvalue;
            if(manyobject.getClass()== ZlyqManyToOneLazyLoader.class){
                manyvalue = ZlyqTableInfo.get(many.getManyClass()).getId().getValue(((ZlyqManyToOneLazyLoader)manyobject).get());
            }else{
                manyvalue = ZlyqTableInfo.get(manyobject.getClass()).getId().getValue(manyobject);
            }
			if(manycolumn!=null && manyvalue!=null){
				kv = new ZlyqKeyValue(manycolumn, manyvalue);
			}
		}
		
		return kv;
	}
	
}
