package com.yc.springframework.context;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.yc.dao.UserDao;
import com.yc.springframework.context.annotation.Autowired;
import com.yc.springframework.context.annotation.Bean;
import com.yc.springframework.context.annotation.Component;
import com.yc.springframework.context.annotation.ComponentScan;
import com.yc.springframework.context.annotation.PostConstruct;
import com.yc.springframework.context.annotation.Resource;


public class AnnotationConfigApplicationContext implements ApplicationContext {
	private Map<String, Object> beans=new HashMap<String,Object>();
	
	@Override
	public Object getBean(String beanId) {
		return beans.get(  beanId);
	}

	@Override
	public Object getBean(Class clz) {
		Collection<Object> collection=beans.values();
		Iterator<Object> its=collection.iterator();
		while( its.hasNext() ) {
			Object obj=its.next();
			if(  obj.getClass().getName().equals( clz.getName() )) {
				return obj;
			}
		}
		return null;
	}
	
	
	/**
	 * 配置类的实例，可以用多个  ,  因为配置文件是可以有多个. (  dao, service, mail, jms,.....)..
	 * AppConfigure    MailConfigure   JmsConfigure
	 * @param clz
	 */
	public AnnotationConfigApplicationContext( Class... clz) {
		if(  clz==null) {
			return;
		}
		for(   Class c: clz) {   // c:  ->  AppConfigure   也有可能是  MailConfigure   JmsConfigure
			register(  c );
		}
	}

	private void register(Class c) {   // c: AppConfigure
		try {
			// 1. 完成的是将所有的  @Bean对应的方法中创建的对象托管到了  spring中
			//读取    c  中的方法  取出带了   @Bean注解     的方法  激活   
			// c =>  getDeclarMethod() /   getMethods()   
			//循环这些方法，判断是否带有  @Bean注解,如果有，则
			//  invoke(    对象， 参数 )
			//取出返回值，  Object  -> 存到  beans的map中,  键名:  方法名.
			Method[] ms=c.getMethods();
			Object obj=c.newInstance();
			for(  Method m:ms) {
				Annotation[] ans=  m.getAnnotations();
				for(   Annotation a:ans) {
					if(  a instanceof Bean ) {
						Object o=m.invoke(obj, m.getParameters());   // 
						
						//TODO:实例化对象后，要查看这个对象中是否有  @PostConstruct
						postConstruct(   o      );
						
						String beanId=m.getName();
						beans.put(beanId, o);
					}
				}
			}
			//以上完成的是将所有的  @Bean对应的方法中创建的对象托管到了  spring中
			
			
			
			//DI:  constructor    field*     setter
			//2. 将上面托管的对象中带有  @Resource的属性进入    注入  -> 利用   field结合 @Resource注解完成注入
			Collection<Object> collections=beans.values();   //所有托管的对象
			Iterator<Object> ites=collections.iterator();
			while(   ites.hasNext() ) {
				Object managedBean=ites.next();
				//第一种注入方案的实现:  field
				Field[] fs=managedBean.getClass().getDeclaredFields();
				for(  Field f: fs) {
					Annotation[] fieldAns=f.getAnnotations();
					for(  Annotation a:fieldAns) {
						if(  a instanceof Resource) {    
							// @Resource(name="userDaoJdbcImpl")
							//private UserDao userDao;   1. 根据name的值找beans中的托管bean   2. 设置到 userDao属性上
							String beanId=((Resource)a).name();
							if(  beanId.equals("") ) {
								beanId=f.getName();
							}
							Object beanObject=beans.get(beanId);
							f.setAccessible(true);  //设置可访问性     private -> 
							//UerBizImpl
							f.set(managedBean, beanObject);       // userBizImpl.userDao=userDaoJdbcImpl
							
						}
					}
				}
				//TODO: 第二种注入方案的实现:  setter
				Method[] mms=managedBean.getClass().getMethods();
				for( Method method:mms) {
					Annotation[] methodAns=method.getAnnotations();
					for( Annotation a:methodAns) {
						if( a instanceof Resource) {
							//@Resource
							//public void setUserDao(UserDao userDao) {
							String beanId=((Resource)a).name();
							if(  beanId.equals("") ) {
								String methodName=method.getName();
								beanId=methodName.substring(3,4).toLowerCase()+methodName.substring(4);
							}
							Object beanObject=beans.get(beanId);
							method.invoke(managedBean, beanObject);
						}
					}
				}
			}
			
			//TODO: @ComponentScan
			ComponentScan csAnnotation=(ComponentScan) c.getAnnotation(  ComponentScan.class );
			if(  csAnnotation!=null) {
				String packageName=csAnnotation.basePackages();  //这里指定要扫描的路径
				findPackageAndSubPackageInClasses( packageName);  // com.yc
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 找到类路径下包及子包中的所有字节码文件
	 * @param packageName包名
	 */
	public void findPackageAndSubPackageInClasses(String packageName) {
		// \\.
		String packagePath = packageName.replaceAll("\\.", "/"); // com.yc.biz
																	// -> 将.转为 /
																	// com/yc/spring
		try {
			// getContextClassLoader() 返回该线程的上下文 ClassLoader
			// getResources查找具有给定名称的资源。
			Enumeration<URL> files = Thread.currentThread().getContextClassLoader().getResources(packagePath);
			// /C:/Users/Administrator/eclipse-workspace/selfspring_componentscan/bin/com/yc
			while (files.hasMoreElements()) {
				URL url = files.nextElement();
				System.out.println("配置的要扫描的包的路径为:" + url.getFile());
				// 找包中的字节码文件
				finPackageClasses(url.getFile(), packageName);
				
				//TODO:   *********从 classes的 set集中   筛选出  @Component,实例化
				createComponentBean(classes);
			}
		} catch (Exception e) {
			throw new RuntimeException("包资源" + packagePath + "取到失败!!!", e);
		}
	}
	
	private Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

	/**
	 * 找到包下的字节码文件
	 * @param packagePath:   /Users/yingzhang/Desktop/git_projects/spring/springContainer/bin/com/yc 基路径
	 * @param  packageName:   com.yc
	 */
	public void finPackageClasses(String packagePath, String packageName) {
		// 从packagePath这个包中查找所有的 .class类及子包
		// System.out.println( packagePath.substring(1) );
		//if(   packagePath.startsWith("/")){
		//	packagePath=packagePath.substring(1);
		//}
		File[] files = new File(packagePath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// System.out.println( "====="+ file.getAbsolutePath() );
				return file.getName().endsWith(".class") || file.isDirectory();// 过滤出xxx.class文件和目录
			}
		});
		if (files != null && files.length > 0) {
			// 循环这些找到的文件
			for (File f : files) {
				// 如果是目录 ，则表示这是一个子包, 递归再查找它下面的字节码文件  com/yc/biz
				if (f.isDirectory()) {       
					finPackageClasses(  f.getAbsolutePath(), packageName + "." + f.getName()); // com.yc.biz
				} else { // 是字节码文件，则存到 classes集合中，这些字节码文件 就是将来ioc的.
					// 要根据字节码文件创建类的类对象  自定义tomcat   servlet
					URLClassLoader uc = new URLClassLoader(new URL[] {});// 指定文件的类路径
					try {
						// com.yc.dao.StudentDaoImpl
						// com.yc.biz.StudentBizImpl     javac  xxx.java     java xxx
						classes.add(  uc.loadClass(packageName + "." + f.getName().replace(".class", "")));// 根据前类名
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							uc.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * 找交给容器管理的bean的类的类对象
	 * @param classes
	 */
	public void createComponentBean(Set<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			// TODO:其它的语义注解...
			if (clazz.isAnnotationPresent(Component.class)) {
				String beanName = clazz.getAnnotation(Component.class).value();
				if(   beanName==null||"".equals(beanName)) {
					String className=clazz.getSimpleName();
					beanName=className.substring(0,1).toLowerCase()+className.substring(1);
				}
				try {
					if(   beans.get(beanName)==null){
						Object o=clazz.newInstance();
						postConstruct(   o      );
						beans.put(beanName, o); // ioc
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("创建类为:" + clazz.getName() + "的对象失败!!!", e);
				}
			}
		}
		// di
		createAutoWired(classes);
	}
	
	/**
	 * 注入bean的依赖bean对象
	 * @param classes
	 */
	public void createAutoWired(Set<Class<?>> classes) {
		try {
			for (Class<?> clazz : classes) {
				//按属性注入  fields  autowired
				// 取出类中的属性
				Field[] fs = clazz.getDeclaredFields();// getDeclaredFields()
														// 能获取自己声明的各种字段，包括public，protected，private。
				if(   !clazz.isAnnotationPresent(Component.class)) {
					continue;
				}
				String beanName = clazz.getAnnotation(Component.class).value(); // id
				if(  beanName==null|| "".equals(beanName)) {
					beanName=  clazz.getSimpleName().substring(0,1).toLowerCase()+clazz.getSimpleName().substring(1);
				}
				for (Field field : fs) {
					// Autowired 自动装配: -> 
					if (field.isAnnotationPresent(Autowired.class)) {
						Class type=field.getType();    // private UserDao userDao; 
						Collection<Object> collection=beans.values();
						Iterator<Object> ites=collection.iterator();
						int i=0;
						Object bean=null;
						while( ites.hasNext() ) {
							 bean=ites.next();
							//判断     UserDaoJdbcImpl是否为  UserDao的子类
							if(  type.isAssignableFrom(bean.getClass() )     ) {
								i++;
								if( i>=2) {
									throw new RuntimeException("期待"+  type.getName()+"的一个对象，但出现了:"+i+"个" );
								}
							}
						}
						Object obj = beans.get(   beanName  );
						field.setAccessible(true);
						field.set(obj, bean);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	


	private void postConstruct(Object o) {
		try {
			Method[] mms=o.getClass().getMethods();
			for( Method method:mms) {
				Annotation[] methodAns=method.getAnnotations();
				for( Annotation a:methodAns) {
					if( a instanceof PostConstruct) {
						method.invoke(o,  null  );
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	

}
