package utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

public class ApiClient {
    private final String baseUrl;
    private final String basePath;

    public ApiClient() {
        this.baseUrl = ConfigManager.get("base.url", "http://localhost:8080");
        this.basePath = ConfigManager.get("api.base.path", "/api/todos");
    }

    private RequestSpecification baseRequest() {
        return RestAssured.given()
                .baseUri(baseUrl).basePath(basePath)
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .log().ifValidationFails();
    }

    public Response createTodo(Map<String, Object> body) {
        return baseRequest().body(body).post();
    }

    public Response getTodos(String statusFilter) {
        RequestSpecification req = baseRequest();
        if (statusFilter != null && !statusFilter.isEmpty())
            req.queryParam("status", statusFilter);
        return req.get();
    }

    public Response getTodoById(int id) {
        return baseRequest().get("/" + id);
    }

    public Response updateTodoStatus(int id, String status) {
        return baseRequest().body(Map.of("status", status)).patch("/" + id);
    }

    public Response deleteTodo(int id) {
        return baseRequest().delete("/" + id);
    }
}
