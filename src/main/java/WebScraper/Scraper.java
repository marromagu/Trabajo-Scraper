/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package WebScraper;

import java.io.BufferedReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private static String nombre;
    private static int jugadas;
    private static int ganadas;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String direccionWeb = "http://localhost:5000/GET";
        ascii();
        System.out.println("Escribe un nombre.");
        nombre = sc.nextLine();
        urlGET(direccionWeb);
    }

    private static void urlGET(String direccionWeb) {
        try {
            URI uri = new URI(direccionWeb);
            URL url = uri.toURL();
            System.out.println(url);
            procesarSolicitud(url);
        } catch (URISyntaxException | MalformedURLException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void procesarSolicitud(URL url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            StringBuffer body = leerBody(urlConnection);
            String action = extraerFormAction(body.toString());
            String selectName = extraerSelectName(body.toString());
            ArrayList<String> opciones = extraerOpciones(body.toString());
            System.out.println(opciones.toString());
            List<String> partidasJugadas = new ArrayList<>();
            List<String> partidasGanadas = new ArrayList<>();

            for (String op : opciones) {
                String respuesta = conexionGET(action + "?" + selectName + "=" + op);
                System.out.println(action + "?" + selectName + "=" + op);
                partidasJugadas.add(obtenerPartidasJugadas(respuesta, nombre));
                partidasGanadas.add(obtenerPartidasGanadas(respuesta, nombre));
            }
            System.out.println("Partidas jugadas por " + nombre + ": " + jugadas);
            System.out.println("Partidas ganadas por " + nombre + ": " + ganadas);

        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    // Metodo para extraer la accion del formulario
    private static String extraerFormAction(String body) {
        // Expresión regular para encontrar un valor.
        String regex = "<form[^>]*action=\"([^\"]+)\"[^>]*>";// También conocidas como regex, son patrones de búsqueda que se utilizan para encontrar coincidencias dentro de cadenas de texto.

        // Compilar la expresión regular en un patrón
        Pattern pattern = Pattern.compile(regex);//Se utiliza para representar una expresión regular compilada.

        // Crear un matcher para el cuerpo del HTML
        Matcher matcher = pattern.matcher(body);//Es una clase que se encarga de realizar la búsqueda de la expresión regular en una cadena de texto específica.

        // Si se encuentra una coincidencia, extraer el valor del atributo "action"
        if (matcher.find()) {//Este método busca la siguiente coincidencia de la expresión regular en la cadena de texto analizada
            return matcher.group(1);//Se utiliza para obtener el texto que coincide con el primer grupo capturado por la expresión regular
        } else {
            return "";
        }
        /**
         * *
         * Los grupos capturados por una expresión regular son partes
         * específicas de la cadena que coinciden con ciertas partes del patrón
         * definido en la expresión regular. Los paréntesis (( y )) se utilizan
         * para definir grupos en una expresión regular.
         */
    }

    private static String extraerSelectName(String body) {
        String regex = "<select[^>]*name=\"([^\"]+)\"[^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    //Necesita implemtnear la dependecias de org.jsoup
    private static ArrayList<String> extraerOpciones(String body) {
        ArrayList<String> opciones = new ArrayList<>();
        Document doc = Jsoup.parse(body);

        // Encuentra todos los elementos select
        Elements selects = doc.select("select");

        // Itera sobre los elementos select
        for (Element select : selects) {
            // Encuentra todas las opciones dentro del select actual
            Elements options = select.select("option");

            // Itera sobre las opciones y extrae sus valores
            for (Element option : options) {
                String optionValue = option.attr("value");
                opciones.add(optionValue);
            }
        }

        return opciones;
    }

    private static String conexionGET(String peticionGET) {
        String body = "";
        try {
            URI uri = new URI(peticionGET);
            URL url = uri.toURL();
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Lee el cuerpo de la respuesta
            String linea;
            StringBuilder sb = new StringBuilder();
            BufferedReader lector = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((linea = lector.readLine()) != null) {
                sb.append(linea);
            }
            body = sb.toString();

        } catch (IOException | URISyntaxException e) {
            System.out.println("Error: conexionGet");
        }

        return body;
    }

    private static String obtenerPartidasJugadas(String html, String jugador) {
        String partidasJugadas = null;
        Document doc = Jsoup.parse(html);
        Elements tablas = doc.select(".contenedor-tableros");

        for (Element tabla : tablas) {
            Elements h2s = tabla.select("h2");
            for (Element h2 : h2s) {
                if (h2.text().contains(jugador)) {
                    partidasJugadas = h2.text();
                    jugadas++;
                }
            }
        }

        return partidasJugadas;
    }

    private static String obtenerPartidasGanadas(String html, String jugador) {
        String partidasGanadas = null;
        Document doc = Jsoup.parse(html);
        Elements tablas = doc.select(".contenedor-tableros");

        for (Element tabla : tablas) {
            Elements h2s = tabla.select("h2");
            for (Element h2 : h2s) {
                if (h2.text().contains(jugador)) {
                    Element tablaPadre = tabla.parent();
                    Element h1 = tablaPadre.selectFirst("h1");
                    if (h1 != null && h1.text().contains(jugador)) {
                        partidasGanadas = h1.text();
                        ganadas++;
                    }
                }
            }
        }

        return partidasGanadas;
    }

    private static void ascii() {
        System.out.println("""
                               ________
                              /_______/\\
                              \\ \\    / /
                            ___\\ \\__/_/___
                           /____\\ \\______/\\
                           \\ \\   \\/ /   / /
                            \\ \\  / /\\  / /
                             \\ \\/ /\\ \\/ /
                              \\_\\/  \\_\\/""");
    }
}
