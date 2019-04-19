package pl.edu.agh.papaya.tests.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:/application.properties",
        "classpath:/application-test.properties"})
public class PapayaContextConfiguration {

}
