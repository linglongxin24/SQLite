package cn.bluemobi.dylan.sqlite;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.bjtsh.dylan.selectphoto.SelectPhoto;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        SQLiteDatabase s= openOrCreateDatabase("mytest.db",MODE_PRIVATE,null);

//        SQLiteDatabase.openOrCreateDatabase()
    }

    /**
     * Android Api创建SQLite数据库的三种方式
     *
     * @param view
     */
    public void createDataBase(View view) {
        startActivity(new Intent(this, CreateOrOpenActivity.class));
    }

    /**
     * Android 通过Sql语句操作数据库
     *
     * @param view
     */
    public void sql(View view) {
        startActivity(new Intent(this, SqlOperateActivity.class));
    }

    /**
     * Android 通过Sql语句操作数据库
     *
     * @param view
     */
    public void api(View view) {
        startActivity(new Intent(this, ApiOperateActivity.class));
    }

    /**
     * SQLite优化
     *
     * @param view
     */
    public void optimize(View view) {
        startActivity(new Intent(this, OptimizeActivity.class));
    }



    /**
     * SQLite应用案例
     *
     * @param view
     */
    public void search(View view) {
        startActivity(new Intent(this, SearchActivity.class));
    }
    /**
     * SQLite封装工具类
     *
     * @param view
     */
    public void util(View view) {
        startActivity(new Intent(this, UtilTestActivity.class));
    }

    public void slectPhoto(View view) {
        new SelectPhoto(this).selectPhoto();
    }
}
