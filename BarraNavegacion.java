import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BarraNavegacion extends JPanel {

    private JTextField campoURL;
    private JButton botonIr;
    private JButton botonRecargar;
    private JButton botonFavorito;
    private JButton botonVerFavoritos;
    private JButton botonOpciones;
    private JPopupMenu menuOpcionesPopup;
    private JButton botonAtras;
    private JButton botonAdelante;
    private JButton botonBuscar;

    private boolean modoOscuro = false;

    private AccionNavegacion accionNavegacion;
    private AccionRecargar accionRecargar;
    private AccionFavorito accionFavorito;
    private AccionMostrarFavoritos accionMostrarFavoritos;
    private AccionAtras accionAtras;
    private AccionAdelante accionAdelante;
    private AccionMotorBusqueda accionMotorBusqueda;

    public BarraNavegacion() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        campoURL = new JTextField(40);
        campoURL.setPreferredSize(new Dimension(500, 28));
        campoURL.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        botonAtras = new JButton("‹");
        botonAdelante = new JButton("›");
        botonRecargar = new JButton("↻");
        botonFavorito = new JButton("☆");
        botonIr = new JButton("Ir");
        botonVerFavoritos = new JButton("Favoritos");
        botonOpciones = new JButton("Opciones");
        botonBuscar = new JButton("Buscar");

        botonIr.setEnabled(false);

        menuOpcionesPopup = new JPopupMenu();

        configurarBotonIcono(botonRecargar);
        configurarBotonIcono(botonFavorito);

        Dimension tamBotonTexto = new Dimension(95, 28);
        configurarBotonTexto(botonIr, new Dimension(55, 28));
        configurarBotonTexto(botonVerFavoritos, tamBotonTexto);
        configurarBotonTexto(botonOpciones, tamBotonTexto);
        configurarBotonTexto(botonBuscar, new Dimension(80, 28));
        configurarBotonFlecha(botonAtras);
        configurarBotonFlecha(botonAdelante);

        botonAtras.setEnabled(false);
        botonAdelante.setEnabled(false);

        configurarEventos();
        configurarHoverBotones();

        add(botonAtras);
        add(botonAdelante);
        add(botonRecargar);
        add(botonFavorito);
        add(campoURL);
        add(botonIr);
        add(botonVerFavoritos);
        add(botonOpciones);
        add(botonBuscar);
        aplicarTema(false);
    }

    private void configurarBotonIcono(JButton boton) {
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setPreferredSize(new Dimension(46, 30));
        boton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        boton.setMargin(new Insets(-3, 0, 0, 0));
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setVerticalAlignment(SwingConstants.CENTER);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarBotonFlecha(JButton boton) {
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setPreferredSize(new Dimension(46, 30));

        boton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));

        boton.setMargin(new Insets(-4, 0, 0, 0));
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setVerticalAlignment(SwingConstants.CENTER);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarBotonTexto(JButton boton, Dimension tamano) {
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setPreferredSize(tamano);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarEventos() {
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

        botonAtras.addActionListener(e -> {
            if (accionAtras != null) {
                accionAtras.alAtras();
            }
        });

        botonAdelante.addActionListener(e -> {
            if (accionAdelante != null) {
                accionAdelante.alAdelante();
            }
        });

        botonBuscar.addActionListener(e -> {
            if (accionMotorBusqueda != null) {
                accionMotorBusqueda.alAbrirMotorBusqueda();
            }
        });
    }

    private void configurarHoverBotones() {
        JButton[] botones = {
                botonIr,
                botonRecargar,
                botonFavorito,
                botonVerFavoritos,
                botonOpciones,
                botonAtras,
                botonAdelante,
                botonBuscar
        };

        for (JButton b : botones) {
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (b == botonAtras || b == botonAdelante || b == botonRecargar || b == botonFavorito) {
                        b.setOpaque(false);
                        b.setContentAreaFilled(false);
                        b.setForeground(new Color(139, 58, 71));
                    } else {
                        b.setOpaque(true);
                        b.setContentAreaFilled(true);
                        b.setBackground(new Color(180, 215, 255));
                        b.setForeground(Color.BLACK);
                    }
                    b.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    aplicarTemaBoton(b);
                    b.repaint();
                }
            });
        }
    }

    private void aplicarTemaBoton(JButton boton) {
        Color texto;
        Color fondoBoton;

        if (modoOscuro) {
            texto = new Color(255, 230, 235);
            fondoBoton = new Color(92, 28, 41);
        } else {
            texto = new Color(74, 20, 32);
            fondoBoton =  new Color(255, 218, 224);
        }

        boton.setForeground(texto);

        if (boton == botonRecargar || boton == botonFavorito || boton == botonAtras || boton == botonAdelante) {
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);
        } else {
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setBackground(fondoBoton);
        }
    }

    private void validarCampoVacio() {
        botonIr.setEnabled(!campoURL.getText().trim().isEmpty());
    }

    private void ejecutarNavegacion() {
        if (accionNavegacion != null && !getURL().isEmpty()) {
            String urlIngresada = getURL();

            if (!urlIngresada.startsWith("http://") && !urlIngresada.startsWith("https://") && !urlIngresada.startsWith("file:///")) {
                if (urlIngresada.contains("/") || urlIngresada.contains("\\") || urlIngresada.toLowerCase().endsWith(".html")) {
                    urlIngresada = "file:///" + urlIngresada.replace("\\", "/");
                }
            }

            accionNavegacion.alNavegar(urlIngresada);
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

    public void aplicarTema(boolean modoOscuro) {
        this.modoOscuro = modoOscuro;

        Color fondo;
        Color colorBorde;

        if (modoOscuro) {
            fondo = new Color(60, 60, 60);
            colorBorde = new Color(92, 28, 41);
            campoURL.setBackground(new Color(80, 80, 80));
            campoURL.setForeground(Color.WHITE);
            campoURL.setCaretColor(Color.WHITE);
        } else {
            fondo = new Color(230, 168, 184);
            colorBorde = Color.BLACK;
            campoURL.setBackground(Color.WHITE);
            campoURL.setForeground(Color.BLACK);
            campoURL.setCaretColor(Color.BLACK);
        }

        setBackground(fondo);

        campoURL.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        aplicarTemaBoton(botonIr);
        aplicarTemaBoton(botonRecargar);
        aplicarTemaBoton(botonFavorito);
        aplicarTemaBoton(botonVerFavoritos);
        aplicarTemaBoton(botonOpciones);
        aplicarTemaBoton(botonAdelante);
        aplicarTemaBoton(botonAtras);
        aplicarTemaBoton(botonBuscar);
    }

    public boolean esURLLocalValida() {
        String url = getURL();
        return !url.isEmpty() && url.startsWith("file:///");
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

    public void setAccionAtras(AccionAtras accionAtras) {
        this.accionAtras = accionAtras;
    }

    public void setAccionAdelante(AccionAdelante accionAdelante) {
        this.accionAdelante = accionAdelante;
    }

    public void setAccionMotorBusqueda(AccionMotorBusqueda accionMotorBusqueda) {
        this.accionMotorBusqueda = accionMotorBusqueda;
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

    public interface AccionAtras {
        void alAtras();
    }

    public interface AccionAdelante {
        void alAdelante();
    }

    public interface AccionMotorBusqueda {
        void alAbrirMotorBusqueda();
    }

    public interface AccionAbrirArchivo {
        void alAbrirArchivo();
    }

    private ActionListener listenerBotonIrOffline;
    private AccionAbrirArchivo accionAbrirArchivo;

    public void setAccionAbrirArchivo(AccionAbrirArchivo accion) {
        this.accionAbrirArchivo = accion;
    }

    public void setModoOffline(boolean offline) {
        if (offline) {
            botonIr.setText("📁");
            botonIr.setEnabled(true);
            botonIr.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            campoURL.setEnabled(false);
            if (listenerBotonIrOffline == null) {
                listenerBotonIrOffline = e -> {
                    if (accionAbrirArchivo != null) {
                        accionAbrirArchivo.alAbrirArchivo();
                    }
                };
            }
            botonIr.addActionListener(listenerBotonIrOffline);
        } else {
            botonIr.setText("Ir");
            botonIr.setFont(UIManager.getFont("Button.font"));
            campoURL.setEnabled(true);
            campoURL.setToolTipText(null);
            validarCampoVacio();
            if (listenerBotonIrOffline != null) {
                botonIr.removeActionListener(listenerBotonIrOffline);
            }
        }
        repaint();
    }
    
    public void actualizarBotonesHistorial(
            boolean puedeAtras,
            boolean puedeAdelante) {

        botonAtras.setEnabled(puedeAtras);
        botonAdelante.setEnabled(puedeAdelante);
    }
}
