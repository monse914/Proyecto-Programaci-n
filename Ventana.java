import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class Ventana extends JFrame {

    private BarraNavegacion barra;
    private JTextPane areaTexto;
    private JTabbedPane pestanas;
    private JLabel barraEstado;
    private JProgressBar barraProgreso;
    private Renderizador renderizador;
    private JLabel tituloVentana;
    private MenuSimple menu;
    private ClienteHTTP clienteHTTP;

    private JPanel panelSuperior;
    private JPanel barraTitulo;
    private JPanel barraBotones;
    private JPanel panelEstado;

    private JButton btnMin;
    private JButton btnMax;
    private JButton btnClose;

    private Map<String, Component> pestanasPorUrl;
    private Map<Component, JLabel> etiquetasPestanas;
    private Map<Component, String> urlPorPestana;

    // mover ventana
    private int xMouse;
    private int yMouse;

    // redimensionamiento
    private static final int BORDE = 8;
    private boolean redimensionando = false;
    private boolean resizeN;
    private boolean resizeS;
    private boolean resizeE;
    private boolean resizeW;
    private int xInicio;
    private int yInicio;
    private int anchoInicio;
    private int altoInicio;
    private int frameXInicio;
    private int frameYInicio;

    public Ventana(String urlInicial) {

        setUndecorated(true);
        setSize(800, 600);
        setMinimumSize(new Dimension(200, 200));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        renderizador = new Renderizador();
        clienteHTTP = new ClienteHTTP();
        pestanasPorUrl = new HashMap<>();
        etiquetasPestanas = new HashMap<>();
        urlPorPestana = new HashMap<>();

        panelSuperior = new JPanel(new BorderLayout());

        barraTitulo = new JPanel(new BorderLayout());
        barraTitulo.setPreferredSize(new Dimension(800, 30));

        tituloVentana = new JLabel(" Navegador Web");
        tituloVentana.setFont(new Font("Arial", Font.BOLD, 13));

        barraBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barraBotones.setOpaque(false);

        btnMin = new JButton("-");
        btnMax = new JButton("❑");
        btnClose = new JButton("x");

        JButton[] botones = {btnMin, btnMax, btnClose};

        for (JButton b : botones) {
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setPreferredSize(new Dimension(50, 25));
        }

        configurarHoverBotonesVentana();
        configurarMoverVentana();

        btnMin.addActionListener(e -> setState(JFrame.ICONIFIED));

        btnMax.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        btnClose.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro que deseas cerrar la aplicación?",
                    "Confirmar salida",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcion == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        barraBotones.add(btnMin);
        barraBotones.add(btnMax);
        barraBotones.add(btnClose);

        barraTitulo.add(tituloVentana, BorderLayout.WEST);
        barraTitulo.add(barraBotones, BorderLayout.EAST);

        barra = new BarraNavegacion();

        panelSuperior.add(barraTitulo, BorderLayout.NORTH);
        panelSuperior.add(barra, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        areaTexto = new JTextPane();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 14));

        pestanas = new JTabbedPane();
        add(pestanas, BorderLayout.CENTER);

        JScrollPane scrollInicial = new JScrollPane(areaTexto);
        agregarPestana("Inicio", scrollInicial, null);

        detectarClicks(areaTexto);
        detectarHoverLinks(areaTexto);

        menu = new MenuSimple(areaTexto, this::aplicarTemaGeneral);
        setJMenuBar(menu);

        panelEstado = new JPanel(new BorderLayout());
        panelEstado.setPreferredSize(new Dimension(800, 28));

        barraEstado = new JLabel(" Listo");
        barraEstado.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        barraProgreso = new JProgressBar();
        barraProgreso.setPreferredSize(new Dimension(140, 18));
        barraProgreso.setStringPainted(false);
        barraProgreso.setVisible(false);

        panelEstado.add(barraEstado, BorderLayout.WEST);
        panelEstado.add(barraProgreso, BorderLayout.EAST);

        add(panelEstado, BorderLayout.SOUTH);

        barra.setAccionNavegacion(url -> abrirUrlEnPestana(url));

        aplicarTemaGeneral(false, Color.WHITE, Color.BLACK);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opcion = JOptionPane.showConfirmDialog(
                        Ventana.this,
                        "¿Estás seguro que deseas cerrar la aplicación?",
                        "Confirmar salida",
                        JOptionPane.YES_NO_OPTION
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        habilitarRedimensionamiento();

        setVisible(true);

        if (urlInicial != null && !urlInicial.isEmpty()) {
            barra.setURL(urlInicial);
            cargarPaginaEnComponente(urlInicial, scrollInicial);
        }
    }

    private void cargarPaginaWeb(String url) {
        String urlMostrada = url;

        if (!urlMostrada.startsWith("http://") && !urlMostrada.startsWith("https://")) {
            urlMostrada = "http://" + urlMostrada;
        }

        JTextPane nuevaArea = new JTextPane();
        nuevaArea.setEditable(false);
        nuevaArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane nuevoScroll = new JScrollPane(nuevaArea);

        aplicarTemaArea(nuevaArea);

        agregarPestana(urlMostrada, nuevoScroll, null);
        pestanas.setSelectedComponent(nuevoScroll);
        barra.setURL(urlMostrada);

        mostrarEstadoCargando();

        try {
            String respuesta = clienteHTTP.obtenerRespuesta(urlMostrada);
            barraEstado.setText(" " + clienteHTTP.getEstado());
            nuevaArea.setText(respuesta);
            renderizador.aplicarTemaTextoCompleto(nuevaArea, menu.isModoOscuro());

        } catch (Exception e) {
            nuevaArea.setText(e.getMessage());
            barraEstado.setText(" Error de conexión");
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void abrirUrlEnPestana(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        url = url.trim();

        if (!url.startsWith("file:///")) {
            cargarPaginaWeb(url);
            return;
        }

        String urlNormalizada = normalizarUrl(url);

        Component pestanaExistente = pestanasPorUrl.get(urlNormalizada);

        if (pestanaExistente != null) {
            pestanas.setSelectedComponent(pestanaExistente);
            barra.setURL(urlNormalizada);
            barraEstado.setText(" Listo");
            return;
        }

        JTextPane nuevaArea = new JTextPane();
        nuevaArea.setEditable(false);
        nuevaArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane nuevoScroll = new JScrollPane(nuevaArea);

        detectarClicks(nuevaArea);
        detectarHoverLinks(nuevaArea);
        aplicarTemaArea(nuevaArea);

        agregarPestana(urlNormalizada, nuevoScroll, null);
        pestanas.setSelectedComponent(nuevoScroll);
        barra.setURL(urlNormalizada);

        cargarPaginaEnComponente(urlNormalizada, nuevoScroll);
    }

    private void cargarPaginaEnComponente(String url, Component componente) {
        String urlNormalizada = normalizarUrl(url);
        barra.setURL(urlNormalizada);
        mostrarEstadoCargando();

        barraProgreso.setVisible(true);
        barraProgreso.setIndeterminate(true);

        Timer timer = new Timer(350, e -> {
            barraProgreso.setIndeterminate(false);
            barraProgreso.setVisible(false);

            try {
                String ruta = obtenerRutaArchivo(urlNormalizada);
                JTextPane area = getAreaDeComponente(componente);

                renderizador.renderizarArchivo(ruta, area);
                menu.aplicarTemaActual(area);
                renderizador.aplicarTemaTextoCompleto(area, menu.isModoOscuro());
                area.setCaretPosition(0);

                String titulo = renderizador.obtenerTitulo(ruta);
                if (titulo == null || titulo.isEmpty() || titulo.equals("Sin título")) {
                    titulo = new File(ruta).getName();
                }

                if (titulo.length() > 20) {
                    titulo = titulo.substring(0, 20) + "...";
                }

                actualizarTituloPestana(componente, titulo);
                pestanasPorUrl.put(urlNormalizada, componente);
                urlPorPestana.put(componente, urlNormalizada);

                barraEstado.setText(" Listo");

            } catch (Exception ex) {
                JTextPane area = getAreaDeComponente(componente);
                manejarErrorDeCarga(ex, area);
            }
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void manejarErrorDeCarga(Exception ex, JTextPane area) {
        String mensaje = ex.getMessage();

        if (mensaje == null) {
            mensaje = "Error al cargar archivo";
        }

        if (mensaje.equals("Error: El archivo no es HTML")) {
            area.setText("Error: El archivo no es HTML");
            barraEstado.setText(" Error: Formato no soportado");
            JOptionPane.showMessageDialog(this, "Error: El archivo no es HTML");
            return;
        }

        if (mensaje.equals("Error: Archivo no encontrado")) {
            area.setText("Error: Archivo no encontrado");
            barraEstado.setText(" Error: Archivo no encontrado");
            JOptionPane.showMessageDialog(this, "Error: Archivo no encontrado");
            return;
        }

        area.setText(mensaje);
        barraEstado.setText(" Error");
        JOptionPane.showMessageDialog(this, mensaje);
    }

    private String obtenerRutaArchivo(String url) {
        if (!url.startsWith("file:///")) {
            return url;
        }

        return url.substring(8);
    }

    private String normalizarUrl(String url) {
        if (url == null) {
            return null;
        }

        url = url.trim();

        if (!url.startsWith("file:///")) {
            return url;
        }

        try {
            String ruta = url.substring(8);
            File archivo = new File(ruta);

            if (!archivo.isAbsolute()) {
                archivo = new File(ruta);
            }

            return "file:///" + archivo.getCanonicalPath().replace("\\", "/");
        } catch (Exception e) {
            return url;
        }
    }

    private void mostrarEstadoCargando() {
        barraEstado.setText(" Cargando...");
    }

    private void agregarPestana(String titulo, JScrollPane scroll, String url) {
        pestanas.addTab(titulo, scroll);

        int index = pestanas.indexOfComponent(scroll);
        JPanel encabezado = crearEncabezadoPestana(scroll, titulo);

        pestanas.setTabComponentAt(index, encabezado);
        etiquetasPestanas.put(scroll, (JLabel) encabezado.getComponent(0));

        if (url != null) {
            String urlNormalizada = normalizarUrl(url);
            pestanasPorUrl.put(urlNormalizada, scroll);
            urlPorPestana.put(scroll, urlNormalizada);
        }
    }

    private JPanel crearEncabezadoPestana(Component componentePestana, String titulo) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        JButton btnCerrar = new JButton("x");

        btnCerrar.setMargin(new Insets(0, 4, 0, 4));
        btnCerrar.setFocusable(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        btnCerrar.addActionListener(e -> cerrarPestana(componentePestana));

        MouseAdapter seleccionarPestana = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pestanas.setSelectedComponent(componentePestana);
            }
        };

        panel.addMouseListener(seleccionarPestana);
        lblTitulo.addMouseListener(seleccionarPestana);

        panel.add(lblTitulo);
        panel.add(btnCerrar);

        return panel;
    }

    private void cerrarPestana(Component componente) {
        String url = urlPorPestana.get(componente);

        if (url != null) {
            pestanasPorUrl.remove(url);
            urlPorPestana.remove(componente);
        }

        etiquetasPestanas.remove(componente);
        pestanas.remove(componente);

        if (pestanas.getTabCount() == 0) {
            JTextPane nuevaArea = new JTextPane();
            nuevaArea.setEditable(false);
            nuevaArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

            JScrollPane scroll = new JScrollPane(nuevaArea);
            detectarClicks(nuevaArea);
            detectarHoverLinks(nuevaArea);
            aplicarTemaArea(nuevaArea);

            agregarPestana("Inicio", scroll, null);
            pestanas.setSelectedComponent(scroll);
        }
    }

    private void actualizarTituloPestana(Component componente, String titulo) {
        JLabel lbl = etiquetasPestanas.get(componente);

        if (lbl != null) {
            lbl.setText(titulo);
        }

        int index = pestanas.indexOfComponent(componente);
        if (index >= 0) {
            pestanas.setTitleAt(index, titulo);
        }
    }

    private JTextPane getAreaDeComponente(Component comp) {
        if (comp instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) comp;
            return (JTextPane) scroll.getViewport().getView();
        }
        return areaTexto;
    }

    private void aplicarTemaArea(JTextPane area) {
        menu.aplicarTemaActual(area);
        renderizador.aplicarTemaTextoCompleto(area, menu.isModoOscuro());
    }

    private void aplicarTemaGeneral(boolean modoOscuro, Color fondoArea, Color textoArea) {
        Color fondoPrincipal;
        Color fondoSecundario;
        Color textoPrincipal;
        Color fondoPestanas;

        if (modoOscuro) {
            fondoPrincipal = new Color(45, 45, 45);
            fondoSecundario = new Color(60, 60, 60);
            textoPrincipal = Color.WHITE;
            fondoPestanas = new Color(50, 50, 50);
        } else {
            fondoPrincipal = new Color(230, 230, 230);
            fondoSecundario = new Color(245, 245, 245);
            textoPrincipal = Color.BLACK;
            fondoPestanas = new Color(240, 240, 240);
        }

        getContentPane().setBackground(fondoSecundario);

        panelSuperior.setBackground(fondoSecundario);
        barraTitulo.setBackground(fondoPrincipal);
        tituloVentana.setForeground(textoPrincipal);
        barraBotones.setBackground(fondoPrincipal);

        barra.setBackground(fondoSecundario);
        barra.aplicarTema(modoOscuro);

        pestanas.setBackground(fondoPestanas);
        pestanas.setForeground(textoPrincipal);

        panelEstado.setBackground(fondoPrincipal);
        barraEstado.setForeground(textoPrincipal);

        actualizarColoresBotones(modoOscuro);

        int i = 0;
        while (i < pestanas.getTabCount()) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                scroll.getViewport().setBackground(fondoArea);

                Component vista = scroll.getViewport().getView();
                if (vista instanceof JTextPane) {
                    JTextPane area = (JTextPane) vista;
                    area.setBackground(fondoArea);
                    area.setForeground(textoArea);
                    renderizador.aplicarTemaTextoCompleto(area, modoOscuro);
                }
            }
            i++;
        }

        for (JLabel lbl : etiquetasPestanas.values()) {
            lbl.setForeground(textoPrincipal);
        }

        repaint();
    }

    private void actualizarColoresBotones(boolean modoOscuro) {
        if (modoOscuro) {
            btnMin.setBackground(new Color(70, 70, 70));
            btnMin.setForeground(Color.WHITE);

            btnMax.setBackground(new Color(70, 70, 70));
            btnMax.setForeground(Color.WHITE);

            btnClose.setBackground(new Color(140, 45, 45));
            btnClose.setForeground(Color.WHITE);
        } else {
            btnMin.setBackground(Color.YELLOW);
            btnMin.setForeground(Color.BLACK);

            btnMax.setBackground(Color.GREEN);
            btnMax.setForeground(Color.BLACK);

            btnClose.setBackground(Color.RED);
            btnClose.setForeground(Color.WHITE);
        }
    }

    private void configurarHoverBotonesVentana() {
        JButton[] botones = {btnMin, btnMax, btnClose};

        for (JButton b : botones) {
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (b == btnClose) {
                        b.setBackground(new Color(200, 60, 60));
                    } else {
                        b.setBackground(b.getBackground().brighter());
                    }

                    b.setBorderPainted(true);
                    b.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
                }

                public void mouseExited(MouseEvent e) {
                    actualizarColoresBotones(menu != null && menu.isModoOscuro());
                    b.setBorderPainted(false);
                }
            });
        }
    }

    private void configurarMoverVentana() {
        MouseAdapter moverVentana = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH && !redimensionando) {
                    xMouse = e.getXOnScreen();
                    yMouse = e.getYOnScreen();
                    frameXInicio = getX();
                    frameYInicio = getY();
                }
            }

            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH && !redimensionando) {
                    int dx = e.getXOnScreen() - xMouse;
                    int dy = e.getYOnScreen() - yMouse;
                    setLocation(frameXInicio + dx, frameYInicio + dy);
                }
            }
        };

        barraTitulo.addMouseListener(moverVentana);
        barraTitulo.addMouseMotionListener(moverVentana);
        tituloVentana.addMouseListener(moverVentana);
        tituloVentana.addMouseMotionListener(moverVentana);
    }

    private void habilitarRedimensionamiento() {
        MouseAdapter resizeAdapter = new MouseAdapter() {

            public void mouseMoved(MouseEvent e) {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }

                Point p = SwingUtilities.convertPoint(
                        e.getComponent(),
                        e.getPoint(),
                        getRootPane()
                );

                actualizarDireccionResize(p);
                actualizarCursor();
            }

            public void mousePressed(MouseEvent e) {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    return;
                }

                Point p = SwingUtilities.convertPoint(
                        e.getComponent(),
                        e.getPoint(),
                        getRootPane()
                );

                actualizarDireccionResize(p);

                redimensionando = resizeN || resizeS || resizeE || resizeW;

                xInicio = e.getXOnScreen();
                yInicio = e.getYOnScreen();
                anchoInicio = getWidth();
                altoInicio = getHeight();
                frameXInicio = getX();
                frameYInicio = getY();
            }

            public void mouseDragged(MouseEvent e) {
                if (!redimensionando || getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    return;
                }

                int dx = e.getXOnScreen() - xInicio;
                int dy = e.getYOnScreen() - yInicio;

                int nuevoX = frameXInicio;
                int nuevoY = frameYInicio;
                int nuevoAncho = anchoInicio;
                int nuevoAlto = altoInicio;

                int minAncho = getMinimumSize().width;
                int minAlto = getMinimumSize().height;

                if (resizeE) {
                    nuevoAncho = Math.max(minAncho, anchoInicio + dx);
                }

                if (resizeS) {
                    nuevoAlto = Math.max(minAlto, altoInicio + dy);
                }

                if (resizeW) {
                    nuevoAncho = Math.max(minAncho, anchoInicio - dx);
                    nuevoX = frameXInicio + (anchoInicio - nuevoAncho);
                }

                if (resizeN) {
                    nuevoAlto = Math.max(minAlto, altoInicio - dy);
                    nuevoY = frameYInicio + (altoInicio - nuevoAlto);
                }

                setBounds(nuevoX, nuevoY, nuevoAncho, nuevoAlto);
            }

            public void mouseReleased(MouseEvent e) {
                redimensionando = false;
                resizeN = false;
                resizeS = false;
                resizeE = false;
                resizeW = false;
                setCursor(Cursor.getDefaultCursor());
            }
        };

        registrarEventosRedimension(this, resizeAdapter);
        registrarEventosRedimension(getRootPane(), resizeAdapter);
        registrarEventosRedimension(getContentPane(), resizeAdapter);
    }

    private void registrarEventosRedimension(Component comp, MouseAdapter adapter) {
        comp.addMouseListener(adapter);
        comp.addMouseMotionListener(adapter);

        if (comp instanceof Container) {
            Component[] hijos = ((Container) comp).getComponents();
            for (Component hijo : hijos) {
                registrarEventosRedimension(hijo, adapter);
            }
        }
    }

    private void actualizarDireccionResize(Point p) {
        int w = getWidth();
        int h = getHeight();

        resizeN = p.y <= BORDE;
        resizeS = p.y >= h - BORDE;
        resizeW = p.x <= BORDE;
        resizeE = p.x >= w - BORDE;
    }

    private void actualizarCursor() {
        if ((resizeN && resizeW) || (resizeS && resizeE)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        } else if ((resizeN && resizeE) || (resizeS && resizeW)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        } else if (resizeN || resizeS) {
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        } else if (resizeE || resizeW) {
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void detectarClicks(JTextPane area) {
        area.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int pos = area.viewToModel2D(e.getPoint());
                String ruta = renderizador.getRutaEnlaceEn(area, pos);

                if (ruta != null) {
                    String urlNormalizada = normalizarUrl(ruta);
                    barra.setURL(urlNormalizada);

                    Component pestanaExistente = pestanasPorUrl.get(urlNormalizada);

                    if (pestanaExistente != null) {
                        pestanas.setSelectedComponent(pestanaExistente);
                        barraEstado.setText(" Listo");
                    } else {
                        abrirUrlEnPestana(urlNormalizada);
                    }
                }
            }
        });
    }

    private void detectarHoverLinks(JTextPane area) {
        area.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int pos = area.viewToModel2D(e.getPoint());

                if (renderizador.esEnlace(area, pos)) {
                    area.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    area.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

                renderizador.resaltarEnlaceEn(pos, area, menu.isModoOscuro());
            }
        });
    }
}
