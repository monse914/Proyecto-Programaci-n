import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLSocketFactory;

public class ClienteHTTP {

    private String estado = "";

    public String obtenerRespuesta(String url) throws IOException {
        return obtenerRespuestaConRedireccion(url, 0);
    }

    private String obtenerRespuestaConRedireccion(String url, int redirecciones) throws IOException {
        if (redirecciones > 5) {
            throw new IOException("Error: Demasiadas redirecciones");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        String host = limpiarHost(url);
        String ruta = obtenerRuta(url);
        boolean esHttps = esHttps(url);

        int port;
        if (esHttps) {
            port = 443;
        } else {
            port = 80;
        }

        StringBuilder respuesta = new StringBuilder();
        String location = null;
        String statusLine = null;

        try (Socket socket = crearSocket(host, port, esHttps)) {

            socket.setSoTimeout(10000);

            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()),
                    true
            );

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer.println("GET " + ruta + " HTTP/1.1");
            writer.println("Host: " + host);
            writer.println("User-Agent: ClienteHTTP/1.0");
            writer.println("Connection: close");
            writer.println();

            String line;

            respuesta.append("=== STATUS ===\n");
            line = reader.readLine();

            if (line != null) {
                statusLine = line;
                estado = codigoEstado(line);
                respuesta.append(line).append("\n");
            } else {
                estado = "Sin respuesta";
            }

            respuesta.append("\n=== HEADERS ===\n");

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                respuesta.append(line).append("\n");

                String lower = line.toLowerCase();
                if (lower.startsWith("location:")) {
                    location = line.substring(9).trim();
                }
            }

            if (esRedireccion(statusLine) && location != null) {
                String nuevaUrl = resolverLocation(url, location);
                return obtenerRespuestaConRedireccion(nuevaUrl, redirecciones + 1);
            }

            respuesta.append("\n=== BODY ===\n");

            while ((line = reader.readLine()) != null) {
                respuesta.append(line).append("\n");
            }

        } catch (SocketTimeoutException e) {
            estado = "Timeout";
            throw new IOException("Error: Timeout de conexión");
        } catch (IOException e) {
            estado = "Error de conexión";
            throw new IOException("Error de conexión: " + e.getMessage());
        }

        return respuesta.toString();
    }

    public String getEstado() {
        return estado;
    }

    private Socket crearSocket(String host, int port, boolean esHttps) throws IOException {
        if (esHttps) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return factory.createSocket(host, port);
        }

        return new Socket(host, port);
    }

    private boolean esHttps(String url) {
        url = url.trim().toLowerCase();
        return url.startsWith("https://");
    }

    private boolean esRedireccion(String statusLine) {
        if (statusLine == null) {
            return false;
        }

        return statusLine.contains(" 301 ")
                || statusLine.contains(" 302 ")
                || statusLine.contains(" 303 ")
                || statusLine.contains(" 307 ")
                || statusLine.contains(" 308 ");
    }

    private String resolverLocation(String urlActual, String location) {
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return location;
        }

        String protocolo;
        if (urlActual.startsWith("https://")) {
            protocolo = "https://";
        } else {
            protocolo = "http://";
        }

        String host = limpiarHost(urlActual);

        if (location.startsWith("/")) {
            return protocolo + host + location;
        }

        return protocolo + host + "/" + location;
    }

    private String codigoEstado(String linea) {
        String[] partes = linea.split(" ", 3);
        if (partes.length >= 3) {
            return partes[1] + " " + partes[2];
        }
        return linea;
    }

    private String obtenerRuta(String url) {
        url = url.trim();

        if (url.startsWith("http://")) {
            url = url.substring(7);
        }

        if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        int slash = url.indexOf("/");
        if (slash != -1) {
            return url.substring(slash);
        }

        return "/";
    }

    private String limpiarHost(String url) {
        url = url.trim();

        if (url.startsWith("http://")) {
            url = url.substring(7);
        }

        if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        int slash = url.indexOf("/");
        if (slash != -1) {
            url = url.substring(0, slash);
        }

        return url;
    }
}
