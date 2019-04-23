package pl.edu.agh.papaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
public class Papaya {

    public static void main(String[] args) {
        SpringApplication.run(Papaya.class, args);
    }
}
