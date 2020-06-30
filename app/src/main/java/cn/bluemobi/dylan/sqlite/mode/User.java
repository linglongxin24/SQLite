package cn.bluemobi.dylan.sqlite.mode;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by yuandl on 2016-11-21.
 */

public class User implements Parcelable {
    private int id;
    private String name;
    private int age;
    private Double integral;
    private Date time;
    private boolean flag;
    private String  description;
    private byte[]  feature;

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

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
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
                ", feature=" + Arrays.toString(feature) +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.age);
        dest.writeValue(this.integral);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeByte(this.flag ? (byte) 1 : (byte) 0);
        dest.writeString(this.description);
        dest.writeByteArray(this.feature);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.age = in.readInt();
        this.integral = (Double) in.readValue(Double.class.getClassLoader());
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.flag = in.readByte() != 0;
        this.description = in.readString();
        this.feature = in.createByteArray();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
