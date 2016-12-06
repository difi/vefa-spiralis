package no.balder.spiralis;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.Validation;
import no.difi.vefa.validator.api.ValidatorException;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * Attempts to validate an EHF xml file with the validator
 *
 * @author steinar
 *         Date: 25.11.2016
 *         Time: 19.41
 */
@Test(enabled = false)
public class ValidatorTest {

    private Validator validator;

    @BeforeTest
    public void setUp() {
        try {
            validator = ValidatorBuilder.newValidator().build();
        } catch (ValidatorException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterTest
    public void tearDown() {
        validator.close();
    }

    @Test(enabled = false)
    public void testValidator() throws Exception {

        assertNotNull(validator);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("hafslund-test-1.xml");
        assertNotNull(resourceAsStream);

        Validation validation = validator.validate(resourceAsStream);
        FlagType flag = validation.getReport().getFlag();

        validation.getReport().setFilename("hafslund-test-1.xml");

        for (SectionType sectionType : validation.getReport().getSection()) {
            System.out.println("\nSection: " + sectionType.getTitle());
            for (AssertionType assertionType : sectionType.getAssertion()) {
                System.out.println("\n");
                System.out.println(assertionType.getIdentifier());
                System.out.println("Test:" + assertionType.getTest());
                System.out.println("Text: " + assertionType.getText());
                System.out.println("Flag: " + assertionType.getFlag().name());

            }
        }

        System.out.println(flag.name());
    }



}
