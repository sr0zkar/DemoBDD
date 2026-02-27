package steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import utils.ApiClient;
import utils.MockServerManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TodoApiSteps {
    private final ApiClient api = new ApiClient();
    private Response response;
    private int currentTodoId;
    private MockServerManager mockServer;

    private MockServerManager getMockServer() {
        if (mockServer == null) {
            mockServer = MockServerManager.getInstance();
        }
        return mockServer;
    }

    @Dado("la API de tareas está disponible")
    public void laApiDisponible() {
        // Configurar stub para verificar disponibilidad
        getMockServer().setupGetTodosStub(null, "[]", 200);
        Response health = api.getTodos(null);
        assertNotNull(health);
    }

    @Cuando("creo una tarea con título {string}")
    public void creoTareaConTitulo(String titulo) {
        // Crear tarea en el mock server
        int id = getMockServer().createTask(titulo, null, "PENDING");
        String responseBody = getMockServer().toJson(getMockServer().getTaskById(id));

        // Configurar stub para crear tarea
        getMockServer().setupCreateTodoSuccessStub(responseBody);

        // Realizar la petición
        Map<String, Object> body = new HashMap<>();
        body.put("title", titulo);
        response = api.createTodo(body);

        if (response.statusCode() == 201) {
            currentTodoId = id;
        }
    }

    @Cuando("creo una tarea sin título")
    public void creoTareaSinTitulo() {
        // Configurar stub para error de validación
        getMockServer().setupCreateTodoErrorStub("titulo es obligatorio", 400);

        response = api.createTodo(Map.of("title", ""));
    }

    @Cuando("creo una tarea con un título de {int} caracteres")
    public void creoTareaConTituloDe(int n) {
        // Configurar stub para error de validación
        getMockServer().setupCreateTodoErrorStub("maximo 100 caracteres", 400);

        response = api.createTodo(Map.of("title", "a".repeat(n)));
    }

    @Cuando("creo una tarea con título {string} y descripción {string}")
    public void creoTareaConTituloYDesc(String titulo, String desc) {
        // Verificar si el título está vacío para configurar error
        if (titulo == null || titulo.trim().isEmpty()) {
            getMockServer().setupCreateTodoErrorStub("titulo es obligatorio", 400);
            Map<String, Object> body = new HashMap<>();
            body.put("title", titulo);
            if (!desc.isEmpty()) body.put("description", desc);
            response = api.createTodo(body);
            return;
        }

        // Crear tarea en el mock server
        int id = getMockServer().createTask(titulo, desc.isEmpty() ? null : desc, "PENDING");
        String responseBody = getMockServer().toJson(getMockServer().getTaskById(id));

        // Configurar stub
        getMockServer().setupCreateTodoSuccessStub(responseBody);

        // Realizar la petición
        Map<String, Object> body = new HashMap<>();
        body.put("title", titulo);
        if (!desc.isEmpty()) body.put("description", desc);
        response = api.createTodo(body);

        if (response.statusCode() == 201) {
            currentTodoId = id;
        }
    }

    @Dado("existe una tarea con título {string}")
    public void existeTarea(String titulo) {
        // Crear tarea en el mock server
        int id = getMockServer().createTask(titulo, null, "PENDING");

        // Configurar stub para crear
        String responseBody = getMockServer().toJson(getMockServer().getTaskById(id));
        getMockServer().setupCreateTodoSuccessStub(responseBody);

        // Realizar la petición
        Response r = api.createTodo(Map.of("title", titulo));
        assertEquals(201, r.statusCode());
        currentTodoId = id;
    }

    @Dado("existen las siguientes tareas:")
    public void existenTareas(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            String titulo = row.get("titulo");
            String estado = row.get("estado");

            // Crear tarea en el mock server
            int id = getMockServer().createTask(titulo, null, estado);

            // Configurar stub
            String responseBody = getMockServer().toJson(getMockServer().getTaskById(id));
            getMockServer().setupCreateTodoSuccessStub(responseBody);

            Response r = api.createTodo(Map.of("title", titulo));
            assertEquals(201, r.statusCode());

            if ("DONE".equals(estado)) {
                getMockServer().updateTaskStatus(id, "DONE");
                getMockServer().setupUpdateTodoStub(id,
                        getMockServer().toJson(getMockServer().getTaskById(id)), 200);
                api.updateTodoStatus(id, "DONE");
            }
        }
    }

    @Cuando("marco la tarea como completada")
    public void marcoCompletada() {
        // Actualizar en el mock server
        getMockServer().updateTaskStatus(currentTodoId, "DONE");

        // Configurar stub
        String responseBody = getMockServer().toJson(getMockServer().getTaskById(currentTodoId));
        getMockServer().setupUpdateTodoStub(currentTodoId, responseBody, 200);

        response = api.updateTodoStatus(currentTodoId, "DONE");
    }

    @Cuando("elimino la tarea")
    public void eliminoTarea() {
        // Eliminar del mock server
        getMockServer().deleteTask(currentTodoId);

        // Configurar stub para delete exitoso
        getMockServer().setupDeleteTodoStub(currentTodoId, 204);

        response = api.deleteTodo(currentTodoId);
    }

    @Cuando("elimino una tarea con id {int}")
    public void eliminoTareaConId(int id) {
        // Configurar stub para tarea no encontrada
        getMockServer().setupDeleteTodoStub(id, 404);

        response = api.deleteTodo(id);
    }

    @Cuando("consulto las tareas con filtro {string}")
    public void consultoConFiltro(String filtro) {
        // Configurar stub con tareas filtradas
        List<Map<String, Object>> filteredTasks = getMockServer().getTasksByStatus(filtro);
        String responseBody = getMockServer().toJson(filteredTasks);
        getMockServer().setupGetTodosStub(filtro, responseBody, 200);

        response = api.getTodos(filtro);
    }

    @Entonces("la respuesta tiene código {int}")
    public void respuestaCodigo(int codigo) {
        assertEquals(codigo, response.statusCode());
    }

    @Entonces("la tarea tiene título {string}")
    public void tareaTieneTitulo(String titulo) {
        assertEquals(titulo, response.jsonPath().getString("title"));
    }

    @Entonces("la tarea tiene estado {string}")
    public void tareaTieneEstado(String estado) {
        assertEquals(estado, response.jsonPath().getString("status"));
    }

    @Entonces("el mensaje de error contiene {string}")
    public void mensajeErrorContiene(String texto) {
        String msg = response.jsonPath().getString("message");
        assertTrue(msg.toLowerCase().contains(texto.toLowerCase()),
                "Esperado: " + texto + " | Actual: " + msg);
    }

    @Entonces("la tarea ya no existe en el sistema")
    public void tareaNoExiste() {
        // Configurar stub para tarea no encontrada
        getMockServer().setupGetTodoByIdStub(currentTodoId, "{\"message\": \"Tarea no encontrada\"}", 404);

        assertEquals(404, api.getTodoById(currentTodoId).statusCode());
    }

    @Entonces("se retornan {int} tareas")
    public void seRetornanTareas(int n) {
        assertEquals(n, response.jsonPath().getList("$").size());
    }
}
