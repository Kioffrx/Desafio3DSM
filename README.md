# AprendeApp

Aplicación Android en Kotlin para gestionar recursos de aprendizaje (libros, videos, artículos y tutoriales de Ingeniería de Sistemas). Proyecto del Tercer Desafío Práctico de Desarrollo de Software para Móviles (DSM), Universidad Don Bosco.

**Autor:** Jose Enrique Rodriguez Zometa
**Carnet:** RZ242333

## Funcionalidades

- Registro de usuarios con validación segura de contraseña, login y logout con sesión persistente.
- Dos roles: **Estudiante** y **Docente**.
- **Estudiante:** ver los recursos en tarjetas, buscar por ID, título o tipo, filtrar por tipo, ordenar, marcar favoritos y calificar de 1 a 5 estrellas.
- **Docente:** pantalla de administración con CRUD completo: agregar, editar (formulario prellenado) y eliminar con confirmación.
- **Calificaciones:** el promedio se guarda en el backend, por lo que todos los usuarios ven el mismo valor.
- **Favoritos:** se guardan localmente por usuario y pueden verse en una lista filtrada.
- **Feedback visual:** indicadores de carga, toasts, diálogos de confirmación y mensajes de error por campo.

## Validación de contraseña

La contraseña debe cumplir todas estas reglas:

- Mínimo 12 caracteres.
- Al menos una letra mayúscula.
- Al menos una letra minúscula.
- Al menos un número.
- Al menos un carácter especial (`! @ # $ % ^ & *`).

La contraseña nunca se guarda en texto plano: se almacena como hash SHA-256.

## Arquitectura MVC

| Capa | Contenido |
|---|---|
| **model** | Data classes (`Usuario`, `Recurso`, `Rol`), `ApiService`, `RetrofitClient` y los repositorios. |
| **view** | Activities, adaptador del RecyclerView y layouts XML. Solo muestran datos y capturan eventos. |
| **controller** | `AuthController`, `RecursoController`, `SessionManager` y `DatosUsuarioManager`. Contienen la lógica, las validaciones y el filtrado. |

Todos los textos de la interfaz están centralizados en `strings.xml`.

## Tecnologías

- Kotlin y Android Studio
- Retrofit + Gson (consumo de la API)
- Coroutines (llamadas asíncronas)
- Glide (carga de imágenes)
- Material Components / Material 3 (interfaz)
- ViewBinding y RecyclerView
- SharedPreferences (sesión, favoritos y calificaciones del usuario)
- MockAPI.io (backend)

## API (MockAPI.io)

Base: `https://6ae3cef0933120ae20f932e7.mockapi.io/api/v1/`

| Recurso | Campos |
|---|---|
| `Usuarios` | id, nombre, email, passwordHash, rol |
| `recursos` | id, titulo, descripcion, tipo, enlace, imagen, sumaCalificaciones, totalVotos |

Operaciones usadas sobre `recursos`:

| Método | Ruta | Uso |
|---|---|---|
| GET | `/recursos` | Listar recursos |
| GET | `/recursos/:id` | Obtener un recurso |
| POST | `/recursos` | Agregar un recurso (Docente) |
| PUT | `/recursos/:id` | Editar un recurso (Docente) y actualizar la calificación |
| DELETE | `/recursos/:id` | Eliminar un recurso (Docente) |

El promedio de calificación se calcula como `sumaCalificaciones / totalVotos`.

## Estructura del proyecto

```
app/src/main/java/com/udb/aprendeapp/
├── model/        Rol, Usuario, Recurso, ApiService, RetrofitClient,
│                 UsuarioRepository, RecursoRepository
├── controller/   PasswordValidator, HashUtil, SessionManager, AuthController,
│                 Resultado, DatosUsuarioManager, RecursoController
└── view/         LoginActivity, RegisterActivity, HomeActivity, RecursoAdapter,
                  AdminRecursosActivity, FormularioRecursoActivity
```

## Cómo ejecutar

1. Clonar el repositorio: `git clone https://github.com/Kioffrx/Desafio3DSM.git`
2. Abrirlo en Android Studio y esperar la sincronización de Gradle.
3. Ejecutar en un emulador o dispositivo con conexión a internet.
4. Registrar un usuario **Estudiante** y otro **Docente** para probar ambos roles. Ejemplo de contraseña válida: `Aprende2026!Udb`.

## Pruebas manuales realizadas

- Registro con contraseña débil (muestra las reglas que faltan) y con correo repetido.
- Login con credenciales incorrectas y sesión que persiste al reabrir la app.
- Cierre de sesión con confirmación.
- Búsqueda por ID, título y tipo; filtro por tipo; ordenamiento.
- Favoritos y lista de solo favoritos.
- Calificación de recursos y actualización del promedio.
- Como Docente: agregar, editar y eliminar recursos con validación y confirmación.


## Video de defensa
[https://drive.google.com/file/d/11LeLiYXnYYx88JbALIeXnzIPuJzAXJLn/view?usp=sharing]
