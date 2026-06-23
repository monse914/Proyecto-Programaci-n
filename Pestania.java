import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

public class Pestania extends JPanel {

    private BarraNavegacion barraNavegacion;
    private JTextPane areaContenido;
    private JScrollPane scrollContenido;
    private String urlActual;
    private java.util.List<String> historial = new java.util.ArrayList<>();
    private int indice = -1;
    private String estado = "";
    private String historialIA = "";

    public Pestania() {
        setLayout(new BorderLayout());

        barraNavegacion = new BarraNavegacion();

        areaContenido = new JTextPane();
        areaContenido.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
        areaContenido.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaContenido.setHighlighter(null);
        areaContenido.setFocusable(false);
        areaContenido.setSelectionColor(
                new Color(0,0,0,0)
        );

        scrollContenido = new JScrollPane(areaContenido);

        scrollContenido.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        scrollContenido.setViewportBorder(null);

        scrollContenido.setFocusable(false);

        scrollContenido.getViewport().setFocusable(false);

        add(barraNavegacion, BorderLayout.NORTH);
        add(scrollContenido, BorderLayout.CENTER);

        urlActual = null;
    }

    public BarraNavegacion getBarraNavegacion() {
        return barraNavegacion;
    }

    public JTextPane getAreaContenido() {
        return areaContenido;
    }

    public JScrollPane getScrollContenido() {
        return scrollContenido;
    }

    public String getUrlActual() {
        return urlActual;
    }

    public void setUrlActual(String urlActual) {
        this.urlActual = urlActual;
        barraNavegacion.setURL(urlActual);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void aplicarTema(boolean modoOscuro, Color fondoArea, Color textoArea, Renderizador renderizador) {
        barraNavegacion.aplicarTema(modoOscuro);

        areaContenido.setBackground(fondoArea);
        areaContenido.setForeground(textoArea);

        if (renderizador != null) {
            renderizador.aplicarTemaTextoCompleto(areaContenido, modoOscuro);
        }

        if (scrollContenido != null) {
            scrollContenido.setBackground(fondoArea);
            scrollContenido.getViewport().setBackground(fondoArea);
        }
    }

    public void liberarRecursos() {
        if (areaContenido != null) {
            areaContenido.setText("");
            areaContenido.setDocument(new javax.swing.text.DefaultStyledDocument());

            for (java.awt.event.MouseListener ml : areaContenido.getMouseListeners()) {
                areaContenido.removeMouseListener(ml);
            }

            for (java.awt.event.MouseMotionListener mml : areaContenido.getMouseMotionListeners()) {
                areaContenido.removeMouseMotionListener(mml);
            }

            for (javax.swing.event.HyperlinkListener hl : areaContenido.getHyperlinkListeners()) {
                areaContenido.removeHyperlinkListener(hl);
            }

        }

        if (barraNavegacion != null) {
            barraNavegacion.setAccionNavegacion(null);
            barraNavegacion.setAccionRecargar(null);
            barraNavegacion.setAccionFavorito(null);
            barraNavegacion.setAccionMostrarFavoritos(null);
            barraNavegacion.setAccionModo(null);
            barraNavegacion.setURL("");
            barraNavegacion.setAccionAtras(null);
            barraNavegacion.setAccionAdelante(null);
        }

        if (scrollContenido != null) {
            scrollContenido.setViewportView(null);
        }

        removeAll();

        barraNavegacion = null;
        areaContenido = null;
        scrollContenido = null;
        urlActual = null;

        revalidate();
        repaint();
    }

    public void navegar(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        urlActual = url;

        if (indice >= 0 && historial.get(indice).equals(url)) {
            return;
        }

        if (indice < historial.size() - 1) {
            historial = new ArrayList<>(historial.subList(0, indice + 1));
        }

        historial.add(url);
        indice++;
    }

    public String atras() {
        if (indice > 0) {
            indice--;
            urlActual = historial.get(indice);
        }

        return urlActual;
    }

    public String adelante() {
        if (indice < historial.size() - 1) {
            indice++;
            urlActual = historial.get(indice);
        }

        return urlActual;
    }

    public boolean puedeAtras() {
        return indice > 0;
    }

    public boolean puedeAdelante() {
        return indice < historial.size() - 1;
    }

    public String getHistorialIA() {
        return historialIA;
    }

    public void agregarHistorialIA(String texto) {
        historialIA += texto + "\n";
    }

}
