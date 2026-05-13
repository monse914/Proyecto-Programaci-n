import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class MenuSimple extends JMenuBar {

    private Color colorFondoActual;
    private Color colorTextoActual;
    private boolean modoOscuro;

    private JMenu menuOpciones;
    private JMenuItem modoClaroItem;
    private JMenuItem modoOscuroItem;
    private JMenuItem historialItem;
    private Ventana ventana;

    private CambioTemaListener listener;
    private Runnable accionHistorial;

    public interface CambioTemaListener {
        void alCambiarTema(boolean modoOscuro, Color fondo, Color texto);
    }

    public MenuSimple(JTextPane areaTexto,Ventana ventana, CambioTemaListener listener) {
        this.listener = listener;
        this.accionHistorial = accionHistorial;
        this.ventana = ventana;

        menuOpciones = new JMenu("Opciones");

        modoClaroItem = new JMenuItem("Modo claro");
        modoOscuroItem = new JMenuItem("Modo oscuro");
        historialItem = new JMenuItem("Mostrar Historial");

        modoClaroItem.addActionListener(e -> {
            this.modoOscuro = false;
            colorFondoActual = Color.WHITE;
            colorTextoActual = Color.BLACK;
            aplicarTema(areaTexto);

            if (listener != null) {
                listener.alCambiarTema(this.modoOscuro, colorFondoActual, colorTextoActual);
            }
        });

        historialItem.addActionListener(e -> {
            if (accionHistorial != null) {
                accionHistorial.run();
            }
        });

        modoOscuroItem.addActionListener(e -> {
            this.modoOscuro = true;
            colorFondoActual = new Color(30, 30, 30);
            colorTextoActual = Color.WHITE;
            aplicarTema(areaTexto);

            if (listener != null) {
                listener.alCambiarTema(this.modoOscuro, colorFondoActual, colorTextoActual);
            }
        });
        
        historialItem.addActionListener(e -> {
            ventana.mostrarOcultarHistorial();
        });

        menuOpciones.add(modoClaroItem);
        menuOpciones.add(modoOscuroItem);
        menuOpciones.add(historialItem);
        add(menuOpciones);

        this.modoOscuro = false;
        colorFondoActual = Color.WHITE;
        colorTextoActual = Color.BLACK;

        aplicarTema(areaTexto);
        aplicarTemaMenu();
    }

    private void aplicarTema(JTextPane areaTexto) {
        areaTexto.setBackground(colorFondoActual);
        cambiarColorTexto(areaTexto, colorTextoActual);
        aplicarTemaMenu();
    }

    private void aplicarTemaMenu() {
        Color fondoMenu;
        Color textoMenu;
        Color fondoItems;

        if (modoOscuro) {
            fondoMenu = new Color(45, 45, 45);
            textoMenu = Color.WHITE;
            fondoItems = new Color(60, 60, 60);
        } else {
            fondoMenu = new Color(240, 240, 240);
            textoMenu = Color.BLACK;
            fondoItems = Color.WHITE;
        }

        setBackground(fondoMenu);
        setForeground(textoMenu);

        menuOpciones.setBackground(fondoMenu);
        menuOpciones.setForeground(textoMenu);
        menuOpciones.setOpaque(true);

        modoClaroItem.setBackground(fondoItems);
        modoClaroItem.setForeground(textoMenu);
        modoClaroItem.setOpaque(true);

        modoOscuroItem.setBackground(fondoItems);
        modoOscuroItem.setForeground(textoMenu);
        modoOscuroItem.setOpaque(true);

        historialItem.setBackground(fondoItems);
        historialItem.setForeground(textoMenu);
        historialItem.setOpaque(true);

        repaint();
    }

    private void cambiarColorTexto(JTextPane areaTexto, Color color) {
        StyledDocument doc = areaTexto.getStyledDocument();

        if (doc.getLength() == 0) {
            areaTexto.setForeground(color);
            return;
        }

        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, color);

        doc.setCharacterAttributes(0, doc.getLength(), estilo, false);
        areaTexto.repaint();
    }

    public boolean isModoOscuro() {
        return modoOscuro;
    }

    public void aplicarTemaActual(JTextPane areaTexto) {
        aplicarTema(areaTexto);
    }
}
