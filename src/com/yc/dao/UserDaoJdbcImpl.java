package com.yc.dao;

import com.yc.springframework.context.annotation.Component;


public class UserDaoJdbcImpl implements UserDao {

	@Override
	public void addUser() {
		String sql="";
		//  xxx.doUpdate();
		System.out.println("添加用户");
	}

	@Override
	public void deleteUser() {
		System.out.println("删除用户");
	}

	@Override
	public void updateUser() {
		System.out.println("修改用户");
	}

	@Override
	public void findUser() {
		System.out.println("查询用户");
	}

}
