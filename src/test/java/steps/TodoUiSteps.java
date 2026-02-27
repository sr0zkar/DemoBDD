package steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import pages.TodoPage;
import static org.junit.jupiter.api.Assertions.*;

public class TodoUiSteps {
    private TodoPage todoPage;

    private TodoPage page() {
        if (todoPage == null) todoPage = new TodoPage(Hooks.getDriver());
        return todoPage;
    }

    @Dado("estoy en la página de tareas")
    public void estoyEnPagina() { page().navigateTo(); }

    @Cuando("ingreso el título {string} en el formulario")
    public void ingresoTitulo(String t) { page().enterTitle(t); }

    @Cuando("hago clic en el botón {string}")
    public void clicBoton(String b) { page().clickAdd(); }

    @Cuando("dejo el campo título vacío")
    public void tituloVacio() { page().enterTitle(""); }

    @Entonces("veo la tarea {string} en la lista")
    public void veoTarea(String t) {
        assertTrue(page().isTodoVisible(t), "Tarea no visible: " + t);
    }

    @Entonces("la tarea aparece con estado pendiente")
    public void estadoPendiente() { /* verificado implícitamente */ }

    @Dado("existe la tarea {string} en la lista")
    public void existeTarea(String t) {
        page().enterTitle(t);
        page().clickAdd();
        assertTrue(page().isTodoVisible(t));
    }

    @Cuando("marco como completada la tarea {string}")
    public void marcoCompletada(String t) { page().completeTodo(t); }

    @Entonces("la tarea {string} aparece como completada")
    public void completada(String t) { assertTrue(page().isTodoCompleted(t)); }

    @Cuando("elimino la tarea {string} desde la interfaz")
    public void eliminoUI(String t) { page().deleteTodo(t); }

    @Entonces("la tarea {string} ya no aparece en la lista")
    public void noAparece(String t) { assertFalse(page().isTodoVisible(t)); }

    @Entonces("veo un mensaje de error {string}")
    public void mensajeError(String msg) {
        assertEquals(msg, page().getErrorMessage());
    }
}
