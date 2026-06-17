import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class Gemini {
    private static final String API_KEY = "tu api";

    public String preguntar(String pregunta) throws Exception {

        String endpoint = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        URL url = new URL(endpoint);

        HttpsURLConnection conexion =
        (HttpsURLConnection) url.openConnection();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);

        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);

        conexion.setConnectTimeout(10000);
        conexion.setReadTimeout(10000);

        conexion.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        String json = "{"+ "\"contents\":[{" + "\"parts\":[{" + "\"text\":\"" + escapar(pregunta) + "\"" + "}]" + "}]" + "}";

        try(OutputStream os =
                    conexion.getOutputStream()) {

            os.write(json.getBytes("UTF-8"));
        }

        int codigo = conexion.getResponseCode();
        
        if (codigo != 200) {
                throw new IOException("No fue posible conectarse a Gemini. Código: " + codigo);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conexion.getInputStream(), "UTF-8"));

        StringBuilder respuesta = new StringBuilder();

        String linea;

        while((linea = br.readLine()) != null) {
            respuesta.append(linea);
        }

        br.close();

        return extraerTexto(respuesta.toString());
    }

    private String escapar(String texto) {

        return texto
                .replace("\\","\\\\")
                .replace("\"","\\\"");
    }

   private String extraerTexto(String json) {
        try {
                int inicio = json.indexOf("\"text\":");
                if (inicio == -1) {
                        return "No se encontró texto.";
                }
                
                inicio = json.indexOf("\"", inicio + 7) + 1;
                
                StringBuilder texto = new StringBuilder();
                
                boolean escape = false;
                
                for (int i = inicio; i < json.length(); i++) {
                        char c = json.charAt(i);
                        if (escape) {
                                switch (c) {
                                        case 'n': texto.append('\n');
                                                break;
                                                case '"': texto.append('"');
                                                        break;
                                                        case '\\': texto.append('\\');
                                                                break;
                                                                default: texto.append(c);
                                                                }
                                                                escape = false;
                                                                continue;
                                                        }
                                                        
                                                        if (c == '\\') {
                                                                escape = true;
                                                                continue;
                                                        }
                                                        
                                                        if (c == '"') {
                                                                break;
                                                        }
                                                        texto.append(c);
                                                }
                                                return texto.toString();
                                        } catch (Exception e) {
                                                return "Error leyendo respuesta: " + e.getMessage();
                                        }
                                }
                        }
