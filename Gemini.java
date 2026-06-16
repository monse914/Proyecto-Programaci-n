import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class Gemini {

    private final String API_KEY =
            "tu api monse";

    public String preguntar(String pregunta) throws Exception {

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                        + API_KEY;

        URL url = new URL(endpoint);

        HttpsURLConnection conexion =
                (HttpsURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);

        conexion.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        String json =
                "{"
                        + "\"contents\":[{"
                        + "\"parts\":[{"
                        + "\"text\":\"" + escapar(pregunta) + "\""
                        + "}]"
                        + "}]"
                        + "}";

        try(OutputStream os =
                    conexion.getOutputStream()) {

            os.write(json.getBytes("UTF-8"));
        }

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(
                                conexion.getInputStream(),
                                "UTF-8"
                        )
                );

        StringBuilder respuesta =
                new StringBuilder();

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

        String clave = "\"text\":\"";

        int inicio = json.indexOf(clave);

        if(inicio == -1) {
            return "No se pudo obtener respuesta.";
        }

        inicio += clave.length();

        int fin = json.indexOf("\"", inicio);

        if(fin == -1) {
            return "Respuesta inválida.";
        }

        return json.substring(inicio, fin)
                .replace("\\n","\n");
    }
}
