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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

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
    public <T> void createTable(Class<T> c) {
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        List<String> column = new ArrayList<>(Arrays.asList(JavaReflectUtil.getAttributeNames(c)));
        List<Class> type = new ArrayList<>(Arrays.asList(JavaReflectUtil.getAttributeType(c)));
        int idIndex = column.indexOf("id");
        if (idIndex != -1) {
            column.remove(idIndex);
            type.remove(idIndex);
        }
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
    }

    private String getType(Class type) {
        if (type.equals(String.class)) {
            return "String";
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
        } else {
            return "String";
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
     * @param c     要删除的对象类
     * @param <T>   泛型对象
     * @param where 要删除条件
     * @return [影响的行数]the number of rows affected
     */
    public <T> int delete(Class<T> c, String where) {
        if (c == null) {
            return 0;
        }
        String TABLE_NAME = JavaReflectUtil.getClassName(c);
        int num = 0;
        try {
            open();
            num = sqLiteDatabase.delete(TABLE_NAME, where, null);
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
                    map.put(column, cursor.getString(cursor.getColumnIndex(column)));
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
                } else if (type.equals(Date.class)) {
                    value = TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(column))) ? null : new Date(cursor.getString(cursor.getColumnIndex(column)));
                } else if (type.equals(java.sql.Date.class)) {
                    value = TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(column))) ? null : java.sql.Date.valueOf(cursor.getString(cursor.getColumnIndex(column)));
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
                contentValues.put(column, String.valueOf(value));
            }
        }
        contentValues.remove("id");
        return contentValues;
    }

}
