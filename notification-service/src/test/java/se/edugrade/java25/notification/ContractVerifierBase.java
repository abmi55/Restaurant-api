package se.edugrade.java25.notification;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for the generated {@code ContractVerifierTest}.
 *
 * <p>The spring-cloud-contract-maven-plugin generates one test method per
 * contract file. Each method sends the contract's request through MockMvc and
 * asserts that the response matches the contract field by field.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class ContractVerifierBase {

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUpContractVerifier() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }
}
