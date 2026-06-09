import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenderAvanzado {

    public String interpretarEtiqueta(String tag) {

        if (tag == null || tag.trim().isEmpty()) {
            return "";
        }

        String t = tag.trim().toLowerCase();

        if (t.matches("(?is)<!doctype[^>]*>")) return "";
        if (t.matches("(?is)<html[^>]*>|</html>")) return "";
        if (t.matches("(?is)<head[^>]*>|</head>")) return "";
        if (t.matches("(?is)<body[^>]*>|</body>")) return "";
        if (t.matches("(?is)<title[^>]*>|</title>")) return "";
        if (t.matches("(?is)<meta[^>]*>")) return "";
        if (t.matches("(?is)<link[^>]*>")) return "";

        if (t.matches("(?is)<div[^>]*>")) return "\n";
        if (t.matches("(?is)</div>")) return "\n";

        if (t.matches("(?is)<span[^>]*>")) return "";
        if (t.matches("(?is)</span>")) return "";

        if (t.matches("(?is)<header[^>]*>")) return "\n[ENCABEZADO]\n";
        if (t.matches("(?is)</header>")) return "\n";

        if (t.matches("(?is)<footer[^>]*>")) return "\n[PIE DE PÁGINA]\n";
        if (t.matches("(?is)</footer>")) return "\n";

        if (t.matches("(?is)<nav[^>]*>")) return "\n[NAVEGACIÓN]\n";
        if (t.matches("(?is)</nav>")) return "\n";

        if (t.matches("(?is)<section[^>]*>")) return "\n[SECCIÓN]\n";
        if (t.matches("(?is)</section>")) return "\n";

        if (t.matches("(?is)<article[^>]*>")) return "\n[ARTÍCULO]\n";
        if (t.matches("(?is)</article>")) return "\n";

        if (t.matches("(?is)<aside[^>]*>")) return "\n[CONTENIDO SECUNDARIO]\n";
        if (t.matches("(?is)</aside>")) return "\n";

        if (t.matches("(?is)<figure[^>]*>")) return "\n[FIGURA]\n";
        if (t.matches("(?is)</figure>")) return "\n";

        if (t.matches("(?is)<figcaption[^>]*>")) return "\nLeyenda: ";
        if (t.matches("(?is)</figcaption>")) return "\n";

        if (t.matches("(?is)<table[^>]*>")) return "\n┌──────── TABLA ────────┐\n";
        if (t.matches("(?is)</table>")) return "\n└───────────────────────┘\n";

        if (t.matches("(?is)<tr[^>]*>")) return "\n";
        if (t.matches("(?is)</tr>")) return "\n";

        if (t.matches("(?is)<th[^>]*>")) return " | ";
        if (t.matches("(?is)</th>")) return " | ";

        if (t.matches("(?is)<td[^>]*>")) return " | ";
        if (t.matches("(?is)</td>")) return " | ";

        if (t.matches("(?is)<form[^>]*>")) return "\n┌──── FORMULARIO ────┐\n";
        if (t.matches("(?is)</form>")) return "\n└────────────────────┘\n";

        if (t.matches("(?is)<label[^>]*>")) return "\n";
        if (t.matches("(?is)</label>")) return " ";

        if (t.matches("(?is)<input[^>]*>")) return interpretarInput(tag);

        if (t.matches("(?is)<button[^>]*>")) return "\n[Botón: ";
        if (t.matches("(?is)</button>")) return "]\n";

        if (t.matches("(?is)<select[^>]*>")) return "\n[Lista desplegable]\n";
        if (t.matches("(?is)</select>")) return "\n";

        if (t.matches("(?is)<option[^>]*>")) return "- ";
        if (t.matches("(?is)</option>")) return "\n";

        if (t.matches("(?is)<textarea[^>]*>")) return "\n[Área de texto]\n";
        if (t.matches("(?is)</textarea>")) return "\n";

        if (t.matches("(?is)<audio[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: audio";
        }
        if (t.matches("(?is)</audio>")) return "";

        if (t.matches("(?is)<picture[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar completamente: picture";
        }
        if (t.matches("(?is)</picture>")) return "";

        if (t.matches("(?is)<video[^>]*>")) {
            return "NO_RENDER:No se puede renderizar: video";
        }
        if (t.matches("(?is)</video>")) return "";

        if (t.matches("(?is)<source[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: source";
        }

        if (t.matches("(?is)<track[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: track";
        }

        if (t.matches("(?is)<canvas[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: canvas";
        }
        if (t.matches("(?is)</canvas>")) return "";

        if (t.matches("(?is)<svg[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: svg";
        }
        if (t.matches("(?is)</svg>")) return "";

        if (t.matches("(?is)<iframe[^>]*>")) {
            return "NO_RENDER:Este elemento no se puede renderizar: iframe";
        }
        if (t.matches("(?is)</iframe>")) return "";

        return "";
    }

    private String interpretarInput(String tag) {

        String type = extraerAtributo(tag, "type");
        String name = extraerAtributo(tag, "name");
        String placeholder = extraerAtributo(tag, "placeholder");
        String value = extraerAtributo(tag, "value");

        if (type.isEmpty()) {
            type = "text";
        }

        String texto = "\n[Input tipo=" + type;

        if (!name.isEmpty()) {
            texto += ", nombre=" + name;
        }

        if (!placeholder.isEmpty()) {
            texto += ", placeholder=" + placeholder;
        }

        if (!value.isEmpty()) {
            texto += ", valor=" + value;
        }

        texto += "]\n";

        return texto;
    }

    private String extraerAtributo(String tag, String atributo) {

        Pattern p = Pattern.compile("(?is)" + atributo + "\\s*=\\s*\"(.*?)\"");
        Matcher m = p.matcher(tag);

        if (m.find()) {
            return m.group(1).trim();
        }

        p = Pattern.compile("(?is)" + atributo + "\\s*=\\s*'(.*?)'");
        m = p.matcher(tag);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "";
    }
}
