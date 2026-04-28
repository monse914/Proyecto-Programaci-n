import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.text.*;

public class Renderizador {

    private Map<JTextPane, List<EnlaceInfo>> enlacesPorArea;

    public Renderizador() {
        enlacesPorArea = new HashMap<>();
    }

    public void renderizarArchivo(String ruta, JTextPane areaContenido) throws IOException {

        File archivo = new File(ruta);

        if (!archivo.exists() || !archivo.isFile()) {
            throw new IOException("Error: Archivo no encontrado");
        }

        if (!ruta.toLowerCase().endsWith(".html")) {
            throw new IOException("Error: El archivo no es HTML");
        }

        areaContenido.setText("");
        enlacesPorArea.put(areaContenido, new ArrayList<>());

        StyledDocument doc = areaContenido.getStyledDocument();

        StringBuilder contenido = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(archivo));

        String linea;
        while ((linea = br.readLine()) != null) {
            contenido.append(linea).append("\n");
        }
        br.close();

        String html = contenido.toString();
        html = extraerBody(html);
        html = eliminarScriptsYStyles(html);

        parsearHTML(html, doc, archivo.getParentFile(), areaContenido);
    }

    private String extraerBody(String html) {
        String lower = html.toLowerCase();

        int inicio = lower.indexOf("<body");
        int fin = lower.indexOf("</body>");

        if (inicio == -1 || fin == -1) {
            return html;
        }

        inicio = lower.indexOf(">", inicio);
        if (inicio == -1) {
            return html;
        }

        return html.substring(inicio + 1, fin);
    }

    private String eliminarScriptsYStyles(String html) {
        html = html.replaceAll("(?is)<script.*?>.*?</script>", "");
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");
        return html;
    }

    private void parsearHTML(String html, StyledDocument doc, File carpetaBase, JTextPane area) {
        Pattern patron = Pattern.compile("(?is)<[^>]+>|[^<]+");
        Matcher matcher = patron.matcher(html);

        SimpleAttributeSet estiloNormal = crearEstiloNormal();
        SimpleAttributeSet estiloTitulo = crearEstiloTitulo();
        SimpleAttributeSet estiloParrafo = crearEstiloParrafo();

        AttributeSet estiloActual = estiloNormal;
        String hrefActual = null;

        while (matcher.find()) {
            String token = matcher.group();

            if (token.startsWith("<")) {
                String t = token.trim().toLowerCase();

                if (t.matches("(?is)<h1[^>]*>")) {
                    estiloActual = estiloTitulo;

                } else if (t.matches("(?is)</h1>")) {
                    insertar(doc, "\n\n", estiloNormal);
                    estiloActual = estiloNormal;

                } else if (t.matches("(?is)<p[^>]*>")) {
                    estiloActual = estiloParrafo;

                } else if (t.matches("(?is)</p>")) {
                    insertar(doc, "\n\n", estiloNormal);
                    estiloActual = estiloNormal;

                } else if (t.matches("(?is)<a\\b[^>]*>")) {
                    hrefActual = extraerHref(token, carpetaBase);

                } else if (t.matches("(?is)</a>")) {
                    hrefActual = null;

                } else if (t.matches("(?is)<br\\s*/?>")) {
                    insertar(doc, "\n", estiloActual);

                } else if (t.matches("(?is)<li[^>]*>")) {
                    insertar(doc, "• ", estiloNormal);

                } else if (t.matches("(?is)</li>")) {
                    insertar(doc, "\n", estiloNormal);

                } else if (t.matches("(?is)</ul>|</ol>|</div>|</span>|</table>|</tr>|</td>")) {
                    insertar(doc, "\n", estiloNormal);

                } else if (t.matches("(?is)<img\\b[^>]*>")) {
                    String alt = extraerAlt(token);
                    if (alt != null && !alt.trim().isEmpty()) {
                        insertar(doc, decodificarTextoVisible(alt), estiloNormal);
                    }
                }
            } else {
                String texto = decodificarTextoNormal(token);
                texto = compactarEspacios(texto);

                if (!texto.isEmpty()) {
                    if (hrefActual != null) {
                        insertarEnlace(doc, texto, hrefActual, estiloActual, area);
                    } else {
                        insertar(doc, texto, estiloActual);
                    }
                }
            }
        }
    }

    private SimpleAttributeSet crearEstiloNormal() {
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setFontSize(estilo, 14);
        return estilo;
    }

    private SimpleAttributeSet crearEstiloTitulo() {
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setFontSize(estilo, 22);
        StyleConstants.setBold(estilo, true);
        return estilo;
    }

    private SimpleAttributeSet crearEstiloParrafo() {
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setFontSize(estilo, 14);
        StyleConstants.setLeftIndent(estilo, 20);
        StyleConstants.setFirstLineIndent(estilo, 10);
        return estilo;
    }

    private String extraerHref(String tag, File carpetaBase) {
        Pattern p = Pattern.compile("(?is)href\\s*=\\s*\"(.*?)\"");
        Matcher m = p.matcher(tag);

        if (m.find()) {
            return resolverRuta(carpetaBase, m.group(1).trim());
        }

        return null;
    }

    private String extraerAlt(String tag) {
        Pattern p = Pattern.compile("(?is)alt\\s*=\\s*\"(.*?)\"");
        Matcher m = p.matcher(tag);

        if (m.find()) {
            return m.group(1);
        }

        return "";
    }

    private String decodificarTextoNormal(String texto) {
        texto = texto.replace("&nbsp;", " ");
        texto = texto.replace("&amp;", "&");
        texto = texto.replace("&quot;", "\"");
        texto = texto.replace("&#39;", "'");
        texto = texto.replace("\r", "");
        texto = texto.replace("\t", " ");
        return texto;
    }

    private String decodificarTextoVisible(String texto) {
        texto = texto.replace("&nbsp;", " ");
        texto = texto.replace("&lt;", "<");
        texto = texto.replace("&gt;", ">");
        texto = texto.replace("&amp;", "&");
        texto = texto.replace("&quot;", "\"");
        texto = texto.replace("&#39;", "'");
        texto = texto.replace("\r", "");
        texto = texto.replace("\t", " ");
        return texto;
    }

    private String compactarEspacios(String texto) {
        texto = texto.replace("\n", " ");
        texto = texto.replaceAll(" +", " ");
        return texto;
    }

    private void insertarEnlace(StyledDocument doc, String texto, String ruta, AttributeSet estiloBase, JTextPane area) {
        if (texto == null || texto.isEmpty()) {
            return;
        }

        SimpleAttributeSet estilo = new SimpleAttributeSet(estiloBase);
        StyleConstants.setForeground(estilo, new Color(0, 102, 204));
        StyleConstants.setUnderline(estilo, true);

        int inicio = doc.getLength();
        insertar(doc, texto, estilo);
        int fin = doc.getLength();

        List<EnlaceInfo> enlaces = enlacesPorArea.get(area);
        if (enlaces == null) {
            enlaces = new ArrayList<>();
            enlacesPorArea.put(area, enlaces);
        }

        enlaces.add(new EnlaceInfo(inicio, fin, ruta));
    }

    private void insertar(StyledDocument doc, String texto, AttributeSet estilo) {
        try {
            doc.insertString(doc.getLength(), texto, estilo);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String resolverRuta(File base, String href) {
        try {
            if (href.startsWith("file:///")) {
                String resto = href.substring(8);

                boolean tieneRutaAbsoluta =
                        resto.contains("/") || resto.contains("\\") || resto.matches("^[A-Za-z]:.*");

                if (!tieneRutaAbsoluta) {
                    File relativo = new File(base, resto);
                    return "file:///" + relativo.getCanonicalPath().replace("\\", "/");
                }

                File f = new File(resto);
                return "file:///" + f.getCanonicalPath().replace("\\", "/");
            }

            File relativo = new File(base, href);
            return "file:///" + relativo.getCanonicalPath().replace("\\", "/");
        } catch (Exception e) {
            File relativo = new File(base, href);
            return "file:///" + relativo.getPath().replace("\\", "/");
        }
    }

    public String getRutaEnlaceEn(JTextPane area, int pos) {
        List<EnlaceInfo> enlaces = enlacesPorArea.get(area);
        if (enlaces == null) {
            return null;
        }

        for (EnlaceInfo e : enlaces) {
            if (pos >= e.inicio && pos < e.fin) {
                return e.ruta;
            }
        }

        return null;
    }

    public boolean esEnlace(JTextPane area, int pos) {
        return getRutaEnlaceEn(area, pos) != null;
    }

    public void resaltarEnlaceEn(int pos, JTextPane area, boolean modoOscuro) {
        StyledDocument doc = area.getStyledDocument();
        List<EnlaceInfo> enlaces = enlacesPorArea.get(area);

        if (enlaces == null) {
            return;
        }

        for (EnlaceInfo e : enlaces) {
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setUnderline(estilo, true);
            StyleConstants.setFontSize(estilo, 14);

            if (pos >= e.inicio && pos < e.fin) {
                StyleConstants.setForeground(estilo, Color.RED);
            } else {
                if (modoOscuro) {
                    StyleConstants.setForeground(estilo, new Color(120, 170, 255));
                } else {
                    StyleConstants.setForeground(estilo, new Color(0, 102, 204));
                }
            }

            doc.setCharacterAttributes(e.inicio, e.fin - e.inicio, estilo, false);
        }
    }

    public void aplicarTemaEnlaces(JTextPane area, boolean modoOscuro) {
        StyledDocument doc = area.getStyledDocument();
        List<EnlaceInfo> enlaces = enlacesPorArea.get(area);

        if (enlaces == null) {
            return;
        }

        for (EnlaceInfo e : enlaces) {
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setUnderline(estilo, true);
            StyleConstants.setFontSize(estilo, 14);

            if (modoOscuro) {
                StyleConstants.setForeground(estilo, new Color(120, 170, 255));
            } else {
                StyleConstants.setForeground(estilo, new Color(0, 102, 204));
            }

            doc.setCharacterAttributes(e.inicio, e.fin - e.inicio, estilo, false);
        }
    }

    public void aplicarTemaTextoCompleto(JTextPane area, boolean modoOscuro) {
        StyledDocument doc = area.getStyledDocument();

        Color colorTexto;
        if (modoOscuro) {
            colorTexto = Color.WHITE;
        } else {
            colorTexto = Color.BLACK;
        }

        SimpleAttributeSet estiloTexto = new SimpleAttributeSet();
        StyleConstants.setForeground(estiloTexto, colorTexto);
        StyleConstants.setFontSize(estiloTexto, 14);

        doc.setCharacterAttributes(0, doc.getLength(), estiloTexto, false);

        aplicarTemaEnlaces(area, modoOscuro);
    }

    public String obtenerTitulo(String ruta) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            String linea;
            StringBuilder contenido = new StringBuilder();

            while ((linea = br.readLine()) != null) {
                contenido.append(linea);
            }
            br.close();

            String html = contenido.toString();

            Pattern p = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(html);

            if (m.find()) {
                return m.group(1);
            }

        } catch (Exception e) {
        }

        return "Sin título";
    }

    private static class EnlaceInfo {
        int inicio;
        int fin;
        String ruta;

        EnlaceInfo(int inicio, int fin, String ruta) {
            this.inicio = inicio;
            this.fin = fin;
            this.ruta = ruta;
        }
    }
}