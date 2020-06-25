# Respaldomysql
Script en java para realizar un respaldo sql de uan base de datos en mariadb o mysql y mostrar una notificacion en el sistema.


### PARA CONFIGURAR DE LADO SERVIDOR WINDOWS

Agregar los binarios de mariadb o mysql a las variables de entorno para el usuario con el fin de poder usar desde la terminal el comando mysqldump.

Para ello agregar la sigueite ruta al path

    C:\Program Files\MariaDB 10.4\bin

### PARA OPTIMIZAR EL PROCESO DE RESPALDO

Programar una tarea semanal para que ejecute el ejecutable respaldo.jar para ello se puede usar el programdor de tareas de windows.

### CONFIGURACION DEL SCRIPT

Configurar las cedenciales de acceso a la base de datos.

	protected String user = "username";
    protected String password = "userpass";
    protected String database = "dbname";

Configurar la ruta donde se va a guardar el archivo SQL con el respaldo de la base de datos aqui se peude usar **$usuariolog** y **$fechaactual** para obetner el usuario actual y la fecha actual respectivamente.

    private String ruta
            = "C:\\Users\\$usuariolog\\Documents\\mysql\\$fechaactual.sql";

En el ejemplo anterior  se crea un directorio mysql en los documentos del usuario y se crea un archivo cuyo nombre es la fecha actual y la extension sql.

##### COMPILACION
Despues de haber modificado las variables, en el directorio raiz **respaldomysql** y ejecutar el siquiente comando para compilar la clase principal.

	javac respaldo/Respaldo.java

##### CREAR .JAR
	jar cmf Manifest.mf respaldo.jar .\respaldo\*class

**Nota:** si desea modificar el archivo Manifest.mf se debe guardar con formato unix.
Esto se puede hacer en Windows usado Notepad++
Editar > Fin de linea > Formato Unix .


##### EJECUTAR SIN .JAR
	java -cp . respaldo.Respaldo
