package com.yc.bean;

import com.yc.springframework.context.annotation.PostConstruct;

public class Person {

	public Person() {
		System.out.println("调用了Person无参构造方法");
	}
	
	@PostConstruct
	public void init() {
		System.out.println("调用了Person的   init");
	}

	public void show() {
		System.out.println("hello world");
	}
}
