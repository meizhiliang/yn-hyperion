package yuanian.middleconsole.hyperion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @projectName hyperion
 * @author ZhiliangMei
 * @date 2022/10/13
 * @menu: TODO
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = {"yuanian.middleconsole.**.dao", "middleconsole.hyperion.**.dao"})
public class HyperionApplication {

	public static void main(String[] args) {
		SpringApplication.run(HyperionApplication.class, args);
	}

}
