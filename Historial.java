import java.util.ArrayList;
import java.util.List;

public class Historial {

    private List<EntradaHistorial> entradas;

    public Historial() {
        entradas = new ArrayList<>();
    }

    public void agregar(String url, String titulo) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            titulo = "Sin título";
        }

        entradas.add(new EntradaHistorial(url, titulo));
    }

    public List<EntradaHistorial> getEntradas() {
        return entradas;
    }

    public void limpiar() {
        entradas.clear();
    }

    public String mostrarHistorial() {
        StringBuilder sb = new StringBuilder();

        if (entradas.isEmpty()) {
            return "No hay páginas visitadas.";
        }

        int i = 0;
        while (i < entradas.size()) {
            EntradaHistorial entrada = entradas.get(i);

            sb.append(i + 1)
                    .append(". ")
                    .append(entrada.getTitulo())
                    .append(" - ")
                    .append(entrada.getUrl())
                    .append("\n");

            i++;
        }

        return sb.toString();
    }

    public static class EntradaHistorial {
        private String url;
        private String titulo;

        public EntradaHistorial(String url, String titulo) {
            this.url = url;
            this.titulo = titulo;
        }

        public String getUrl() {
            return url;
        }

        public String getTitulo() {
            return titulo;
        }
    }
}
