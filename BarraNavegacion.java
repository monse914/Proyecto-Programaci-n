import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

public class BarraNavegacion extends JPanel {

    private JTextField campoURL;
    private JButton botonIr;
    private JButton botonRecargar;
    private JButton botonFavorito;
    private JButton botonVerFavoritos;
    private JButton botonOpciones;
    private JPopupMenu menuOpcionesPopup;

    private AccionNavegacion accionNavegacion;
    private AccionRecargar accionRecargar;
    private AccionFavorito accionFavorito;
    private AccionMostrarFavoritos accionMostrarFavoritos;

    public BarraNavegacion() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        campoURL = new JTextField(30);
        campoURL.setPreferredSize(new Dimension(320, 28));

        botonIr = new JButton("Ir");
        botonIr.setEnabled(false);

        botonRecargar = new JButton("↻");
        aplicarEstiloBotonIcono(botonRecargar);
        botonRecargar.setFont(
                botonRecargar.getFont().deriveFont(20f)
        );

        botonFavorito = new JButton("☆");
        aplicarEstiloBotonIcono(botonFavorito);
        botonFavorito.setFont(
                botonFavorito.getFont().deriveFont(20f)
        );

        botonVerFavoritos = new JButton("Favoritos");

        botonOpciones = new JButton("Opciones");
        botonOpciones.setFocusPainted(false);
        botonOpciones.setBorderPainted(false);
        botonOpciones.setBackground(Color.WHITE);
        botonOpciones.setForeground(Color.BLACK);
        botonOpciones.setPreferredSize(new Dimension(100, 28));
        menuOpcionesPopup = new JPopupMenu();

        campoURL.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validarCampoVacio();
            }

            public void removeUpdate(DocumentEvent e) {
                validarCampoVacio();
            }

            public void changedUpdate(DocumentEvent e) {
                validarCampoVacio();
            }
        });

        campoURL.addActionListener(e -> ejecutarNavegacion());
        botonIr.addActionListener(e -> ejecutarNavegacion());

        botonRecargar.addActionListener(e -> {
            if (accionRecargar != null) {
                accionRecargar.alRecargar();
            }
        });

        botonFavorito.addActionListener(e -> {
            if (accionFavorito != null) {
                accionFavorito.alCambiarFavorito();
            }
        });

        botonVerFavoritos.addActionListener(e -> {
            if (accionMostrarFavoritos != null) {
                accionMostrarFavoritos.alMostrarFavoritos();
            }
        });

        add(botonRecargar);
        add(botonFavorito);
        add(campoURL);
        add(botonIr);
        add(botonVerFavoritos);
        add(botonOpciones);

        configurarHoverBotones();
    }

    private void configurarHoverBotones() {
        JButton[] botones = {
                botonIr,
                botonRecargar,
                botonFavorito,
                botonVerFavoritos,
                botonOpciones
        };
        for (JButton b : botones) {
            b.setFocusPainted(false);
            b.setBorderPainted(false);

            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (b == botonRecargar || b == botonFavorito) {
                        b.setOpaque(true);
                        b.setBackground(new Color(180, 215, 255));
                    } else {
                        b.setBackground(new Color(180, 215, 255));
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (b == botonRecargar || b == botonFavorito) {
                        b.setOpaque(false);
                        b.setBackground(new Color(0,0,0,0));
                    } else {
                        b.setBackground(Color.WHITE);
                    }
                }
            });
        }
    }

    private void validarCampoVacio() {
        botonIr.setEnabled(!campoURL.getText().trim().isEmpty());
    }

    private void ejecutarNavegacion() {
        if (accionNavegacion != null && !getURL().isEmpty()) {
            accionNavegacion.alNavegar(getURL());
        }
    }

    public String getURL() {
        return campoURL.getText().trim();
    }

    public void setURL(String url) {
        campoURL.setText(url);
    }

    public void setFavoritoActivo(boolean activo) {
        if (activo) {
            botonFavorito.setText("★");
        } else {
            botonFavorito.setText("☆");
        }
    }

    public void setAccionNavegacion(AccionNavegacion accionNavegacion) {
        this.accionNavegacion = accionNavegacion;
    }

    public void setAccionRecargar(AccionRecargar accionRecargar) {
        this.accionRecargar = accionRecargar;
    }

    public void setAccionFavorito(AccionFavorito accionFavorito) {
        this.accionFavorito = accionFavorito;
    }

    public void setAccionMostrarFavoritos(AccionMostrarFavoritos accionMostrarFavoritos) {
        this.accionMostrarFavoritos = accionMostrarFavoritos;
    }

    public boolean esURLLocalValida() {
        String url = getURL();
        return !url.isEmpty() && url.startsWith("file:///");
    }

    private void aplicarEstiloBotonIcono(JButton boton) {

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);

        boton.setPreferredSize(new Dimension(46, 30));

        boton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));

        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setVerticalAlignment(SwingConstants.CENTER);

        boton.setMargin(new Insets(0, 0, 0, 0));

        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void aplicarTema(boolean modoOscuro) {
        Color fondo;
        Color texto;
        Color fondoBoton;

        if (modoOscuro) {
            fondo = new Color(60, 60, 60);
            texto = Color.WHITE;
            fondoBoton = new Color(90, 90, 90);
        } else {
            texto = Color.BLACK;
            fondo = new Color(240, 240, 240);
            fondoBoton = Color.WHITE;
        }

        setBackground(fondo);

        campoURL.setBackground(Color.WHITE);
        campoURL.setForeground(Color.BLACK);
        campoURL.setCaretColor(Color.BLACK);

        if (modoOscuro) {
            campoURL.setBackground(new Color(80, 80, 80));
            campoURL.setForeground(Color.WHITE);
            campoURL.setCaretColor(Color.WHITE);
        }

        botonIr.setBackground(fondoBoton);
        botonIr.setForeground(texto);

        botonRecargar.setForeground(texto);

        botonFavorito.setForeground(texto);

        botonVerFavoritos.setBackground(fondoBoton);
        botonVerFavoritos.setForeground(texto);
    }

    public interface AccionNavegacion {
        void alNavegar(String url);
    }

    public interface AccionRecargar {
        void alRecargar();
    }

    public interface AccionFavorito {
        void alCambiarFavorito();
    }

    public interface AccionMostrarFavoritos {
        void alMostrarFavoritos();
    }

    public void configurarMenuOpciones(MenuSimple menu) {

        menuOpcionesPopup.removeAll();

        JMenu menuOriginal = menu.getMenuOpciones();

        for (int i = 0; i < menuOriginal.getItemCount(); i++) {

            JMenuItem item = menuOriginal.getItem(i);

            if (item != null) {
                JMenuItem copia = new JMenuItem(item.getText());

                for (ActionListener al : item.getActionListeners()) {
                    copia.addActionListener(al);
                }

                menuOpcionesPopup.add(copia);
            } else {
                menuOpcionesPopup.addSeparator();
            }
        }

        botonOpciones.addActionListener(e -> {
            menuOpcionesPopup.show(
                    botonOpciones,
                    0,
                    botonOpciones.getHeight()
            );
        });
    }
}
