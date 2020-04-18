模拟使用全注解实现spring的ioc和di
   1. 如何ioc:    使用了  @Configuration   @Bean注解
   
   @Configuration
   public class AppConfigure{
       @Bean
       public Person p(){
       	return new Person();   
       }
   }
  
  
  
   2. 如何di:  使用了 @Resource
   
 思路:
    1. YcApplicationContext 接口
         getBean( String )
    2. YcAnnotationConfigApplicationContext类实现  YcApplicationContext 接口
      		 属性:   Map<String,Object> beans
      		 构造方法:  YcAnnotationConfigApplicationContext(  Class clz)
      		 
推断: 
  1. 在构造方法中要使用   反射机制读取   AppConfig中所有的带有  @Bean注解的方法.   -> 就要ioc一下. 
  2. 接下来，读取所有被ioc实例化后的类，看它们里面是否有一个带有 @Resource注解的方法，如果有，则激活它，完成  di. 
  
 技术: 
  1. 注解的定义.    @Configure  @Bean   @Resource
  2. 注解解析  -> 反射. 