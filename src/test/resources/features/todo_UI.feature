# language: es
@ui @todo
Característica: Gestión de tareas desde la interfaz web
  Como usuario registrado
  Quiero gestionar tareas desde el navegador

  Antecedentes:
    Dado estoy en la página de tareas

  @smoke @ui-crear
  Escenario: Crear tarea desde la interfaz
    Dado ingreso el título "Comprar leche" en el formulario
    Y hago clic en el botón "Agregar"
    Entonces veo la tarea "Comprar leche" en la lista
    Y la tarea aparece con estado pendiente

  @ui-completar
  Escenario: Completar tarea desde la interfaz
    Dado existe la tarea "Estudiar Java" en la lista
    Dado marco como completada la tarea "Estudiar Java"
    Entonces la tarea "Estudiar Java" aparece como completada

  @ui-eliminar
  Escenario: Eliminar tarea desde la interfaz
    Dado existe la tarea "Tarea temporal" en la lista
    Dado elimino la tarea "Tarea temporal" desde la interfaz
    Entonces la tarea "Tarea temporal" ya no aparece en la lista

  @ui-validacion
  Escenario: Validación de título vacío en interfaz
    Dado dejo el campo título vacío
    Y hago clic en el botón "Agregar"
    Entonces veo un mensaje de error "El título es obligatorio"
