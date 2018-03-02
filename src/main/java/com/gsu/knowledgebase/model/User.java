package com.gsu.knowledgebase.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by cnytync on 01/03/2018.
 */
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Long roleId;
    private Integer status;
    private Integer loginTryCount;

    public User(){

    }

    public User(ResultSet rs) throws SQLException {
        this.id = rs.getLong("u.id");
        this.username = rs.getString("u.username");
        this.email = rs.getString("u.email");
        this.password = rs.getString("u.password");
        this.firstName = rs.getString("u.first_name");
        this.lastName = rs.getString("u.last_name");
        this.roleId = rs.getLong("u.role_id");
        this.status = rs.getInt("u.status");
        this.loginTryCount = rs.getInt("u.login_try_count");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLoginTryCount() {
        return loginTryCount;
    }

    public void setLoginTryCount(Integer loginTryCount) {
        this.loginTryCount = loginTryCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
