package cn.bluemobi.dylan.sqlite.mode;

import java.util.Date;

/**
 * Created by yuandl on 2016-11-21.
 */

public class User {
    private int id;
    private String name;
    private int age;
    private Double integral;
    private Date time;
    private boolean flag;
    private String  description;

    public int getAge() {
        return age;
    }

    public User setAge(int age) {
        this.age = age;
        return this;
    }

    public int getId() {
        return id;
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    public Double getIntegral() {
        return integral;
    }

    public User setIntegral(Double integral) {
        this.integral = integral;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isFlag() {
        return flag;
    }

    public User setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public User setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", integral=" + integral +
                ", time=" + time +
                ", flag=" + flag +
                ", description='" + description + '\'' +
                '}';
    }
}
