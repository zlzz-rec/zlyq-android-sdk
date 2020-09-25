package com.zlyq.client.android.analytics;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.db.ZlyqFinalDb;

import java.util.List;

 public class ZlyqDBHelper {

    private static final ZlyqFinalDb db;

   // SQLite 采用串行模型，所有线程都公用同一个数据库连接。
    static {
        db = ZlyqFinalDb.create(ZADataManager.getContext(), ZADataManager.getDistinctId().get()+ ZlyqConstant.DB_NAME, false, ZlyqConstant.DB_VERSION,
                new ZlyqFinalDb.DbUpdateListener() {
                    @Override
                    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                        Cursor cursor = db.rawQuery(
                                "SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'", null);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                db.execSQL("DROP TABLE " + cursor.getString(0));
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                        ZlyqLogger.logWrite(ZlyqConstant.TAG, "onUpgrade ,delete DB_NAME success!-->");

                    }
                });
    }

    /**
     *  检索 该时间节点之前的数据
     * @param formateDate
     * @return
     */
    public static synchronized List<EventBean> getEventListByDate(String formateDate) {
        //select * from shopping.tb_item where it < '2015-03-08 21:28:44';
        List<EventBean> resultList = null;
        try {
            resultList = db.findAllByWhere(EventBean.class, " event_time<\"" + formateDate + "\"");
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByDate  success!-->"+formateDate+"--resultList.size()--"+resultList.size());
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByDate  failed-->"+e.getMessage());
        }
        return resultList;
    }

    /**
     *  检索所有数据
     * @return
     */
    public static synchronized List<EventBean> getEventList() {
        //select * from shopping.tb_item where it < '2015-03-08 21:28:44';
        List<EventBean> resultList = null;
        try {
            resultList = db.findAll(EventBean.class);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByDate  success!-->--resultList.size()--"+resultList.size());
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByDate  failed-->"+e.getMessage());
        }
        return resultList;
    }

    /**
     * 获取 检索记录行 start-end 之间的数据
     * @return
     */
    public static synchronized List<EventBean> getEventListByLimit(int  start ,int end){
        //mysql> SELECT * FROM table LIMIT 5,10;  // 检索记录行 6-15
        //mysql> SELECT * FROM table LIMIT 95,-1; // 检索记录行 96-last.
        //mysql> SELECT * FROM table LIMIT 5;     //检索前 5 个记录行
        List<EventBean> resultList = null;
        try {
            resultList = db.findAllByLimit(EventBean.class, start,end);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByLimit  success!-->第"+start+"条到"+end+"之间--resultList.size()--"+resultList.size());
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventListByLimit  failed-->"+e.getMessage());
        }
        return resultList;
    }

    /**
     * 获取 检索记录行 start-end 之间的数据
     * @return
     */
    public static synchronized void deleteEventListByLimit(int start ,int end){
        List<EventBean> resultList = null;
        try {
             db.deleteByLimit(EventBean.class, start,end);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByLimit  success!-->第"+start+"条到"+end+"之间--resultList.size()--"+resultList.size());
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByLimit  failed-->"+e.getMessage());
        }
    }

    /**
     *  检索 数据库的条数
     * @return
     */
    public static synchronized int  getEventRowCount() {
        //select * from shopping.tb_item where it < '2015-03-08 21:28:44';
        int resultCount = 0;
        try {
            resultCount = db.getRowCount(EventBean.class);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventRowCount  success!-->"+resultCount);

        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "getEventRowCount  failed-->"+e.getMessage());
        }
        return resultCount;
    }

    /**
     * 删除该时间节点之前的数据
     * @param formateDate
     */
    public static synchronized void deleteEventListByDate(String formateDate) {
        //select * from shopping.tb_item where it < '2015-03-08 21:28:44';
        try {
            db.deleteByWhere(EventBean.class, " event_time<\"" + formateDate + "\"");
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByDate  success!-->"+formateDate);
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByDate  failed-->"+e.getMessage());
        }
    }

    /**
     * 删除数据
     */
    public static synchronized void deleteEventList() {
        try {
            db.deleteAll(EventBean.class);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByDate  success!-->");
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "deleteEventListByDate  failed-->"+e.getMessage());
        }
    }

    /**
     * 向数据库中添加一条记录
     *
     * @param data
     */
    public static synchronized boolean addEventData(EventBean data) {
        try {
            // 如果该条数据存在于数据库中 删掉原来的记录 添加新纪录 保持在最新浏览位置
            db.save(data);
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "save to db success-->"+data.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logError(ZlyqConstant.TAG, "save to db failed-->"+e.getMessage());
            if (e.getMessage().contains("no column named")){
                db.dropTable(EventBean.class);
                //table eventlist has no column named ds
                ZlyqLogger.logWrite(ZlyqConstant.TAG, "has no column named,so dropTable");
                db.save(data);
                ZlyqLogger.logWrite(ZlyqConstant.TAG, "reload : save to db success-->");
            }
            return false;
        }
    }

}
