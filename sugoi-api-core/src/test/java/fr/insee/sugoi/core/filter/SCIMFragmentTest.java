package fr.insee.sugoi.core.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SCIMFragmentTest {
    @ParameterizedTest
    @CsvSource({"test eq \"value\",test,EQUALITY,\"value\"","test eq 2,test,EQUALITY,2"})
    public void convertTestEq_shouldBeProperlyConverted(String input, String attribute, SCIMFilterOperator middleOperator, Object value){
        SCIMFragment fragment = SCIMFragment.of(input);
        Assertions.assertEquals(attribute,fragment.getAttribute());
        Assertions.assertEquals(middleOperator,fragment.getScimFilterOperator());
        System.out.printf(String.valueOf(value.getClass()));
        Assertions.assertEquals(value,fragment.getValue());
    }
    @ParameterizedTest
    @CsvSource({"title pr","test eq \"value\" 2"})
    public void convertTestLengthNot3_shouldFail(String input){
        Assertions.assertThrows(RuntimeException.class,() -> SCIMFragment.of(input));
    }
}
