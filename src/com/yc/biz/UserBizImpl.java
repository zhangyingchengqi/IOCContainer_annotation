package com.yc.biz;

import com.yc.dao.UserDao;
import com.yc.springframework.context.annotation.Autowired;
import com.yc.springframework.context.annotation.Component;
import com.yc.springframework.context.annotation.PostConstruct;
import com.yc.springframework.context.annotation.Resource;

@Component     //    mo信为  userBizImpl作为beanId, 
public class UserBizImpl implements UserBiz {
	
	@Autowired
	private UserDao userDao;    //     boolean exists        boolean isExists()    setExists()
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@PostConstruct
	public void init() {
		System.out.println("调用了userBizImpl的   init");
	}
	
	public UserBizImpl(UserDao userDao) {
		this.userDao=userDao;
	}
	
	public UserBizImpl() {
		
	}
	
	

	@Override
	public void addUser() {
		userDao.addUser();
	}

	@Override
	public void deleteUser() {
		userDao.deleteUser();
	}

	@Override
	public void updateUser() {
		userDao.updateUser();

	}

	@Override
	public void findUser() {
		userDao.findUser();
	}

}
