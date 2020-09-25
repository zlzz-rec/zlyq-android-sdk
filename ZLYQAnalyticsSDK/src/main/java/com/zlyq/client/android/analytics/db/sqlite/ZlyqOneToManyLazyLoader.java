package com.zlyq.client.android.analytics.db.sqlite;

import com.zlyq.client.android.analytics.db.ZlyqFinalDb;

import java.util.ArrayList;
import java.util.List;

public class ZlyqOneToManyLazyLoader<O,M> {
    O ownerEntity;
    Class<O> ownerClazz;
    Class<M> listItemClazz;
    ZlyqFinalDb db;
    public ZlyqOneToManyLazyLoader(O ownerEntity, Class<O> ownerClazz, Class<M> listItemclazz, ZlyqFinalDb db){
        this.ownerEntity = ownerEntity;
        this.ownerClazz = ownerClazz;
        this.listItemClazz = listItemclazz;
        this.db = db;
    }
    List<M> entities;

    /**
     * 如果数据未加载，则调用loadOneToMany填充数据
     * @return
     */
    public List<M> getList(){
        if(entities==null){
            this.db.loadOneToMany((O)this.ownerEntity,this.ownerClazz,this.listItemClazz);
        }
        if(entities==null){
            entities =new ArrayList<M>();
        }
        return entities;
    }
    public void setList(List<M> value){
        entities = value;
    }

}
