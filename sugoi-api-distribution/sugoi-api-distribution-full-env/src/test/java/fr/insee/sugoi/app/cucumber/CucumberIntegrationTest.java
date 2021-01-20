package fr.insee.sugoi.app.cucumber;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * CucumberIntegrationTest
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/scenario/" }, glue = {
        "fr.insee.sugoi.app.cucumber.glue" }, dryRun = false, strict = true, plugin = { "pretty",
                "json:target/cucumber/cucumber.json", "usage:target/cucumber/usage.jsonx",
                "junit:target/cucumber/junit.xml",
                "de.monochromata.cucumber.report.PrettyReports:target/cucumber" })
public class CucumberIntegrationTest {

}