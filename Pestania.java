import java.awt.*;
import javax.swing.*;

public class Pestania extends JPanel {

    private BarraNavegacion barraNavegacion;
    private JTextPane areaContenido;
    private JScrollPane scrollContenido;
    private String urlActual;

    public Pestania() {
        setLayout(new BorderLayout());

        barraNavegacion = new BarraNavegacion();

        areaContenido = new JTextPane();
        areaContenido.setEditable(false);
        areaContenido.setFont(new Font("Monospaced", Font.PLAIN, 14));

        scrollContenido = new JScrollPane(areaContenido);

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

    public void aplicarTema(boolean modoOscuro, Color fondoArea, Color textoArea, Renderizador renderizador) {
        barraNavegacion.aplicarTema(modoOscuro);

        areaContenido.setBackground(fondoArea);
        areaContenido.setForeground(textoArea);

        if (renderizador != null) {
            renderizador.aplicarTemaTextoCompleto(areaContenido, modoOscuro);
        }
    }

    private void cerrarPestana(Component componente) {
        int index = pestanas.indexOfComponent(componente);

        if(index == pestanas.getTabCount() - 1 ){
            JOptionPane.showMessageDialog(this, "no puedes eliminar la ultima pestaña");
            return;
        }

        String url = urlPorPestana.get(componente);

        if(url != null){
            pestanasPorUrl.remove(url);
            urlPorPestana.remove(componente);
        }

        etiquetasPestanas.remove(componente);
        pestanas.remove(componente);
    }
}
