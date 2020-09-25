package com.zlyq.client.android.analytics.db.sqlite;

import android.database.Cursor;

import com.zlyq.client.android.analytics.db.ZlyqFinalDb;
import com.zlyq.client.android.analytics.db.table.ZlyqManyToOne;
import com.zlyq.client.android.analytics.db.table.ZlyqOneToMany;
import com.zlyq.client.android.analytics.db.table.ZlyqProperty;
import com.zlyq.client.android.analytics.db.table.ZlyqTableInfo;

import java.util.HashMap;
import java.util.Map.Entry;

public class ZlyqCursorUtils {

	public static <T> T getEntity(Cursor cursor, Class<T> clazz, ZlyqFinalDb db){
		try {
			if(cursor!=null ){
				ZlyqTableInfo table = ZlyqTableInfo.get(clazz);
				int columnCount = cursor.getColumnCount();
				if(columnCount>0){
					T  entity = (T) clazz.newInstance();
					for(int i=0;i<columnCount;i++){
						
						String column = cursor.getColumnName(i);
						
						ZlyqProperty zlyqProperty = table.propertyMap.get(column);
						if(zlyqProperty !=null){
							zlyqProperty.setValue(entity, cursor.getString(i));
						}else{
							if(table.getId().getColumn().equals(column)){
								table.getId().setValue(entity,  cursor.getString(i));
							}
						}

					}
                    /**
                     * 处理OneToMany的lazyLoad形式
                     */
                    for(ZlyqOneToMany zlyqOneToManyProp : table.oneToManyMap.values()){
                        if(zlyqOneToManyProp.getDataType()== ZlyqOneToManyLazyLoader.class){
                            ZlyqOneToManyLazyLoader zlyqOneToManyLazyLoader = new ZlyqOneToManyLazyLoader(entity,clazz, zlyqOneToManyProp.getOneClass(),db);
                            zlyqOneToManyProp.setValue(entity, zlyqOneToManyLazyLoader);
                        }
                    }

                    /**
                     * 处理ManyToOne的lazyLoad形式
                     */
                    for(ZlyqManyToOne zlyqManyToOneProp : table.manyToOneMap.values()){
                        if(zlyqManyToOneProp.getDataType()== ZlyqManyToOneLazyLoader.class){
                            ZlyqManyToOneLazyLoader zlyqManyToOneLazyLoader = new ZlyqManyToOneLazyLoader(entity,clazz, zlyqManyToOneProp.getManyClass(),db);
                            zlyqManyToOneLazyLoader.setFieldValue(cursor.getInt(cursor.getColumnIndex(zlyqManyToOneProp.getColumn())));
                            zlyqManyToOneProp.setValue(entity, zlyqManyToOneLazyLoader);
                        }
                    }
					return entity;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static ZlyqDbModel getDbModel(Cursor cursor){
		if(cursor!=null && cursor.getColumnCount() > 0){
			ZlyqDbModel model = new ZlyqDbModel();
			int columnCount = cursor.getColumnCount();
			for(int i=0;i<columnCount;i++){
				model.set(cursor.getColumnName(i), cursor.getString(i));
			}
			return model;
		}
		return null;
	}
	
	
	public static <T> T dbModel2Entity(ZlyqDbModel zlyqDbModel, Class<?> clazz){
		if(zlyqDbModel !=null){
			HashMap<String, Object> dataMap = zlyqDbModel.getDataMap();
			try {
				@SuppressWarnings("unchecked")
				T  entity = (T) clazz.newInstance();
				for(Entry<String, Object> entry : dataMap.entrySet()){
					String column = entry.getKey();
					ZlyqTableInfo table = ZlyqTableInfo.get(clazz);
					ZlyqProperty zlyqProperty = table.propertyMap.get(column);
					if(zlyqProperty !=null){
						zlyqProperty.setValue(entity, entry.getValue()==null?null:entry.getValue().toString());
					}else{
						if(table.getId().getColumn().equals(column)){
							table.getId().setValue(entity, entry.getValue()==null?null:entry.getValue().toString());
						}
					}
					
				}
				return entity;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
}
