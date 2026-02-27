# language: es
@api @todo
Característica: : Gestión de tareas vía API REST
  Como usuario del sistema de tareas
  Quiero gestionar mis tareas mediante la API
  Para organizar mis actividades diarias

  Antecedentes:
    Dado la API de tareas está disponible

  @smoke @crear
  Escenario: Crear una tarea con título válido
    Dado creo una tarea con título "Comprar leche"
    Entonces la respuesta tiene código 201
    Y la tarea tiene título "Comprar leche"
    Y la tarea tiene estado "PENDING"

  @crear @negativo
  Escenario: : Crear tarea sin título retorna error
    Dado creo una tarea sin título
    Entonces la respuesta tiene código 400
    Y el mensaje de error contiene "titulo es obligatorio"

  @crear @negativo
  Escenario: Crear tarea con título mayor a 100 caracteres
    Dado creo una tarea con un título de 101 caracteres
    Entonces la respuesta tiene código 400
    Y el mensaje de error contiene "maximo 100 caracteres"

  @crear @outline
  Esquema del escenario: Crear tarea con diferentes datos
    Dado creo una tarea con título "<titulo>" y descripción "<desc>"
    Entonces la respuesta tiene código <codigo>
    Ejemplos:
      | titulo          | desc              | codigo |
      | Ir al gimnasio  | Rutina de piernas | 201    |
      | Estudiar BDD    |                   | 201    |
      |                 | Sin titulo        | 400    |

  @completar
  Escenario: Marcar tarea como completada
    Dado existe una tarea con título "Leer libro"
    Dado marco la tarea como completada
    Entonces la respuesta tiene código 200
    Y la tarea tiene estado "DONE"

  @eliminar
  Escenario: Eliminar una tarea existente
    Dado existe una tarea con título "Tarea temporal"
    Dado elimino la tarea
    Entonces la respuesta tiene código 204
    Y la tarea ya no existe en el sistema

  @eliminar @negativo
  Escenario: Eliminar tarea inexistente retorna 404
    Dado elimino una tarea con id 99999
    Entonces la respuesta tiene código 404

  @filtrar
  Escenario: Filtrar tareas por estado pendiente
    Dado existen las siguientes tareas:
      | titulo       | estado  |
      | Tarea uno    | PENDING |
      | Tarea dos    | DONE    |
      | Tarea tres   | PENDING |
    Dado consulto las tareas con filtro "PENDING"
    Entonces la respuesta tiene código 200
    Y se retornan 2 tareas
