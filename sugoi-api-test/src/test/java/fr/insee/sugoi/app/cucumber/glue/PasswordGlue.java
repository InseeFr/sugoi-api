package fr.insee.sugoi.app.cucumber.glue;

import fr.insee.sugoi.app.cucumber.utils.StepData;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class PasswordGlue {

  private Scenario scenario;

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  private StepData stepData;

  public PasswordGlue(StepData stepData) {
    this.stepData = stepData;
  }
}
