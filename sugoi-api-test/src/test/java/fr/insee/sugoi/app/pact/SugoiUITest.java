package fr.insee.sugoi.app.pact;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import fr.insee.sugoi.app.SugoiTestService;

@RunWith(PactRunner.class)
@Provider("Sugoi-API-User")
@PactFolder("../resources/contracts")
public class SugoiUITest {

    @TestTarget
    public final Target target = new HttpTarget("http", "localhost", 8082, "/spring-rest");


    @BeforeClass
    public static void start() {
        SpringApplication.run(SugoiTestService.class);
        SugoiUITest.getRes
    }

    @State("test GET")
    public void toGetState() { }

    @Test
    public void test(){
        System.out.println("coucou");
    }

}