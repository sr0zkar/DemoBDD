package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Gestor del servidor mock para simular la API de tareas.
 * Implementa un comportamiento stateful para soportar operaciones CRUD.
 */
public class MockServerManager {
    private static MockServerManager instance;
    private WireMockServer wireMockServer;
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final Map<Integer, Map<String, Object>> tasks = new ConcurrentHashMap<>();
    private int port;

    private MockServerManager() {}

    public static synchronized MockServerManager getInstance() {
        if (instance == null) {
            instance = new MockServerManager();
        }
        return instance;
    }

    /**
     * Inicia el servidor mock en el puerto especificado.
     */
    public void start(int port) {
        this.port = port;
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        System.out.println("Mock Server iniciado en puerto: " + port);
    }

    /**
     * Detiene el servidor mock.
     */
    public void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            tasks.clear();
            idGenerator.set(1);
            System.out.println("Mock Server detenido");
        }
    }

    /**
     * Limpia todas las tareas (para usar entre tests).
     */
    public void reset() {
        tasks.clear();
        idGenerator.set(1);
        wireMockServer.resetMappings();
        wireMockServer.resetRequests();
    }

    /**
     * Obtiene el puerto del servidor.
     */
    public int getPort() {
        return port;
    }

    /**
     * Obtiene la URL base del servidor.
     */
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Configura stubs para un escenario específico.
     */
    public void setupGetTodosStub(String statusFilter, String responseBody, int status) {
        if (statusFilter != null && !statusFilter.isEmpty()) {
            stubFor(WireMock.get(urlPathEqualTo("/api/todos"))
                    .withQueryParam("status", equalTo(statusFilter))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseBody)
                            .withStatus(status)));
        } else {
            stubFor(WireMock.get(urlPathEqualTo("/api/todos"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseBody)
                            .withStatus(status)));
        }
    }

    /**
     * Configura stub para crear tarea exitosamente.
     */
    public void setupCreateTodoSuccessStub(String responseBody) {
        stubFor(WireMock.post(urlPathEqualTo("/api/todos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                        .withStatus(201)));
    }

    /**
     * Configura stub para error de validación.
     */
    public void setupCreateTodoErrorStub(String errorMessage, int status) {
        stubFor(WireMock.post(urlPathEqualTo("/api/todos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"" + errorMessage + "\"}")
                        .withStatus(status)));
    }

    /**
     * Configura stub para obtener tarea por ID.
     */
    public void setupGetTodoByIdStub(int id, String responseBody, int status) {
        stubFor(WireMock.get(urlPathEqualTo("/api/todos/" + id))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                        .withStatus(status)));
    }

    /**
     * Configura stub para actualizar tarea.
     */
    public void setupUpdateTodoStub(int id, String responseBody, int status) {
        stubFor(WireMock.patch(urlPathEqualTo("/api/todos/" + id))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                        .withStatus(status)));
    }

    /**
     * Configura stub para eliminar tarea.
     */
    public void setupDeleteTodoStub(int id, int status) {
        stubFor(WireMock.delete(urlPathEqualTo("/api/todos/" + id))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(status == 204 ? "" : "{\"message\": \"Tarea no encontrada\"}")
                        .withStatus(status)));
    }

    /**
     * Configura stub para la página HTML de la UI.
     */
    public void setupUiStub() {
        stubFor(WireMock.get(urlPathEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withBody(getTodoHtmlPage())
                        .withStatus(200)));
    }

    // ================== Métodos de gestión de tareas ==================

    /**
     * Crea una tarea en el almacenamiento interno.
     */
    public int createTask(String titulo, String descripcion, String status) {
        int id = idGenerator.getAndIncrement();
        Map<String, Object> task = new ConcurrentHashMap<>();
        task.put("id", id);
        task.put("title", titulo);
        task.put("titulo", titulo);
        task.put("description", descripcion != null ? descripcion : "");
        task.put("descripcion", descripcion != null ? descripcion : "");
        task.put("status", status != null ? status : "PENDING");
        tasks.put(id, task);
        return id;
    }

    /**
     * Obtiene todas las tareas.
     */
    public List<Map<String, Object>> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Obtiene tareas filtradas por estado.
     */
    public List<Map<String, Object>> getTasksByStatus(String status) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> task : tasks.values()) {
            if (status.equals(task.get("status"))) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    /**
     * Obtiene una tarea por ID.
     */
    public Map<String, Object> getTaskById(int id) {
        return tasks.get(id);
    }

    /**
     * Actualiza el estado de una tarea.
     */
    public boolean updateTaskStatus(int id, String status) {
        Map<String, Object> task = tasks.get(id);
        if (task != null) {
            task.put("status", status);
            return true;
        }
        return false;
    }

    /**
     * Elimina una tarea.
     */
    public boolean deleteTask(int id) {
        return tasks.remove(id) != null;
    }

    // ================== Métodos de conversión JSON ==================

    public String toJson(List<Map<String, Object>> taskList) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> task : taskList) {
            if (!first) sb.append(",");
            first = false;
            sb.append(toJson(task));
        }
        sb.append("]");
        return sb.toString();
    }

    public String toJson(Map<String, Object> task) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : task.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Página HTML simple para simular la UI de tareas.
     */
    private String getTodoHtmlPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Lista de Tareas</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                "        .task-list { margin-top: 20px; }\n" +
                "        .todo-item { padding: 10px; border: 1px solid #ccc; margin: 5px 0; }\n" +
                "        .todo-item.completed { background-color: #d4edda; text-decoration: line-through; }\n" +
                "        .error-message { color: red; }\n" +
                "        input[type=\"text\"] { padding: 8px; width: 300px; }\n" +
                "        button { padding: 8px 15px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Lista de Tareas</h1>\n" +
                "    <div id=\"task-form\">\n" +
                "        <input type=\"text\" id=\"todo-title-input\" placeholder=\"Nueva tarea\">\n" +
                "        <button id=\"add-todo-btn\">Agregar</button>\n" +
                "        <p class=\"error-message\" style=\"display:none;\"></p>\n" +
                "    </div>\n" +
                "    <div id=\"task-list\" class=\"task-list\"></div>\n" +
                "    \n" +
                "    <script>\n" +
                "        const tasks = [];\n" +
                "        let taskIdCounter = 1;\n" +
                "        \n" +
                "        function renderTasks() {\n" +
                "            const list = document.getElementById('task-list');\n" +
                "            list.innerHTML = '';\n" +
                "            tasks.forEach(task => {\n" +
                "                const div = document.createElement('div');\n" +
                "                div.className = 'todo-item' + (task.status === 'DONE' ? ' completed' : '');\n" +
                "                div.innerHTML = `\n" +
                "                    <input type=\"checkbox\" onchange=\"toggleTask(${task.id})\" ${task.status === 'DONE' ? 'checked' : ''}>\n" +
                "                    <span>${task.titulo}</span>\n" +
                "                    <button class=\"delete-btn\" onclick=\"deleteTask(${task.id})\">Eliminar</button>\n" +
                "                `;\n" +
                "                list.appendChild(div);\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        function addTask() {\n" +
                "            const tituloInput = document.getElementById('todo-title-input');\n" +
                "            const titulo = tituloInput.value.trim();\n" +
                "            const errorEl = document.querySelector('.error-message');\n" +
                "            \n" +
                "            if (!titulo) {\n" +
                "                errorEl.textContent = 'El título es obligatorio';\n" +
                "                errorEl.style.display = 'block';\n" +
                "                return;\n" +
                "            }\n" +
                "            \n" +
                "            errorEl.style.display = 'none';\n" +
                "            tasks.push({ id: taskIdCounter++, titulo: titulo, status: 'PENDING' });\n" +
                "            tituloInput.value = '';\n" +
                "            renderTasks();\n" +
                "        }\n" +
                "        \n" +
                "        function toggleTask(id) {\n" +
                "            const task = tasks.find(t => t.id === id);\n" +
                "            if (task) {\n" +
                "                task.status = task.status === 'PENDING' ? 'DONE' : 'PENDING';\n" +
                "                renderTasks();\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        function deleteTask(id) {\n" +
                "            const index = tasks.findIndex(t => t.id === id);\n" +
                "            if (index > -1) {\n" +
                "                tasks.splice(index, 1);\n" +
                "                renderTasks();\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        document.getElementById('add-todo-btn').addEventListener('click', addTask);\n" +
                "        document.getElementById('todo-title-input').addEventListener('keypress', (e) => {\n" +
                "            if (e.key === 'Enter') addTask();\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
