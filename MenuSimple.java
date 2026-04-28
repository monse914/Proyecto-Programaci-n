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

    private CambioTemaListener listener;

    public interface CambioTemaListener {
        void alCambiarTema(boolean modoOscuro, Color fondo, Color texto);
    }

    public MenuSimple(JTextPane areaTexto, CambioTemaListener listener) {
        this.listener = listener;

        menuOpciones = new JMenu("Opciones");

        modoClaroItem = new JMenuItem("Modo claro");
        modoOscuroItem = new JMenuItem("Modo oscuro");

        modoClaroItem.addActionListener(e -> {
            this.modoOscuro = false;
            colorFondoActual = Color.WHITE;
            colorTextoActual = Color.BLACK;
            aplicarTema(areaTexto);

            if (listener != null) {
                listener.alCambiarTema(this.modoOscuro, colorFondoActual, colorTextoActual);
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

        menuOpciones.add(modoClaroItem);
        menuOpciones.add(modoOscuroItem);
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