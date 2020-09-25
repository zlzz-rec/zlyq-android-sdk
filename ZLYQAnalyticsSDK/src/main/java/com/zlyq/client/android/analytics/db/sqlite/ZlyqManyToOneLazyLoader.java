package com.zlyq.client.android.analytics.db.sqlite;

import com.zlyq.client.android.analytics.db.ZlyqFinalDb;

public class ZlyqManyToOneLazyLoader<M,O> {
    M manyEntity;
    Class<M> manyClazz;
    Class<O> oneClazz;
    ZlyqFinalDb db;
    /**
     * 用于
     */
    private Object fieldValue;
    public ZlyqManyToOneLazyLoader(M manyEntity, Class<M> manyClazz, Class<O> oneClazz, ZlyqFinalDb db){
        this.manyEntity = manyEntity;
        this.manyClazz = manyClazz;
        this.oneClazz = oneClazz;
        this.db = db;
    }
    O oneEntity;
    boolean hasLoaded = false;

    /**
     * 如果数据未加载，则调用loadManyToOne填充数据
     * @return
     */
    public O get(){
        if(oneEntity==null && !hasLoaded){
            this.db.loadManyToOne(null,this.manyEntity,this.manyClazz,this.oneClazz);
            hasLoaded = true;
        }
        return oneEntity;
    }
    public void set(O value){
        oneEntity = value;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }
}
