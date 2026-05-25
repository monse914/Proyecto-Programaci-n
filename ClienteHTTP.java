import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLSocketFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class ClienteHTTP {

    private String estado = "";

    public String obtenerRespuesta(String urlTexto) throws IOException {

        if (!urlTexto.startsWith("http://") &&
                !urlTexto.startsWith("https://")) {

            urlTexto = "https://" + urlTexto;
        }

        StringBuilder respuesta = new StringBuilder();

        try {

            URL url = new URL(urlTexto);

            HttpURLConnection conexion =
                    (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("GET");

            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(10000);

            conexion.setInstanceFollowRedirects(true);

            conexion.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
            );

            conexion.setRequestProperty(
                    "Accept",
                    "text/html"
            );

            conexion.setRequestProperty(
                    "Accept-Encoding",
                    "gzip"
            );

            int codigo = conexion.getResponseCode();

            estado = codigo + " " +
                    conexion.getResponseMessage();

            InputStream entrada;

            if (codigo >= 400) {
                entrada = conexion.getErrorStream();
            } else {
                entrada = conexion.getInputStream();
            }

            if (entrada == null) {
                throw new IOException(
                        "No se recibió respuesta del servidor"
                );
            }

            String encoding =
                    conexion.getContentEncoding();

            if (encoding != null &&
                    encoding.equalsIgnoreCase("gzip")) {

                entrada =
                        new GZIPInputStream(entrada);
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    entrada,
                                    "UTF-8"
                            )
                    );

            String line;

            while ((line = reader.readLine()) != null) {
                respuesta.append(line).append("\n");
            }

            reader.close();
            conexion.disconnect();

        } catch (SocketTimeoutException e) {

            throw new IOException(
                    "Error: Timeout de conexión"
            );

        } catch (IOException e) {

            throw new IOException(
                    "Error de conexión: " +
                            e.getMessage()
            );
        }

        return respuesta.toString();
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
        boolean https = esHttps(url);

        int puerto = https ? 443 : 80;

        String statusLine;
        String location = null;
        boolean chunked = false;

        try (Socket socket = crearSocket(host, puerto, https)) {
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
            writer.println("Accept-Encoding: identity");
            writer.println("Connection: close");
            writer.println();

            statusLine = reader.readLine();

            if (statusLine != null) {
                estado = codigoEstado(statusLine);
            } else {
                estado = "Sin respuesta";
            }

            String line;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String lower = line.toLowerCase();

                if (lower.startsWith("location:")) {
                    location = line.substring(9).trim();
                }

                if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                    chunked = true;
                }
            }

            if (esRedireccion(statusLine) && location != null) {
                String nuevaUrl = resolverLocation(url, location);
                return obtenerRespuestaConRedireccion(nuevaUrl, redirecciones + 1);
            }

            if (chunked) {
                return leerBodyChunkedSeguro(reader);
            }

            return leerBodyNormal(reader);

        } catch (SocketTimeoutException e) {
            estado = "Timeout";
            throw new IOException("Error: Timeout de conexión");
        } catch (IOException e) {
            estado = "Error de conexión";
            throw new IOException("Error de conexión: " + e.getMessage());
        }
    }

    public String getEstado() {
        return estado;
    }

    private Socket crearSocket(String host, int puerto, boolean https) throws IOException {
        if (https) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return factory.createSocket(host, puerto);
        }

        return new Socket(host, puerto);
    }

    private String leerBodyNormal(BufferedReader reader) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }

        return body.toString();
    }

    private String leerBodyChunkedSeguro(BufferedReader reader) throws IOException {
        StringBuilder body = new StringBuilder();

        while (true) {
            String sizeLine = reader.readLine();

            if (sizeLine == null) {
                break;
            }

            sizeLine = sizeLine.trim();

            if (sizeLine.isEmpty()) {
                continue;
            }

            int puntoComa = sizeLine.indexOf(";");
            if (puntoComa != -1) {
                sizeLine = sizeLine.substring(0, puntoComa);
            }

            int size;

            try {
                size = Integer.parseInt(sizeLine, 16);
            } catch (NumberFormatException e) {
                body.append(sizeLine).append("\n");
                body.append(leerBodyNormal(reader));
                break;
            }

            if (size == 0) {
                break;
            }

            char[] buffer = new char[size];
            int leidos = 0;

            while (leidos < size) {
                int n = reader.read(buffer, leidos, size - leidos);

                if (n == -1) {
                    break;
                }

                leidos += n;
            }

            body.append(buffer, 0, leidos);

            reader.readLine();
        }

        return body.toString();
    }

    private boolean esHttps(String url) {
        return url.toLowerCase().startsWith("https://");
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

        String protocolo = urlActual.startsWith("https://") ? "https://" : "http://";
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
