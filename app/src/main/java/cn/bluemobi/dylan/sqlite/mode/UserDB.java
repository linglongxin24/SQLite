package cn.bluemobi.dylan.sqlite.mode;

/**
 * 用户表
 * Created by lenovo on 2017/9/6.
 */

public class UserDB {
    private int id;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户学号
     */
    private String userNum;
    /**
     * 用户卡号
     */
    private String userCardNum;
    /**
     * 用户名
     */
    private String userName;

    public int getId() {
        return id;
    }

    public UserDB setId(int id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserDB setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserNum() {
        return userNum;
    }

    public UserDB setUserNum(String userNum) {
        this.userNum = userNum;
        return this;
    }

    public String getUserCardNum() {
        return userCardNum;
    }

    public UserDB setUserCardNum(String userCardNum) {
        this.userCardNum = userCardNum;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserDB setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String toString() {
        return "UserDB{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", userNum='" + userNum + '\'' +
                ", userCardNum='" + userCardNum + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
