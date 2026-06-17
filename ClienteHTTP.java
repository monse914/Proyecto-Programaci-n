import java.io.*;
import java.net.SocketTimeoutException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class ClienteHTTP {
    private String estado = "";
    private Map<String, List<String>> headers;
    private String cuerpo = "";

    public String obtenerRespuesta(String urlTexto) throws IOException {
        validarURLAbsoluta(urlTexto);
        StringBuilder respuesta = new StringBuilder();

        try {
            URL url = new URL(urlTexto);

            System.out.println("URL COMPLETA: " + urlTexto);
            System.out.println("HOST: " + url.getHost());
            System.out.println("PUERTO: " + url.getPort());
            System.out.println("PROTOCOLO: " + url.getProtocol());

            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(10000);
            conexion.setInstanceFollowRedirects(true);

            conexion.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/137.0 Safari/537.36");
            conexion.setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*");
            conexion.setRequestProperty("Accept-Encoding", "gzip");

            int codigo = conexion.getResponseCode();

            if (codigo == 301 || codigo == 302) {
                String nuevaUrl = conexion.getHeaderField("Location");
                if (nuevaUrl != null && !nuevaUrl.isEmpty()) {
                    return obtenerRespuesta(nuevaUrl);
                }
            }

            estado = codigo + " " + conexion.getResponseMessage();
            headers = conexion.getHeaderFields();

            InputStream entrada;

            if (codigo >= 400) {
                entrada = conexion.getErrorStream();
            } else {
                entrada = conexion.getInputStream();
            }

            if (entrada == null) {
                throw new IOException("No se recibió respuesta del servidor");
            }

            String encoding = conexion.getContentEncoding();

            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                entrada = new GZIPInputStream(entrada);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entrada,"UTF-8")
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    respuesta.append(line).append("\n");
                }
            }
            conexion.disconnect();
            cuerpo = respuesta.toString();

        } catch (SocketTimeoutException e) {
            estado = "Timeout";
            throw new IOException("Error: Timeout de conexión");
        } catch (MalformedURLException e) {
            estado = "URL inválida";
            throw new IOException("Error: URL inválida");
        } catch (IOException e) {
            estado = "Error de conexión";
            throw new IOException("Error de conexión: " + e.getMessage());
        }
        return cuerpo;
    }

    private void validarURLAbsoluta(String urlTexto) throws IOException {
        if (urlTexto == null || urlTexto.trim().isEmpty()) {
            throw new IOException("Error: URL vacía");
        }

        urlTexto = urlTexto.trim();

        if (!urlTexto.startsWith("http://") && !urlTexto.startsWith("https://")) {
            urlTexto = "https://" + urlTexto;
        }
        try {
            URL url = new URL(urlTexto);
            if (url.getHost() == null || url.getHost().isEmpty()) {
                throw new IOException("Error: URL sin host");
            }
            int puerto = url.getPort();
            if (puerto == -1) {
                if (url.getProtocol().equals("https")) {
                    puerto = 443;
                } else {
                    puerto = 80;
                }
            }

            if (puerto != 80 && puerto != 443 && puerto != 8080 && puerto != 3000 && puerto != 5173) {
                throw new IOException("Conexión a puerto " + puerto + " no soportada");
            }

        } catch (MalformedURLException e) {
            throw new IOException("Error: Formato de URL inválido");
        }
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public String getEstado() {
        return estado;
    }
}
