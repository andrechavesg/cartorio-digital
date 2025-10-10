package br.com.cartoriodigital.identity;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
class IdentityVerificationResourceTest {

    @Test
    void shouldCreateAndApproveVerification() {
        Map<String, Object> payload = Map.of(
                "applicant", Map.of(
                        "name", "Joana da Silva",
                        "documentNumber", "12345678900",
                        "email", "joana@example.com"
                ),
                "evidences", List.of(Map.of(
                        "type", "DOCUMENT",
                        "checksum", "abc123",
                        "collectedAt", Instant.now().toString(),
                        "collectedBy", "agent-1"
                ))
        );

        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/identity/verifications")
                .then()
                .statusCode(201)
                .body("status", Matchers.is("IN_REVIEW"))
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "operatorId", "operator-1",
                        "notes", "Validacao presencial"
                ))
                .when()
                .post("/identity/verifications/" + id + "/approve")
                .then()
                .statusCode(200)
                .body("status", Matchers.is("APPROVED"))
                .body("decision.operatorId", Matchers.is("operator-1"));
    }
}
