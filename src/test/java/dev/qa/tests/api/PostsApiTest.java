package dev.qa.tests.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import dev.qa.core.BaseApiTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** API tests use Playwright's request client, so one dependency covers UI and API. */
@Epic("API")
@Feature("Posts")
public class PostsApiTest extends BaseApiTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test(groups = "smoke", description = "GET a post returns the expected fields")
    @Story("Fetch a single post")
    public void getPostReturnsExpectedFields() throws Exception {
        APIResponse res = api().get("/posts/1");

        assertThat(res.status()).isEqualTo(200);
        JsonNode body = JSON.readTree(res.text());
        assertThat(body.get("id").asInt()).isEqualTo(1);
        assertThat(body.get("title").asText()).isNotBlank();
        assertThat(body.get("userId").asInt()).isPositive();
    }

    @Test(groups = "smoke", description = "POST a post echoes the payload with 201")
    @Story("Create a post")
    public void createPostEchoesPayloadAndReturns201() throws Exception {
        APIResponse res = api().post("/posts", RequestOptions.create()
                .setData(Map.of("title", "qa", "body", "hello", "userId", 7)));

        assertThat(res.status()).isEqualTo(201);
        JsonNode body = JSON.readTree(res.text());
        assertThat(body.get("title").asText()).isEqualTo("qa");
        assertThat(body.get("userId").asInt()).isEqualTo(7);
    }

    @Test(groups = "smoke", description = "Unknown post returns 404")
    @Story("Unknown post")
    public void unknownPostReturns404() {
        assertThat(api().get("/posts/99999").status()).isEqualTo(404);
    }
}
