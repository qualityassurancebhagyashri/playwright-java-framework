package dev.qa.tests.api;

import dev.qa.clients.PostsClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@Epic("API")
@Feature("Posts (REST-Assured)")
public class RestAssuredPostsTest {

    private final PostsClient posts = new PostsClient();

    @Test(groups = "smoke", description = "GET a post returns the expected fields")
    @Story("Fetch a single post")
    public void getPostReturnsExpectedFields() {
        posts.getPost(1).then()
                .statusCode(200)
                .body("id", equalTo(1),
                        "title", not(emptyOrNullString()),
                        "userId", greaterThan(0));
    }

    @Test(groups = "regression", description = "Filtering by user returns only that user's posts")
    @Story("Filter posts")
    public void filteringByUserReturnsOnlyThatUsersPosts() {
        posts.getPostsByUser(1).then()
                .statusCode(200)
                .body("size()", equalTo(10))
                .body("userId", everyItem(equalTo(1)));
    }

    @Test(groups = "smoke", description = "Creating a post echoes the payload and returns 201")
    @Story("Create a post")
    public void createPostEchoesPayloadAndReturns201() {
        posts.createPost(Map.of("title", "qa", "body", "hello", "userId", 7)).then()
                .statusCode(201)
                .body("title", equalTo("qa"),
                        "body", equalTo("hello"),
                        "userId", equalTo(7),
                        "id", notNullValue());
    }

    @Test(groups = "regression", description = "Replacing a post returns the updated fields")
    @Story("Update a post")
    public void replacePostReturnsUpdatedFields() {
        posts.replacePost(1, Map.of("id", 1, "title", "updated", "body", "new body", "userId", 1)).then()
                .statusCode(200)
                .body("title", equalTo("updated"),
                        "body", equalTo("new body"));
    }

    @Test(groups = "regression", description = "Deleting a post returns 200")
    @Story("Delete a post")
    public void deletePostReturns200() {
        posts.deletePost(1).then().statusCode(200);
    }

    @Test(groups = "regression", description = "Unknown post returns 404")
    @Story("Unknown post")
    public void unknownPostReturns404() {
        posts.getPost(99999).then().statusCode(404);
    }
}
