package com.zlyq.client.android.analytics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zlyq.client.android.analytics.db.exception.ZlyqDbExceptionZlyq;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqCursorUtils;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqDbModel;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqManyToOneLazyLoader;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqOneToManyLazyLoader;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqSqlBuilder;
import com.zlyq.client.android.analytics.db.sqlite.ZlyqSqlInfo;
import com.zlyq.client.android.analytics.db.table.ZlyqKeyValue;
import com.zlyq.client.android.analytics.db.table.ZlyqManyToOne;
import com.zlyq.client.android.analytics.db.table.ZlyqOneToMany;
import com.zlyq.client.android.analytics.db.table.ZlyqTableInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ZlyqFinalDb {

    private static final String TAG = "ZlyqFinalDb";

    private static HashMap<String, ZlyqFinalDb> daoMap = new HashMap<String, ZlyqFinalDb>();

    private SQLiteDatabase db;
    private DaoConfig config;

    private ZlyqFinalDb(DaoConfig config) {
        if (config == null)
            throw new ZlyqDbExceptionZlyq("daoConfig is null");
        if (config.getContext() == null)
            throw new ZlyqDbExceptionZlyq("android context is null");
        if (config.getTargetDirectory() != null
                && config.getTargetDirectory().trim().length() > 0) {
            this.db = createDbFileOnSDCard(config.getTargetDirectory(),
                    config.getDbName());
        } else {
            this.db = new SqliteDbHelper(config.getContext()
                    .getApplicationContext(), config.getDbName(),
                    config.getDbVersion(), config.getDbUpdateListener())
                    .getWritableDatabase();
        }
        this.config = config;
    }

    private synchronized static ZlyqFinalDb getInstance(DaoConfig daoConfig) {
        ZlyqFinalDb dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new ZlyqFinalDb(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        }
        return dao;
    }

    /**
     * 创建FinalDb
     *
     * @param context
     */
    public static ZlyqFinalDb create(Context context) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        return create(config);
    }

    /**
     * 创建FinalDb
     *
     * @param context
     * @param isDebug
     *            是否是debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static ZlyqFinalDb create(Context context, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDebug(isDebug);
        return create(config);

    }

    /**
     * 创建FinalDb
     *
     * @param context
     * @param dbName
     *            数据库名称
     */
    public static ZlyqFinalDb create(Context context, String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        return create(config);
    }

    /**
     * 创建 ZlyqFinalDb
     *
     * @param context
     * @param dbName
     *            数据库名称
     * @param isDebug
     *            是否为debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static ZlyqFinalDb create(Context context, String dbName, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        return create(config);
    }

    /**
     * 创建FinalDb
     *
     * @param context
     * @param dbName
     *            数据库名称
     */
    public static ZlyqFinalDb create(Context context, String targetDirectory,
                                     String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setTargetDirectory(targetDirectory);
        return create(config);
    }

    /**
     * 创建 ZlyqFinalDb
     *
     * @param context
     * @param dbName
     *            数据库名称
     * @param isDebug
     *            是否为debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static ZlyqFinalDb create(Context context, String targetDirectory,
                                     String dbName, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setTargetDirectory(targetDirectory);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        return create(config);
    }

    /**
     * 创建 ZlyqFinalDb
     *
     * @param context
     *            上下文
     * @param dbName
     *            数据库名字
     * @param isDebug
     *            是否是调试模式：调试模式会log出sql信息
     * @param dbVersion
     *            数据库版本信息
     * @param dbUpdateListener
     *            数据库升级监听器：如果监听器为null，升级的时候将会清空所所有的数据
     * @return
     */
    public static ZlyqFinalDb create(Context context, String dbName,
                                     boolean isDebug, int dbVersion, DbUpdateListener dbUpdateListener) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(dbUpdateListener);
        return create(config);
    }

    /**
     *
     * @param context
     *            上下文
     * @param targetDirectory
     *            db文件路径，可以配置为sdcard的路径
     * @param dbName
     *            数据库名字
     * @param isDebug
     *            是否是调试模式：调试模式会log出sql信息
     * @param dbVersion
     *            数据库版本信息
     * @param dbUpdateListener 数据库升级监听器
     *            ：如果监听器为null，升级的时候将会清空所所有的数据
     * @return
     */
    public static ZlyqFinalDb create(Context context, String targetDirectory,
                                     String dbName, boolean isDebug, int dbVersion,
                                     DbUpdateListener dbUpdateListener) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setTargetDirectory(targetDirectory);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(dbUpdateListener);
        return create(config);
    }

    /**
     * 创建FinalDb
     *
     * @param daoConfig
     * @return
     */
    public static ZlyqFinalDb create(DaoConfig daoConfig) {
        return getInstance(daoConfig);
    }

    /**
     * 保存数据库，速度要比save快
     *
     * @param entity
     */
    public void save(Object entity) {
        checkTableExist(entity.getClass());
        exeSqlInfo(ZlyqSqlBuilder.buildInsertSql(entity));
    }

    /**
     * 保存数据到数据库<br />
     * <b>注意：</b><br />
     * 保存成功后，entity的主键将被赋值（或更新）为数据库的主键， 只针对自增长的id有效
     *
     * @param entity
     *            要保存的数据
     * @return ture： 保存成功 false:保存失败
     */
    public boolean saveBindId(Object entity) {
        checkTableExist(entity.getClass());
        List<ZlyqKeyValue> entityKvList = ZlyqSqlBuilder
                .getSaveKeyValueListByEntity(entity);
        if (entityKvList != null && entityKvList.size() > 0) {
            ZlyqTableInfo tf = ZlyqTableInfo.get(entity.getClass());
            ContentValues cv = new ContentValues();
            insertContentValues(entityKvList, cv);
            Long id = db.insert(tf.getTableName(), null, cv);
            if (id == -1)
                return false;
            tf.getId().setValue(entity, id);
            return true;
        }
        return false;
    }

    /**
     * 把List<ZlyqKeyValue>数据存储到ContentValues
     *
     * @param list
     * @param cv
     */
    private void insertContentValues(List<ZlyqKeyValue> list, ContentValues cv) {
        if (list != null && cv != null) {
            for (ZlyqKeyValue kv : list) {
                cv.put(kv.getKey(), kv.getValue().toString());
            }
        } else {
            Log.w(TAG,
                    "insertContentValues: List<ZlyqKeyValue> is empty or ContentValues is empty!");
        }

    }

    /**
     * 更新数据 （主键ID必须不能为空）
     *
     * @param entity
     */
    public void update(Object entity) {
        checkTableExist(entity.getClass());
        exeSqlInfo(ZlyqSqlBuilder.getUpdateSqlAsSqlInfo(entity));
    }

    /**
     * 根据条件更新数据
     *
     * @param entity
     * @param strWhere
     *            条件为空的时候，将会更新所有的数据
     */
    public void update(Object entity, String strWhere) {
        checkTableExist(entity.getClass());
        exeSqlInfo(ZlyqSqlBuilder.getUpdateSqlAsSqlInfo(entity, strWhere));
    }

    /**
     * 删除数据
     *
     * @param entity
     *            entity的主键不能为空
     */
    public void delete(Object entity) {
        checkTableExist(entity.getClass());
        exeSqlInfo(ZlyqSqlBuilder.buildDeleteSql(entity));
    }

    /**
     * 根据主键删除数据
     *
     * @param clazz
     *            要删除的实体类
     * @param id
     *            主键值
     */
    public void deleteById(Class<?> clazz, Object id) {
        checkTableExist(clazz);
        exeSqlInfo(ZlyqSqlBuilder.buildDeleteSql(clazz, id));
    }

    /**
     * 根据条件删除数据
     *
     * @param clazz
     * @param strWhere
     *            条件为空的时候 将会删除所有的数据
     */
    public void deleteByWhere(Class<?> clazz, String strWhere) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.buildDeleteSql(clazz, strWhere);
        debugSql(sql);
        db.execSQL(sql);
    }

    /**
     * 删除表的所有数据
     *
     * @param clazz
     */
    public void deleteAll(Class<?> clazz) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.buildDeleteSql(clazz, null);
        debugSql(sql);
        db.execSQL(sql);
    }

    /**
     * 删除指定的表
     *
     * @param clazz
     */
    public void dropTable(Class<?> clazz) {
        checkTableExist(clazz);
        ZlyqTableInfo table = ZlyqTableInfo.get(clazz);
        String sql = "DROP TABLE " + table.getTableName();
        debugSql(sql);
        db.execSQL(sql);
        table.setCheckDatabese(false);
    }

    /**
     * 删除所有数据表
     */
    public void dropDb() {
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
    }

    /**
     * 在指定的表添加属性
     *
     * @param clazz
     */
    public void alterTable(Class<?> clazz, String colum) {
        checkTableExist(clazz);
        ZlyqTableInfo table = ZlyqTableInfo.get(clazz);
        String sql = "ALTER TABLE " + table.getTableName() + " ADD COLUMN " + colum;
        debugSql(sql);
        db.execSQL(sql);
        table.setCheckDatabese(false);
    }

    public boolean tableIsExist(Class<?> clazz) {
        return tableIsExist(ZlyqTableInfo.get(clazz));
    }

    public void exeSqlInfo(ZlyqSqlInfo zlyqSqlInfo) {
        if (zlyqSqlInfo != null) {
            debugSql(zlyqSqlInfo.getSql());
            db.execSQL(zlyqSqlInfo.getSql(), zlyqSqlInfo.getBindArgsAsArray());
        } else {
            Log.e(TAG, "sava error:zlyqSqlInfo is null");
        }
    }

    /**
     * 根据主键查找数据（默认不查询多对一或者一对多的关联数据）
     *
     * @param id
     * @param clazz
     */
    public <T> T findById(Object id, Class<T> clazz) {
        checkTableExist(clazz);
        ZlyqSqlInfo zlyqSqlInfo = ZlyqSqlBuilder.getSelectSqlAsSqlInfo(clazz, id);
        if (zlyqSqlInfo != null) {
            debugSql(zlyqSqlInfo.getSql());
            Cursor cursor = db.rawQuery(zlyqSqlInfo.getSql(),
                    zlyqSqlInfo.getBindArgsAsStringArray());
            try {
                if (cursor.moveToNext()) {
                    return ZlyqCursorUtils.getEntity(cursor, clazz, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 取前xx条数据
     *
     * @param clazz
     */
    public <T> List<T> findAllByLimit(Class<T> clazz,int start ,int end) {
        checkTableExist(clazz);
        ZlyqSqlInfo zlyqSqlInfo = ZlyqSqlBuilder.getSelectSqlByLimit(clazz, start,end);
        return findAllBySql(clazz, zlyqSqlInfo.getSql());
    }

    /**
     * 删除 前xx条数据
     *
     * @param id
     * @param clazz
     */
    public <T> T deleteByLimit( Class<T> clazz,int start ,int end) {
        checkTableExist(clazz);
        String  sqlInfo = ZlyqSqlBuilder.deleteSelectSqlByLimit(clazz, start,end);
        db.execSQL(sqlInfo);
        return null;
    }




    /**
     * 根据主键查找，同时查找“多对一”的数据（如果有多个“多对一”属性，则查找所有的“多对一”属性）
     *
     * @param id
     * @param clazz
     */
    public <T> T findWithManyToOneById(Object id, Class<T> clazz) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.getSelectSQL(clazz, id);
        debugSql(sql);
        ZlyqDbModel zlyqDbModel = findDbModelBySQL(sql);
        if (zlyqDbModel != null) {
            T entity = ZlyqCursorUtils.dbModel2Entity(zlyqDbModel, clazz);
            return loadManyToOne(zlyqDbModel, entity, clazz);
        }

        return null;
    }

    /**
     * 根据条件查找，同时查找“多对一”的数据（只查找findClass中的类的数据）
     *
     * @param id
     * @param clazz
     * @param findClass
     *            要查找的类
     */
    public <T> T findWithManyToOneById(Object id, Class<T> clazz,
                                       Class<?>... findClass) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.getSelectSQL(clazz, id);
        debugSql(sql);
        ZlyqDbModel zlyqDbModel = findDbModelBySQL(sql);
        if (zlyqDbModel != null) {
            T entity = ZlyqCursorUtils.dbModel2Entity(zlyqDbModel, clazz);
            return loadManyToOne(zlyqDbModel, entity, clazz, findClass);
        }
        return null;
    }

    /**
     * 获取第一条数据
     *
     * @param clazz
     *            要查找的类
     */
    public <T> T findFirst(Class<T> clazz) {
        checkTableExist(clazz);
        ZlyqTableInfo table= ZlyqTableInfo.get(clazz);
        String sql = "select * from " + table.getTableName() + " limit 1";
        debugSql(sql);
        ZlyqDbModel zlyqDbModel = findDbModelBySQL(sql);
        if (zlyqDbModel != null) {
            return ZlyqCursorUtils.dbModel2Entity(zlyqDbModel, clazz);
        }
        return null;
    }

    /**
     * 将entity中的“多对一”的数据填充满 如果是懒加载填充，则dbModel参数可为null
     *
     * @param clazz
     * @param entity
     * @param <T>
     * @return
     */
    public <T> T loadManyToOne(ZlyqDbModel zlyqDbModel, T entity, Class<T> clazz,
                               Class<?>... findClass) {
        if (entity != null) {
            try {
                Collection<ZlyqManyToOne> manys = ZlyqTableInfo.get(clazz).manyToOneMap
                        .values();
                for (ZlyqManyToOne many : manys) {

                    Object id = null;
                    if (zlyqDbModel != null) {
                        id = zlyqDbModel.get(many.getColumn());
                    } else if (many.getValue(entity).getClass() == ZlyqManyToOneLazyLoader.class
                            && many.getValue(entity) != null) {
                        id = ((ZlyqManyToOneLazyLoader) many.getValue(entity))
                                .getFieldValue();
                    }

                    if (id != null) {
                        boolean isFind = false;
                        if (findClass == null || findClass.length == 0) {
                            isFind = true;
                        }
                        for (Class<?> mClass : findClass) {
                            if (many.getManyClass() == mClass) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {

                            @SuppressWarnings("unchecked")
                            T manyEntity = (T) findById(
                                    Integer.valueOf(id.toString()),
                                    many.getManyClass());
                            if (manyEntity != null) {
                                if (many.getValue(entity).getClass() == ZlyqManyToOneLazyLoader.class) {
                                    if (many.getValue(entity) == null) {
                                        many.setValue(
                                                entity,
                                                new ZlyqManyToOneLazyLoader(entity,
                                                        clazz,
                                                        many.getManyClass(),
                                                        this));
                                    }
                                    ((ZlyqManyToOneLazyLoader) many
                                            .getValue(entity)).set(manyEntity);
                                } else {
                                    many.setValue(entity, manyEntity);
                                }

                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    /**
     * 根据主键查找，同时查找“一对多”的数据（如果有多个“一对多”属性，则查找所有的一对多”属性）
     *
     * @param id
     * @param clazz
     */
    public <T> T findWithOneToManyById(Object id, Class<T> clazz) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.getSelectSQL(clazz, id);
        debugSql(sql);
        ZlyqDbModel zlyqDbModel = findDbModelBySQL(sql);
        if (zlyqDbModel != null) {
            T entity = ZlyqCursorUtils.dbModel2Entity(zlyqDbModel, clazz);
            return loadOneToMany(entity, clazz);
        }

        return null;
    }

    /**
     * 根据主键查找，同时查找“一对多”的数据（只查找findClass中的“一对多”）
     *
     * @param id
     * @param clazz
     * @param findClass
     */
    public <T> T findWithOneToManyById(Object id, Class<T> clazz,
                                       Class<?>... findClass) {
        checkTableExist(clazz);
        String sql = ZlyqSqlBuilder.getSelectSQL(clazz, id);
        debugSql(sql);
        ZlyqDbModel zlyqDbModel = findDbModelBySQL(sql);
        if (zlyqDbModel != null) {
            T entity = ZlyqCursorUtils.dbModel2Entity(zlyqDbModel, clazz);
            return loadOneToMany(entity, clazz, findClass);
        }

        return null;
    }

    /**
     * 将entity中的“一对多”的数据填充满
     *
     * @param entity
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T loadOneToMany(T entity, Class<T> clazz, Class<?>... findClass) {
        if (entity != null) {
            try {
                Collection<ZlyqOneToMany> ones = ZlyqTableInfo.get(clazz).oneToManyMap
                        .values();
                Object id = ZlyqTableInfo.get(clazz).getId().getValue(entity);
                for (ZlyqOneToMany one : ones) {
                    boolean isFind = false;
                    if (findClass == null || findClass.length == 0) {
                        isFind = true;
                    }
                    for (Class<?> mClass : findClass) {
                        if (one.getOneClass() == mClass) {
                            isFind = true;
                            break;
                        }
                    }

                    if (isFind) {
                        List<?> list = findAllByWhere(one.getOneClass(),
                                one.getColumn() + "=" + id);
                        if (list != null) {
                            /* 如果是OneToManyLazyLoader泛型，则执行灌入懒加载数据 */
                            if (one.getDataType() == ZlyqOneToManyLazyLoader.class) {
                                ZlyqOneToManyLazyLoader zlyqOneToManyLazyLoader = one
                                        .getValue(entity);
                                zlyqOneToManyLazyLoader.setList(list);
                            } else {
                                one.setValue(entity, list);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    /**
     * 查找所有的数据
     *
     * @param clazz
     */
    public <T> List<T> findAll(Class<T> clazz) {
        checkTableExist(clazz);
        return findAllBySql(clazz, ZlyqSqlBuilder.getSelectSQL(clazz));
    }

    /**
     * 查找所有数据
     *
     * @param clazz
     * @param orderBy
     *            排序的字段
     */
    public <T> List<T> findAll(Class<T> clazz, String orderBy) {
		checkTableExist(clazz);
        return findAllBySql(clazz, ZlyqSqlBuilder.getSelectSQL(clazz)
                + " ORDER BY " + orderBy);
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strWhere
     *            条件为空的时候查找所有数据
     */
    public <T> List<T> findAllByWhere(Class<T> clazz, String strWhere) {
        checkTableExist(clazz);
        return findAllBySql(clazz,
                ZlyqSqlBuilder.getSelectSQLByWhere(clazz, strWhere));
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strWhere
     *            条件为空的时候查找所有数据
     * @param orderBy
     *            排序字段
     */
    public <T> List<T> findAllByWhere(Class<T> clazz, String strWhere,
                                      String orderBy) {
        checkTableExist(clazz);
        return findAllBySql(clazz,
                ZlyqSqlBuilder.getSelectSQLByWhere(clazz, strWhere) + " ORDER BY "
                        + orderBy);
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strSQL
     */
    private <T> List<T> findAllBySql(Class<T> clazz, String strSQL) {
        checkTableExist(clazz);
        debugSql(strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        try {
            List<T> list = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T t = ZlyqCursorUtils.getEntity(cursor, clazz, this);
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return null;
    }

    public <T> int getRowCount(Class<T> clazz) {
        checkTableExist(clazz);
        String strSQL = ZlyqSqlBuilder.getSelectSQL(clazz);
        debugSql(strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public <T> String getExpiredId(Class<T> clazz, String idName, int maxCount) {
        checkTableExist(clazz);
        String strSQL = ZlyqSqlBuilder.getSelectSQL(clazz);
        debugSql(strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        String id = null;
        if (cursor.getCount() >= maxCount) {
            if (cursor.moveToFirst()) {
                id = cursor.getString(cursor.getColumnIndex(idName));
            }
        }
        cursor.close();
        return id;
    }

    /**
     * 根据sql语句查找数据，这个一般用于数据统计
     *
     * @param strSQL
     */
    public ZlyqDbModel findDbModelBySQL(String strSQL) {
        debugSql(strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        try {
            if (cursor.moveToNext()) {
                return ZlyqCursorUtils.getDbModel(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    public List<ZlyqDbModel> findDbModelListBySQL(String strSQL) {
        debugSql(strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        List<ZlyqDbModel> zlyqDbModelList = new ArrayList<ZlyqDbModel>();
        try {
            while (cursor.moveToNext()) {
                zlyqDbModelList.add(ZlyqCursorUtils.getDbModel(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return zlyqDbModelList;
    }

    public void checkTableExist(Class<?> clazz) {
        if (!tableIsExist(ZlyqTableInfo.get(clazz))) {
            String sql = ZlyqSqlBuilder.getCreatTableSQL(clazz);
            debugSql(sql);
            db.execSQL(sql);
        }
    }

    private boolean tableIsExist(ZlyqTableInfo table) {
        if (table.isCheckDatabese())
            return true;

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.getTableName() + "' ";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabese(true);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return false;
    }

    private void debugSql(String sql) {
        if (config != null && config.isDebug())
            Log.d("Debug SQL", ">>>>>>  " + sql);
    }

    public static class DaoConfig {
        private Context mContext = null; // android上下文
        private String mDbName = "afinal.db"; // 数据库名字
        private int dbVersion = 1; // 数据库版本
        private boolean debug = true; // 是否是调试模式（调试模式 增删改查的时候显示SQL语句）
        private DbUpdateListener dbUpdateListener;
        // private boolean saveOnSDCard = false;//是否保存到SD卡
        private String targetDirectory;// 数据库文件在sd卡中的目录

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public String getDbName() {
            return mDbName;
        }

        public void setDbName(String dbName) {
            this.mDbName = dbName;
        }

        public int getDbVersion() {
            return dbVersion;
        }

        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public DbUpdateListener getDbUpdateListener() {
            return dbUpdateListener;
        }

        public void setDbUpdateListener(DbUpdateListener dbUpdateListener) {
            this.dbUpdateListener = dbUpdateListener;
        }

        // public boolean isSaveOnSDCard() {
        // return saveOnSDCard;
        // }
        //
        // public void setSaveOnSDCard(boolean saveOnSDCard) {
        // this.saveOnSDCard = saveOnSDCard;
        // }

        public String getTargetDirectory() {
            return targetDirectory;
        }

        public void setTargetDirectory(String targetDirectory) {
            this.targetDirectory = targetDirectory;
        }
    }

    /**
     * 在SD卡的指定目录上创建文件
     *
     * @param sdcardPath
     * @param dbfilename
     * @return
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath,
                                                String dbfilename) {
        File dbf = new File(sdcardPath, dbfilename);
        if (!dbf.exists()) {
            try {
                if (dbf.createNewFile()) {
                    return SQLiteDatabase.openOrCreateDatabase(dbf, null);
                }
            } catch (IOException ioex) {
                throw new ZlyqDbExceptionZlyq("数据库文件创建失败", ioex);
            }
        } else {
            return SQLiteDatabase.openOrCreateDatabase(dbf, null);
        }

        return null;
    }

    class SqliteDbHelper extends SQLiteOpenHelper {

        private DbUpdateListener mDbUpdateListener;

        public SqliteDbHelper(Context context, String name, int version,
                              DbUpdateListener dbUpdateListener) {
            super(context, name, null, version);
            this.mDbUpdateListener = dbUpdateListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (mDbUpdateListener != null) {
                mDbUpdateListener.onUpgrade(db, oldVersion, newVersion);
            } else { // 清空所有的数据信息
                dropDb();
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            dropDb();
        }


    }

    public interface DbUpdateListener {
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

}
