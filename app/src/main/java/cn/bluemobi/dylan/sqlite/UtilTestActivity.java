package cn.bluemobi.dylan.sqlite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.bluemobi.dylan.sqlite.contacts.Contacts;
import cn.bluemobi.dylan.sqlite.mode.User;
import cn.bluemobi.dylan.sqlite.mode.UserDB;
import cn.bluemobi.dylan.sqlitelibrary.JavaReflectUtil;
import cn.bluemobi.dylan.sqlitelibrary.SQLiteDbUtil;

/**
 * 通过封装的工具类操作SQLite
 * Created by Administrator on 2016-11-19.
 */

public class UtilTestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("通过封装的工具类操作SQLite");
        setContentView(R.layout.ac_api_operate);
        SQLiteDbUtil.getSQLiteDbUtil().openOrCreateDataBase(this);
        Log.d(Contacts.TAG, "User类getAttributeNames：" + Arrays.toString(JavaReflectUtil.getAttributeNames(User.class)));
        Log.d(Contacts.TAG, "User类getAttributeType：" + Arrays.toString(JavaReflectUtil.getAttributeType(User.class)));
        Log.d(Contacts.TAG, "UserDB类的getAttributeNames：" +  Arrays.toString(JavaReflectUtil.getAttributeNames(UserDB.class)));
        Log.d(Contacts.TAG, "UserDB类的getAttributeType：" +  Arrays.toString(JavaReflectUtil.getAttributeType(UserDB.class)));
    }

    /**
     * 1.创建数据表user
     * 表名 user
     * *数据表user表结构字段
     * 主键：id
     * 名字：name
     * 年龄：age:
     *
     * @param v
     */
    public void create(View v) {
        SQLiteDbUtil.getSQLiteDbUtil().createTable(User.class);
//        SQLiteDbUtil.getSQLiteDbUtil().createTable(UserDB.class);
    }

    /**
     * 2.删除数据表user
     *
     * @param v
     */
    public void drop(View v) {
        SQLiteDbUtil.getSQLiteDbUtil().drop(User.class);
    }

    /**
     * 3.给user表中新增一条数据
     * <p>
     * long insert(String table, String nullColumnHack, ContentValues values)
     * 第一个参数：数据库表名
     * 第二个参数：当values参数为空或者里面没有内容的时候，
     * insert是会失败的(底层数据库不允许插入一个空行)，
     * 为了防止这种情况，要在这里指定一个列名，
     * 到时候如果发现将要插入的行为空行时，
     * 就会将你指定的这个列名的值设为null，然后再向数据库中插入。
     * 第三个参数：要插入的值
     * 返回值：成功操作的行号，错误返回-1
     *
     * @param v
     */
    public void insert(View v) {
        User user = new User();
        user.setName("张三");
        user.setAge(22);
        user.setIntegral(12.03);
        user.setFlag(true);
        user.setFeature(new byte[512]);
//        user.setTime(new Date());
        long num = SQLiteDbUtil.getSQLiteDbUtil().insert(user);
        if (num == -1) {
            Toast.makeText(this, "插入失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "成功插入到第" + num + "行", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 4.修改user表中id为2的名字改成“李四”
     * <p>
     * int update(String table, ContentValues values, String whereClause, String[] whereArgs)
     * 第一个参数：表名
     * 第二个参数：所要修改该的字段对应的值
     * 第三个参数：修改的条件字段
     * 第四个参数：修改的条件字段对应的值
     * 返回值：影响的行数
     *
     * @param v
     */
    public void update(View v) {

        User user = new User();
        user.setName("李四");
        user.setAge(22);
        user.setIntegral(12.03);
        int num = SQLiteDbUtil.getSQLiteDbUtil().update(user, 5);
        Toast.makeText(this, "修改了" + num + "行", Toast.LENGTH_SHORT).show();
//        user.setName("王五");
//        SQLiteDbUtil.getSQLiteDbUtil().update(user, "name='李四'");
    }

    /**
     * 5.删除user表中id为2的记录
     * <p>
     * int delete(String table, String whereClause, String[] whereArgs)
     * 第一个参数：删除的表名
     * 第二个参数：修改的条件的字段
     * 第三个参数：修改的条件字段对应的值
     * 返回值：影响的行数
     *
     * @param v
     */
    public void delete(View v) {
        int num = SQLiteDbUtil.getSQLiteDbUtil().delete(User.class, 2);
        Toast.makeText(this, "删除了" + num + "行", Toast.LENGTH_SHORT).show();
//     SQLiteDbUtil.getSQLiteDbUtil().delete(User.class,"name='李四'");
    }

    /**
     * 6.查询数据
     * <p>
     * Cursor query(String table, String[] columns, String selection,String[] selectionArgs, String groupBy, String having,String orderBy)
     * 第一个参数：表名
     * 第二个参数：要查询的字段名
     * 第三个参数：要查询的条件字段
     * 第四个参数：要查询的条件字段对应的值
     * 第五个参数：分组的字段
     * 第六个参数：筛选的字段
     * 第七个参数：排序的字段
     * 返回值：游标
     *
     * @param v
     */
    public void query(View v) {
        List<User> users = SQLiteDbUtil.getSQLiteDbUtil().query(User.class);
        if (users == null) {

            Log.d(Contacts.TAG, "没有数据");
            return;
        }

        Log.d(Contacts.TAG, "util查：共" + users.size() + "个对象=" + users.toString());
        String sql = "SELECT * FROM User";
        List<Map<String, Object>> list = SQLiteDbUtil.getSQLiteDbUtil().rawQuery(sql);
        Log.d(Contacts.TAG, "sql查：共" + list.size() + "个对象=" + list.toString());
        byte[] feature = (byte[]) list.get(0).get("feature");
        Log.d(Contacts.TAG, "feature=" + Arrays.toString(feature));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
