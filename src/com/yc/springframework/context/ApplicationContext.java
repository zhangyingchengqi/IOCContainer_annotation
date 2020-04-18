package com.yc.springframework.context;


// ClassPathXmlApplicationContext    FileSystemXmlApplicationContext
//  AnnotationConfigApplicationContext

public interface ApplicationContext {
	
	public Object getBean(  String beanId );
	
	public Object getBean(   Class clz );
}
