package dev.qa.clients;

import dev.qa.config.Config;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * REST-Assured client for the /posts API. It is the API equivalent of a page object:
 * requests only, no assertions. The spec is immutable, so one instance is safe to share
 * between parallel TestNG threads. Every call is attached to the Allure report.
 */
public class PostsClient {

    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .setContentType(ContentType.JSON)
            .addFilter(new AllureRestAssured())
            .build();

    @Step("GET /posts/{id}")
    public Response getPost(int id) {
        return given().spec(spec).when().get("/posts/{id}", id);
    }

    @Step("GET /posts?userId={userId}")
    public Response getPostsByUser(int userId) {
        return given().spec(spec).queryParam("userId", userId).when().get("/posts");
    }

    @Step("POST /posts")
    public Response createPost(Map<String, Object> body) {
        return given().spec(spec).body(body).when().post("/posts");
    }

    @Step("PUT /posts/{id}")
    public Response replacePost(int id, Map<String, Object> body) {
        return given().spec(spec).body(body).when().put("/posts/{id}", id);
    }

    @Step("DELETE /posts/{id}")
    public Response deletePost(int id) {
        return given().spec(spec).when().delete("/posts/{id}", id);
    }
}
