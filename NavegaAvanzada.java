import java.util.ArrayList;
import java.util.List;

public class NavegaAvanzada {
    private List<String> historial;
    private int indice;

    public NavegaAvanzada() {
        this.historial = new ArrayList<>();
        this.indice = -1;
    }

    public void registrarNavegacion(String url) {
        if (url == null || url.trim().isEmpty()) return;
        url = url.trim();

        if (indice >= 0 && historial.get(indice).equals(url)) {
            return;
        }

        if (indice < historial.size() - 1) {
            historial = new ArrayList<>(historial.subList(0, indice + 1));
        }

        historial.add(url);
        indice++;
    }

    public String obtenerAtras() {
        if (puedeIrAtras()) {
            indice--;
            return historial.get(indice);
        }
        return null;
    }

    public String obtenerAdelante() {
        if (puedeIrAdelante()) {
            indice++;
            return historial.get(indice);
        }
        return null;
    }

    public boolean puedeIrAtras() {
        return indice > 0;
    }

    public boolean puedeIrAdelante() {
        return indice < historial.size() - 1;
    }
}
