package config;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.codeborne.selenide.Configuration.*;

public class Config {

    @BeforeAll
    static void setUp(){
        browser = "chrome";
        browserSize = "1920x1080";
        screenshots = false;
        headless = false;
        //remote = "http://localhost:8080/";
    }

    @AfterAll
    static void tearDown(){
        Selenide.webdriver().driver().close();
    }

}
