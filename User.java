package com.emotionrobotics.apps.sanbotmotion.Entity;

public class User {

    private String group_id;
    private String user_id;
    private String user_info;
    private double score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (Double.compare(user.score, score) != 0) return false;
        if (group_id != null ? !group_id.equals(user.group_id) : user.group_id != null)
            return false;
        if (user_id != null ? !user_id.equals(user.user_id) : user.user_id != null) return false;
        return user_info != null ? user_info.equals(user.user_info) : user.user_info == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = group_id != null ? group_id.hashCode() : 0;
        result = 31 * result + (user_id != null ? user_id.hashCode() : 0);
        result = 31 * result + (user_info != null ? user_info.hashCode() : 0);
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "group_id='" + group_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", user_info='" + user_info + '\'' +
                ", score=" + score +
                '}';
    }
}
