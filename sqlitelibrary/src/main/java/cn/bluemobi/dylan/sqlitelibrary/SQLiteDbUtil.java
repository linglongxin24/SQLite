package cn.bluemobi.dylan.sqlitelibrary;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite封装工具类
 * Created by yuandl on 2016-11-21.
 */

public class SQLiteDbUtil {
    /**
     * 支持的表字段数据类型包括：基本类型、包装类型、String类型、Date类型
     */
    String[] types = {"java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.String",
            "java.util.Date",
            "int", "double", "long", "short", "byte", "boolean", "char", "float"};

    /**
     * SQLite工具类对象
     */
    private static volatile SQLiteDbUtil sqLiteDbUtil;
    /**
     * 打印日志的TAG，用来调试
     */
    private final String TAG = "SQLiteDbUtil";

    /**
     * SQLiteDateBase对象
     */
    private SQLiteDatabase sqLiteDatabase;
    /**
     * 上下文
     */
    private Context context;
    /**
     * 数据库的路径
     */
    private String path;
    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 构造函数私有化，单例
     */
    private SQLiteDbUtil() {

    }

    /**
     * 单例
     *
     * @return 数据库管理工具类对象
     */
    public static SQLiteDbUtil getSQLiteDbUtil() {
        if (sqLiteDbUtil == null) {
            synchronized (SQLiteDbUtil.class) {
                if (sqLiteDbUtil == null) {
                    sqLiteDbUtil = new SQLiteDbUtil();
                }
            }
        }
        return sqLiteDbUtil;
    }

    /**
     * 创建或打开数据库连接
     * 默认创建在"/data/data/cn.bluemobi.dylan.sqlite/databases/mydb.db";
     *
     * @param context 上下文
     */
    public void openOrCreateDataBase(Context context) {
        openOrCreateDataBase(context, null, null);
    }

    /**
     * 创建或打开数据库连接 重载
     * 默认创建在"/data/data/cn.bluemobi.dylan.sqlite/databases/mydb.db";
     *
     * @param context      上下文
     * @param databaseName 数据库名称
     */
    public void openOrCreateDataBase(Context context, String databaseName) {
        openOrCreateDataBase(context, null, databaseName);
    }

    /**
     * 创建或打开数据库连接 重载
     * 默认创建在"/data/data/cn.bluemobi.dylan.sqlite/databases/mydb.db";
     *
     * @param context      上下文
     * @param path         数据库路径
     * @param databaseName 数据库表名
     */
    public void openOrCreateDataBase(Context context, String path, String databaseName) {
        this.context = context;
        this.path = path;
        this.databaseName = databaseName;
        if (TextUtils.isEmpty(databaseName)) {
            databaseName = "mydb.db";
        }
        if (TextUtils.isEmpty(path)) {
            sqLiteDatabase = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        } else {
            File dataBaseFile = new File(path, databaseName);
            if (!dataBaseFile.getParentFile().exists()) {
                dataBaseFile.mkdirs();
            }
            sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dataBaseFile, null);
        }
    }


    /**
     * 获取数据库操作对象
     *
     * @return 数据库操作对象 SQLiteDatabase
     */
    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    /**
     * 打开数据库连接
     *
     * @throws Exception 数据库为初始化异常
     */
    private void open() throws Exception {
        if (sqLiteDatabase != null) {
            this.openOrCreateDataBase(context, path, databaseName);
        } else {
            throw new Exception("未初始化数据库");
        }

    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    /**
     * 创建表默认会创建一个列名为id的列，主键，自动增长
     *
     * @param <T> 泛型对象
     * @param c   要创建的对象类，自动映射为表名
     */
    public <T> void createTable(final Class<T> c) {
        final String TABLE_NAME = JavaReflectUtil.getClassName(c);
        final List<String> column = new ArrayList<>(Arrays.asList(JavaReflectUtil.getAttributeNames(c)));
        final List<Class> type = new ArrayList<>(Arrays.asList(JavaReflectUtil.getAttributeType(c)));
        int idIndex = column.indexOf("id");
        if (idIndex != -1) {
            column.remove(idIndex);
            type.remove(idIndex);
        }
        if (!isTableExist(TABLE_NAME)) {//表不存在，创建表
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(";
            sql += "id  Integer PRIMARY KEY AUTOINCREMENT,";
            for (int i = 0; i < column.size(); i++) {
                if (i != column.size() - 1) {
                    sql += column.get(i) + " " + getType(type.get(i)) + ",";
                } else {
                    sql += column.get(i) + " " + getType(type.get(i));
                }
            }
            sql += ")";
            Log.d(TAG, "创建表" + TABLE_NAME + " sql=" + sql);
            execSQL(sql);
        } else {
            String sql = "PRAGMA table_info([" + TABLE_NAME + "])";
            List<Map<String, Object>> mapList = rawQuery(sql);

            Log.d(TAG, "表信息" + mapList);
            List<String> oldColumnNameList = new ArrayList<>();
            List<String> oldColumnTypeList = new ArrayList<>();
            for (int i = 0; i < mapList.size(); i++) {
                //获取原表字段结构
                String oldColumnName = mapList.get(i).get("name").toString();
                String oldColumnType = mapList.get(i).get("type").toString();
                oldColumnNameList.add(oldColumnName);
                oldColumnTypeList.add(oldColumnType);
            }
            idIndex = oldColumnNameList.indexOf("id");
            if (idIndex != -1) {
                oldColumnNameList.remove(idIndex);
                oldColumnTypeList.remove(idIndex);
            }
            boolean needModifyColumnType = false;
            for (int i = 0; i < column.size(); i++) {
                String newColumn = column.get(i);
                String newColumnType = getType(type.get(i));
                if (!oldColumnNameList.contains(newColumn)) {
                    //判断是否有新增字段
                    String addColumnSql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + newColumn + " " + newColumnType;
                    execSQL(addColumnSql);
                    Log.d(TAG, "升级表【" + TABLE_NAME + "】新字段" + newColumn + ",sql=" + addColumnSql);
                } else {
                    int indexOf = oldColumnNameList.indexOf(newColumn);
                    if (indexOf != -1) {
                        String oldColumnType = oldColumnTypeList.get(indexOf);
                        if (!newColumnType.equals(oldColumnType)) {
                            //需要修改表结构
                            needModifyColumnType = true;
                            Log.d(TAG, "需要修改表结构newColumnType=" + newColumnType + "  oldColumnType=" + oldColumnType);
                        }
                    }
                }
            }
            //修改表结构
            if (needModifyColumnType) {
                //1.查出原表数据
                List<T> list = SQLiteDbUtil.getSQLiteDbUtil().query(c);
                //2.删除原表
                SQLiteDbUtil.getSQLiteDbUtil().drop(TABLE_NAME);

                //3. 创建新表
                sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(";
                sql += "id  Integer PRIMARY KEY AUTOINCREMENT,";
                for (int i = 0; i < column.size(); i++) {
                    if (i != column.size() - 1) {
                        sql += column.get(i) + " " + getType(type.get(i)) + ",";
                    } else {
                        sql += column.get(i) + " " + getType(type.get(i));
                    }
                }
                sql += ")";
                Log.d(TAG, "创建表" + TABLE_NAME + " sql=" + sql);
                execSQL(sql);

                //4. 导入数据
                SQLiteDbUtil.getSQLiteDbUtil().insert(list);
            }
        }
    }

    /**
     * 判断表是否存在
     *
     * @return 表名
     */

    private boolean isTableExist(String tabName) {
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tabName + "'";
        List<Map<String, Object>> rawQuery = rawQuery(sql);
        if ((rawQuery != null && rawQuery.size() > 0)) {
            int count = Integer.parseInt(rawQuery.get(0).get("count(*)").toString());
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    private String getType(Class type) {
        if (type.equals(String.class)) {
            return "TEXT";
        } else if (type.equals(Integer.class) || type.getName().equals("int")) {
            return "Integer";
        } else if (type.equals(Character.class) || type.getName().equals("char")) {
            return "Character";
        } else if (type.equals(Boolean.class) || type.getName().equals("boolean")) {
            return "boolean";
        } else if (type.equals(Float.class) || type.getName().equals("float")) {
            return "Float";
        } else if (type.equals(Double.class) || type.getName().equals("double")) {
            return "Double";
        } else if (type.equals(Byte.class) || type.getName().equals("byte")) {
            return "Byte";
        } else if (type.equals(Short.class) || type.getName().equals("short")) {
            return "Short";
        } else if (type.equals(Long.class) || type.getName().equals("long")) {
            return "Long";
        } else if (type.equals(Date.class)) {
            return "Date";
        } else if (type.equals(java.sql.Date.class)) {
            return "Date";
        } else if (type.isArray()) {
            String name = type.getComponentType().getName();
            if ("byte".equals(name) || "Byte".equals(name)) {
                return "Blob";
            } else {
                return "TEXT";
            }
        } else {
            return "TEXT";
        }

    }

    /**
     * 删除数据表
     *
     * @param <T> 泛型对象
     * @param c   要删除的对象类，自动映射为表名
     */
    public <T> void drop(Class<T> c) {
        if (c == null) {
            return;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        String sql = "DROP TABLE " +
                "IF EXISTS " +
                TABLE_NAME;
        execSQL(sql);
    }

    /**
     * 删除数据表
     *
     * @param tabName 要删除的表名
     */
    public void drop(String tabName) {
        String sql = "DROP TABLE " +
                "IF EXISTS " +
                tabName;
        execSQL(sql);
    }

    /**
     * 插入一条数据
     *
     * @param <T> 泛型对象
     * @param t   要插入的对象
     * @return [影响的行数]he row ID of the newly inserted row, or -1 if an error occurred
     */
    public <T> long insert(T t) {
        if (t == null) {
            return -1;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(t.getClass());
        ContentValues contentValues = getContentValues(t);
        long num = 0;
        try {
            open();
            num = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 插入批量数据
     *
     * @param <T>   泛型对象
     * @param lists 要插入的对象集合
     */
    public <T> void insert(List<T> lists) {
        if (lists == null) {
            return;
        }
        try {
            open();
            sqLiteDatabase.beginTransaction();
            for (T t : lists) {
                String TABLE_NAME = JavaReflectUtil.getClassName(t.getClass());
                ContentValues contentValues = getContentValues(t);
                sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
            close();
        }
    }

    /**
     * 根据id删除一条数据
     *
     * @param c   要删除的对象类
     * @param <T> 泛型对象
     * @param id  要删除的id
     * @return [影响的行数]the number of rows affected
     */
    public <T> int delete(Class<T> c, int id) {
        if (c == null) {
            return 0;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        int num = 0;
        try {
            open();
            num = sqLiteDatabase.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 根据条件删除一条数据
     *
     * @param c           要删除的对象类
     * @param <T>         泛型对象
     * @param whereClause 要修改的条件
     * @param whereArgs   要修改的条件的值
     * @return [影响的行数]the number of rows affected
     */
    public <T> int delete(Class<T> c, String whereClause, String[] whereArgs) {
        if (c == null) {
            return 0;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        int num = 0;
        try {
            open();
            num = sqLiteDatabase.delete(TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 删除表中的全部数据
     *
     * @param c   要删除的对象类
     * @param <T> 泛型对象
     * @return [影响的行数]the number of rows affected
     */
    public <T> int deleteAll(Class<T> c) {
        if (c == null) {
            return 0;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        int num = 0;
        try {
            open();
            num = sqLiteDatabase.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 根据id修改一条数据
     *
     * @param t   要修改的对象
     * @param <T> 泛型对象
     * @param id  要修改的id
     * @return [影响的行数]the number of rows affected
     */
    public <T> int update(T t, int id) {
        if (t == null) {
            return 0;
        }
        int num = 0;
        try {
            String TABLE_NAME = JavaReflectUtil.getClassName(t.getClass());
            ContentValues contentValues = getContentValues(t);
            open();
            num = sqLiteDatabase.update(TABLE_NAME, contentValues, "id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 根据条件修改一条数据
     *
     * @param t           要修改的对象
     * @param <T>         泛型对象
     * @param whereClause 要修改的条件
     * @param whereArgs   要修改的条件的值
     * @return [影响的行数]the number of rows affected
     */
    public <T> int update(T t, String whereClause, String[] whereArgs) {
        if (t == null) {
            return -1;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(t.getClass());
        ContentValues contentValues = getContentValues(t);
        int num = 0;
        try {
            open();
            num = sqLiteDatabase.update(TABLE_NAME, contentValues, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return num;
    }

    /**
     * 根据id查询一条数据
     *
     * @param c   要查询的对象类
     * @param id  要查询的对象的id
     * @param <T> 泛型对象
     * @return 查询出来的数据对象
     */
    public <T> T query(Class<T> c, int id) {

        if (c == null) {
            return null;
        }

        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        Cursor cursor = null;
        try {
            open();
            cursor = sqLiteDatabase.query(TABLE_NAME, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor == null) {

                return null;
            }
            if (cursor.moveToNext()) {
                return newInstance(c, cursor);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return null;
    }

    /**
     * 根据指定条件查询一条数据
     *
     * @param c             要查询的对象类
     * @param selection     要查询的条件字段
     * @param selectionArgs 要查询的条件字段对应的值
     * @param <T>           泛型对象
     * @return 查询出来的数据对象
     */
    public <T> List<T> query(Class<T> c, String selection, String[] selectionArgs) {

        if (c == null) {
            return null;
        }

        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        List<T> lists = null;
        Cursor cursor = null;
        try {
            open();
            cursor = sqLiteDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor == null) {

                return null;
            }
            lists = new ArrayList<>();
            while (cursor.moveToNext()) {
                lists.add(newInstance(c, cursor));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return lists;
    }

    /**
     * 查询表中的所有数据
     *
     * @param c   要查询的对象类
     * @param <T> 泛型对象
     * @return 查询出来的对象类集合
     */
    public <T> List<T> query(Class<T> c) {

        List<T> lists = null;
        Cursor cursor = null;
        try {
            if (c == null) {
                return null;
            }
            open();
            String TABLE_NAME = JavaReflectUtil.getClassName(c);

            cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                return null;
            }
            lists = new ArrayList<>();
            while (cursor.moveToNext()) {
                lists.add(newInstance(c, cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return lists;
    }

    /**
     * 根据id查询一条数据
     *
     * @param c             要查询的对象类
     * @param columns       A list of which columns to return. Passing null will
     *                      return all columns, which is discouraged to prevent reading
     *                      data from storage that isn't going to be used.
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @param groupBy       A filter declaring how to group rows, formatted as an SQL
     *                      GROUP BY clause (excluding the GROUP BY itself). Passing null
     *                      will cause the rows to not be grouped.
     * @param having        A filter declare which row groups to include in the cursor,
     *                      if row grouping is being used, formatted as an SQL HAVING
     *                      clause (excluding the HAVING itself). Passing null will cause
     *                      all row groups to be included, and is required when row
     *                      grouping is not being used.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause
     *                      (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @param limit         Limits the number of rows returned by the query,
     *                      formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @param <T>           泛型对象
     * @return 查询出来的数据对象
     */
    public <T> List<T> query(Class<T> c, String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {
        List<T> lists = null;
        Cursor cursor = null;
        try {
            if (c == null) {
                return null;
            }
            open();
            String TABLE_NAME = JavaReflectUtil.getClassName(c);
            cursor = sqLiteDatabase.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            if (cursor == null) {
                return null;
            }
            lists = new ArrayList<>();
            while (cursor.moveToNext()) {
                lists.add(newInstance(c, cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return lists;
    }

    /**
     * 执行Sql语句建表，插入，修改，删除
     *
     * @param sql 要执行的sqk语句
     * @throws SQLException SQL语句不正确
     */
    public void execSQL(String sql) {
        try {
            open();
            sqLiteDatabase.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    /**
     * 执行Sql语句，查询
     *
     * @param sql 要执行的sql语句
     * @return 一个包含Map中key=字段名,value=值的集合对象
     * @throws SQLException SQL语句不正确
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public List<Map<String, Object>> rawQuery(String sql) {
        List<Map<String, Object>> lists = null;
        Cursor cursor = null;
        try {
            open();
            cursor = sqLiteDatabase.rawQuery(sql, null, null);
            if (cursor == null) {
                return null;
            }
            lists = new ArrayList<>();
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<>();
                for (String column : cursor.getColumnNames()) {
                    int columnIndex = cursor.getColumnIndex(column);
                    if (cursor.getType(columnIndex) == Cursor.FIELD_TYPE_BLOB) {
                        map.put(column, cursor.getBlob(columnIndex));
                    } else {
                        map.put(column, cursor.getString(columnIndex));
                    }
                }
                lists.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return lists;
    }

    /**
     * 根据游标所得到的的值创建一个对象
     *
     * @param c      对象类
     * @param cursor 游标
     * @param <T>    泛型对象
     * @return 所要创建的对象
     */
    private <T> T newInstance(Class<T> c, Cursor cursor) {
        try {
            Constructor<T> con = c.getConstructor();
            con.setAccessible(true);
            T t = con.newInstance();
            Class[] types = JavaReflectUtil.getAttributeType(c);
            String[] columns = JavaReflectUtil.getAttributeNames(c);
            for (int i = 0; i < types.length; i++) {
                String column = columns[i];
                Class type = types[i];
                Method method = c.getMethod("set" + column.substring(0, 1).toUpperCase() + column.substring(1), new Class[]{type});
                Object value = null;
                if (type.equals(String.class)) {
                    value = cursor.getString(cursor.getColumnIndex(column));
                } else if (type.equals(Integer.class) || type.getName().equals("int")) {
                    value = cursor.getInt(cursor.getColumnIndex(column));
                } else if (type.equals(Character.class) || type.getName().equals("char")) {
                    value = Character.valueOf(cursor.getString(cursor.getColumnIndex(column)).charAt(0));
                } else if (type.equals(Boolean.class) || type.getName().equals("boolean")) {
                    value = "true".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(column))) ? true : false;
                } else if (type.equals(Float.class) || type.getName().equals("float")) {
                    value = cursor.getFloat(cursor.getColumnIndex(column));
                } else if (type.equals(Double.class) || type.getName().equals("double")) {
                    value = cursor.getDouble(cursor.getColumnIndex(column));
                } else if (type.equals(Byte.class) || type.getName().equals("byte")) {
                    value = Byte.valueOf(cursor.getString(cursor.getColumnIndex(column)));
                } else if (type.equals(Short.class) || type.getName().equals("short")) {
                    value = cursor.getShort(cursor.getColumnIndex(column));
                } else if (type.equals(Long.class) || type.getName().equals("long")) {
                    value = cursor.getLong(cursor.getColumnIndex(column));
                } else if (type.isArray()) {
                    value = cursor.getBlob(cursor.getColumnIndex(column));
                } else if (type.equals(Date.class)) {
                    value = TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(column))) ? null : new Date(cursor.getString(cursor.getColumnIndex(column)));
                } else if (type.equals(java.sql.Date.class)) {
                    value = TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(column))) ? null : java.sql.Date.valueOf(cursor.getString(cursor.getColumnIndex(column)));
                } else {
                    continue;
                }
                method.invoke(t, value);
            }
            return t;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据对象获取 ContentValues
     *
     * @param t   对象
     * @param <T> 泛型对象
     * @return ContentValues用来操作SQLite数据库
     */

    private <T> ContentValues getContentValues(T t) {
        List<Map<String, Object>> allInfo = JavaReflectUtil.getAllFiledInfo(t);
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < allInfo.size(); i++) {
            Map<String, Object> info = allInfo.get(i);
            String column = (String) info.get("name");
            Object value = info.get("value");
            if (value == null) {
                contentValues.putNull(column);
            } else {
                if (value instanceof byte[]) {
                    contentValues.put(column, (byte[]) value);
                } else {
                    contentValues.put(column, String.valueOf(value));
                }
            }
        }
        contentValues.remove("id");
        return contentValues;
    }

}
