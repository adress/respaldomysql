package respaldo;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Este archivo crea un respado de una base de datos 
 * mediante la extracion de su contenido sql
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

    /* 
     * ruta donde se guardara el backup
     * *usuariolog* => usuario con session iniciada
     * *fechaactual* => fecha del sistema formato dd-MM-yyyy
     */
    private String ruta
            = "C:\\Users\\*usuariolog*\\Documents\\mysql\\*fechaactual*.sql";

    /*
     * cantidad minima de lieneas que debe tener la 
     * respuesta del proceso para que se considere correcto
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

    public static void main(String[] args) {
        Respaldo respado = new Respaldo();
    }

    /*
     * remplaza *usuariolog* por el nombre del usuario que tiene la session iniciada,
     * remplaza *fechaactual* por la feha actual del sistema con formato dd-MM-yyyy
     */
    public String remplazarString(String texto) {
        Date date = new Date();
        String strDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String userLog = System.getProperty("user.name");
        return texto.replace("*usuariolog*", userLog).replace("*fechaactual*", strDate);
    }

    // si NO exiten crea los directorios 
    private boolean comprobar_dir() {
        String path = getPath(ruta);
        File file = new File(path);
        boolean isOk = true; //bandera si puede crear los directorios
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

    //muestra una notificacion en el sistema
    private void mensaje(int mensaje) {
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
            switch (mensaje) {
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

    //retorna el path de una ruta
    private String getPath(String ruta) {
        return ruta.substring(0, ruta.lastIndexOf(File.separator));
    }

    //retrna el nombre del archivo con la extension
    private String getFileName(String ruta) {
        return ruta.substring(ruta.lastIndexOf(File.separator) + 1, ruta.length());
    }

    //crea el archivo con el respaldo de la base de datos
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
            //cambia el nombre del archvo y le pone el prefijo fail-
            String nuevoName = getPath(ruta) + File.separator + "fail-" + getFileName(ruta);
            File file = new File(ruta);
            file.renameTo(new File(nuevoName));
        } else {
            mensaje(MENSAJE_CORRECTO);
        }
    }
}
