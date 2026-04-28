import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClienteHTTP {

    public String obtenerRespuesta(String url) throws IOException {

        String host = limpiarHost(url);
        int port = 80;

        StringBuilder respuesta = new StringBuilder();

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(10000);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer.println("GET / HTTP/1.1");
            writer.println("Host: " + host);
            writer.println("User-Agent: ClienteHTTP/1.0");
            writer.println("Connection: close");
            writer.println();

            String line;

            respuesta.append("=== STATUS ===\n");
            line = reader.readLine();
            if (line != null) {
                respuesta.append(line).append("\n");
            }

            respuesta.append("\n=== HEADERS ===\n");

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                respuesta.append(line).append("\n");
            }

            respuesta.append("\n=== BODY ===\n");

            while ((line = reader.readLine()) != null) {
                respuesta.append(line).append("\n");
            }

        } catch (SocketTimeoutException e) {
            throw new IOException("Error: Timeout de conexión");
        } catch (IOException e) {
            throw new IOException("Error de conexión: " + e.getMessage());
        }

        return respuesta.toString();
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
