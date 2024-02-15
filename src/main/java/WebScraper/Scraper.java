/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package WebScraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mario
 */
public class Scraper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String direccionWeb = "http://localhost:5000";
        ascii();
        System.out.println("Escribe un nombre.");
        String nombre = sc.nextLine();
        urlGET(direccionWeb, nombre);
    }

    private static void urlGET(String direccionWeb, String nombre) {
        try {
            URI uri = new URI(direccionWeb);
            URL url = uri.toURL();
            procesarSolicitud(url, nombre);
        } catch (URISyntaxException | MalformedURLException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void procesarSolicitud(URL url, String nombre) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Map<String, List<String>> headers = leerHeader(urlConnection);
            String contentType = urlConnection.getContentType();
            int contentLength = urlConnection.getContentLength();
            StringBuffer body = leerBody(urlConnection);
//            imprimirResultadoLeido(headers, contentType, contentLength, body.toString());
            extraerDatosTabla(body.toString(), nombre);
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Map<String, List<String>> leerHeader(HttpURLConnection urlConnection) {
        Map<String, List<String>> headersResponse = urlConnection.getHeaderFields();
        return headersResponse;
    }

    private static StringBuffer leerBody(HttpURLConnection urlConnection) {
        BufferedReader bufferedReader = null;
        try {
            String linea;
            StringBuffer stringBuffer = new StringBuffer();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((linea = bufferedReader.readLine()) != null) {
                stringBuffer.append(linea);
            }
            return stringBuffer;
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static void extraerDatosTabla(String body, String nombre) {
        Pattern pattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);

        int ganadas = 0;
        int perdidas = 0;

        while (matcher.find()) {
            String fila = matcher.group(1); // Obtener el contenido de la fila entre <tr> y </tr>
//            System.out.println(fila);
            fila = fila.replaceAll("<[^>]*>", " ");
//            System.out.println(fila);
            if (fila.contains(nombre)) {
                // Dividir el string en palabras
                String[] words = fila.split("\\s+");
                // Obtener la última palabra
                String lastWord = words[words.length - 1];
//                System.out.println("--" + fila);
//                System.out.println("+"+lastWord);
                if (lastWord.equals(nombre)) {
                    ganadas++;
                } else {
                    perdidas++;
                }
            }
        }

        System.out.println("Cantidad de partidas Ganadas: " + ganadas);
        System.out.println("Cantidad de partidas Perdidas: " + perdidas);

    }

    private static void imprimirResultadoLeido(Map<String, List<String>> headers, String contentType, int contentLength, String body) {
        System.out.println("Todos los headers leidos");
        System.out.println(headers);

        // Imprimir el tipo de contenido y la longitud del contenido de la respuesta HTTP
        System.out.println("Header Content-Type");
        System.out.println(contentType);
        System.out.println("Header Content-Length");
        System.out.println(contentLength);

        // Imprimir el cuerpo de la respuesta HTTP
        System.out.println("Body Leido:");
        System.out.println(body);

        // Eliminar todas las etiquetas HTML del cuerpo de la respuesta
        body = body.replaceAll("<[^>]*>", "");
        System.out.println("Body Leido eliminando todas las etiquetas, nos queda solo el 'contents':");
    }

    private static void ascii() {
        System.out.println("    ________\n"
                + "   /_______/\\\n"
                + "   \\ \\    / /\n"
                + " ___\\ \\__/_/___\n"
                + "/____\\ \\______/\\\n"
                + "\\ \\   \\/ /   / /\n"
                + " \\ \\  / /\\  / /\n"
                + "  \\ \\/ /\\ \\/ /\n"
                + "   \\_\\/  \\_\\/");
    }
}
