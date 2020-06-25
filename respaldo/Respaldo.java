package respaldo;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Este script crea un respado de una base de datos 
 * mediante la extracion de su contenido generando un archivo sql
 */

 /* 
 * Created on : May 21, 2020.
 * Author     : Adress  https://github.com/adress
 */
public class Respaldo {

    // credenciales de la base de datos
    protected String user = "username";
    protected String password = "userpass";
    protected String database = "dbname";

    /**
     * Ruta donde se guardara el backup. $usuariolog => usuario con session
     * iniciada $fechaactual => fecha del sistema formato dd-MM-yyyy
     */
    private String ruta
            = "C:\\Users\\$usuariolog\\Documents\\mysql\\$fechaactual.sql";

    /**
     * Prefijo para anteponer al nombre del archivo cuando falla la generacion
     * de este
     */
    private String prefijo_error = "fail-";

    /**
     * Cantidad minima de lieneas que debe tener la respuesta del proceso para
     * que se considere correcto
     */
    private static final int MIN_LINEAS = 5;

    //constantes que indican el tipo de notificacion
    private static final int MENSAJE_CORRECTO = 1;
    private static final int MENSAJE_ERROR = 2;

    public Respaldo() {
        ruta = remplazarString(ruta);
        if (comprobar_dir()) {
            try {
                backup();
                System.out.println(ruta);
            } catch (IOException ex) {
                mensaje(MENSAJE_ERROR);
                Logger.getLogger(Respaldo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                System.exit(0);
            }
        }
    }

    /**
     * Inicio del script
     *
     * @param args
     */
    public static void main(String[] args) {
        Respaldo respado = new Respaldo();
    }

    /**
     * Remplaza $usuariolog por el nombre del usuario que tiene la session
     * iniciada, remplaza $fechaactual por la feha actual del sistema con
     * formato dd-MM-yyyy
     */
    public String remplazarString(String texto) {
        Date date = new Date();
        String strDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String userLog = System.getProperty("user.name");
        return texto.replace("$usuariolog", userLog).replace("$fechaactual", strDate);
    }

    /**
     * Crea los directorios de la ruta si no existen
     *
     * @return isOk indica si los directorios de la ruta existen.
     */
    private boolean comprobar_dir() {
        boolean isOk = true; //bandera si puede crear los directorios
        String path = getPath(ruta);
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                isOk = false;
            }
        }
        if (!isOk) {
            System.out.println("Error al crear los directorios, revisa la ruta");
        }
        return isOk;
    }

    /**
     * Muestra una notificacion en el sistema
     *
     * @param tipoMensaje tipo de mensaje (MENSAJE_CORRECTO, MENSAJE_ERROR)
     */
    private void mensaje(int tipoMensaje) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            // imagne del icono que aparece en la bandeja del sistema
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Java AWT Tray Demo");
            // Deja que el sistema auto escale si es necesario
            trayIcon.setImageAutoSize(true);
            // define el tooltip del icono
            trayIcon.setToolTip("System tray icon demo");
            tray.add(trayIcon);
            // Mostrar notificacion de informacion:
            switch (tipoMensaje) {
                case MENSAJE_CORRECTO:
                    trayIcon.displayMessage("Respaldo realizado",
                            "El respado de la base de datos se ha realizado correctamente", MessageType.INFO);
                    break;
                case MENSAJE_ERROR:
                    // Error
                    trayIcon.displayMessage("Respaldo fallido",
                            "El respado de la base de datos fallo revise los datos", MessageType.ERROR);
                    break;
                default:
                    break;
            }
        } catch (AWTException ex) {
            System.err.println("Error al mostrar notificacion");
            Logger.getLogger(Respaldo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Obtiene el path de una ruta sin el nombre del archivo
     *
     * @param ruta para obetner el path
     * @return path de una ruta
     */
    private String getPath(String ruta) {
        return ruta.substring(0, ruta.lastIndexOf(File.separator));
    }

    /**
     * Obtiene el nombre de un archivo apartir de su ruta
     *
     * @param ruta
     * @return nombre del archivo suministrado en la ruta
     */
    private String getFileName(String ruta) {
        return ruta.substring(ruta.lastIndexOf(File.separator) + 1, ruta.length());
    }

    /**
     * Crea el archivo con el respaldo de la base de datos
     *
     * @throws IOException Erro al crear el archivo con el respaldo
     */
    private void backup() throws IOException {
        // contador de lineas
        int contadorLineas = 0;

        //conexion de la base de datos con el servicio mysqldump
        Process proceso = Runtime.getRuntime().exec("mysqldump -u " + user + " -p" + password + " " + database);

        //guarda la salida del comando en el archivo de la ruta
        InputStream is = proceso.getInputStream();
        FileOutputStream fos = new FileOutputStream(ruta);
        byte[] buffer = new byte[1000];
        int leido = is.read(buffer);
        while (leido > 0) {
            fos.write(buffer, 0, leido);
            leido = is.read(buffer);
            contadorLineas++;
        }
        fos.close();

        if (contadorLineas < MIN_LINEAS) {
            mensaje(MENSAJE_ERROR);
            //cambia el nombre del archvo y le pone el prefijo de error
            String nuevoName = getPath(ruta) + File.separator + prefijo_error + getFileName(ruta);
            File file = new File(ruta);
            file.renameTo(new File(nuevoName));
        } else {
            mensaje(MENSAJE_CORRECTO);
        }
    }
}
