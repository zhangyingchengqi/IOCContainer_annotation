import com.yc.bean.Person;
import com.yc.biz.UserBiz;
import com.yc.biz.UserBizImpl;
import com.yc.dao.UserDao;
import com.yc.dao.UserDaoJdbcImpl;
import com.yc.dao.UserDaoRedisImpl;
import com.yc.springframework.context.AnnotationConfigApplicationContext;
import com.yc.springframework.context.ApplicationContext;
import com.yc.springframework.context.annotation.Bean;
import com.yc.springframework.context.annotation.ComponentScan;
import com.yc.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="com.yc")
public class AppConfigure {

	@Bean
	public Person p() {
		return new Person();
	}

//	@Bean
//	public UserBiz userBizImpl() {
//		return new UserBizImpl();
//	}
//
//	@Bean
//	public UserDao userDaoJdbcImpl() {
//		return new UserDaoJdbcImpl();
//	}
//
//	@Bean
//	public UserDao userDaoRedisImpl() {
//		return new UserDaoRedisImpl();
//	}

	public static void main(String[] args) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfigure.class);

		// 容器初始化时,mo认完成IOC操作

		Person p = (Person) ctx.getBean(Person.class);
		p.show();

		Person p2 = (Person) ctx.getBean("p");
		p.show();

		System.out.println(p == p2);

		UserBiz ub = (UserBiz) ctx.getBean("userBizImpl");
		System.out.println(ub);

		ub.addUser();
	}
}
