import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class Ventana extends JFrame {

    private Pestania pestaniaInicial;
    private JTabbedPane pestanas;
    private JLabel barraEstado;
    private JProgressBar barraProgreso;
    private Renderizador renderizador;
    private JLabel tituloVentana;
    private MenuSimple menu;
    private ClienteHTTP clienteHTTP;
    private Historial historial;

    private JPanel panelSuperior;
    private JPanel barraTitulo;
    private JPanel barraBotones;
    private JPanel panelEstado;
    private JPanel barraHist;
    private JPanel barraPestanasVisual;

    private JButton btnMin;
    private JButton btnMax;
    private JButton btnClose;
    private JButton btnAtras;
    private JButton btnAdelante;
    private JButton btnNuevaPestana;
    private Component pestanaBotonMas;

    private Map<String, Pestania> pestanasPorUrl;
    private Map<Component, JLabel> etiquetasPestanas;
    private Map<Pestania, String> urlPorPestana;

    private int xMouse;
    private int yMouse;

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
        historial = new Historial();

        pestanasPorUrl = new HashMap<>();
        etiquetasPestanas = new HashMap<>();
        urlPorPestana = new HashMap<>();

        panelSuperior = new JPanel(new BorderLayout());

        barraTitulo = new JPanel(new BorderLayout());
        barraTitulo.setPreferredSize(new Dimension(800, 34));

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

        btnClose.addActionListener(e -> confirmarCierre());

        barraBotones.add(btnMin);
        barraBotones.add(btnMax);
        barraBotones.add(btnClose);

        barraHist = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barraHist.setOpaque(false);
        btnAtras = new JButton("🡠");
        btnAdelante = new JButton("🡢");
        JButton[] botonesP = {btnAtras,btnAdelante};
        for (JButton bp : botonesP) {
            bp.setFocusPainted(false);
            bp.setBorderPainted(false);
            bp.setPreferredSize(new Dimension(50, 25));
        }
        barraHist.add(btnAtras);
        barraHist.add(btnAdelante);
        btnAtras.addActionListener(e -> {
            Component comp = pestanas.getSelectedComponent();
            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                String url = p.atras();
                if (url != null) {
                    navegarEnPestaniaActual(p, url);
                }
            }
        });
        btnAdelante.addActionListener(e -> {
            Component comp = pestanas.getSelectedComponent();
            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                String url = p.adelante();
                if (url != null) {
                    navegarEnPestaniaActual(p, url);
                }
            }
        });

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        izquierda.setOpaque(false);
        izquierda.add(tituloVentana);

        barraPestanasVisual = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        barraPestanasVisual.setOpaque(false);

        barraTitulo.add(izquierda, BorderLayout.WEST);
        barraTitulo.add(barraPestanasVisual, BorderLayout.CENTER);
        barraTitulo.add(barraBotones, BorderLayout.EAST);


        panelSuperior.add(barraTitulo, BorderLayout.NORTH);
        add(panelSuperior, BorderLayout.NORTH);

        pestanas = new JTabbedPane();
        pestanas.setFocusable(false);

        pestanas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0;
            }
        });

        add(pestanas, BorderLayout.CENTER);

        configurarAtajosTeclado();
        agregarBotonNuevaPestana();

        pestaniaInicial = new Pestania();

        menu = new MenuSimple(pestaniaInicial.getAreaContenido(), this::aplicarTemaGeneral);
        setJMenuBar(menu);

        configurarPestania(pestaniaInicial);
        agregarPestana("Inicio", pestaniaInicial, null);

        panelEstado = new JPanel(new BorderLayout());
        panelEstado.setPreferredSize(new Dimension(800, 28));

        barraEstado = new JLabel(" Listo");
        barraEstado.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        pestanas.addChangeListener(e -> {
            Component comp = pestanas.getSelectedComponent();
            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                String estado = p.getEstado();
                if (estado != null) {
                    barraEstado.setText(" " + estado);
                } else {
                    barraEstado.setText(" Listo");
                }
                actualizarBarraPestanasVisual();
            }});

        barraProgreso = new JProgressBar();
        barraProgreso.setPreferredSize(new Dimension(140, 18));
        barraProgreso.setStringPainted(false);
        barraProgreso.setVisible(false);

        panelEstado.add(barraEstado, BorderLayout.WEST);
        panelEstado.add(barraProgreso, BorderLayout.EAST);

        add(panelEstado, BorderLayout.SOUTH);

        aplicarTemaGeneral(false, Color.WHITE, Color.BLACK);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmarCierre();
            }
        });

        habilitarRedimensionamiento();

        setVisible(true);

        if (urlInicial != null && !urlInicial.isEmpty()) {
            pestaniaInicial.getBarraNavegacion().setURL(urlInicial);
            cargarPaginaEnComponente(urlInicial, pestaniaInicial);
        }
    }

    private void confirmarCierre() {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro que deseas cerrar la aplicación?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void configurarPestania(Pestania pestania) {
        detectarClicks(pestania.getAreaContenido());
        detectarHoverLinks(pestania.getAreaContenido());

        pestania.getBarraNavegacion().setAccionNavegacion(url -> {
            navegarEnPestaniaActual(pestania, url);
        });

        aplicarTemaArea(pestania.getAreaContenido());
    }

    private void navegarEnPestaniaActual(Pestania pestania, String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        url = url.trim();

        if (!url.startsWith("file:///")) {
            cargarPaginaWebEnPestania(pestania, url);
            return;
        }

        String urlNormalizada = normalizarUrl(url);

        Pestania pestaniaExistente = pestanasPorUrl.get(urlNormalizada);

        if (pestaniaExistente != null && pestaniaExistente != pestania) {
            pestanas.setSelectedComponent(pestaniaExistente);
            barraEstado.setText(" Listo");
            return;
        }

        cargarPaginaEnComponente(urlNormalizada, pestania);
    }

    private void abrirUrlEnPestana(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        url = url.trim();

        if (!url.startsWith("file:///")) {
            Pestania nuevaPestania = new Pestania();
            configurarPestania(nuevaPestania);

            agregarPestana(url, nuevaPestania, null);
            pestanas.setSelectedComponent(nuevaPestania);

            cargarPaginaWebEnPestania(nuevaPestania, url);
            return;
        }

        String urlNormalizada = normalizarUrl(url);

        Pestania pestaniaExistente = pestanasPorUrl.get(urlNormalizada);

        if (pestaniaExistente != null) {
            pestanas.setSelectedComponent(pestaniaExistente);
            barraEstado.setText(" Listo");
            return;
        }

        Pestania nuevaPestania = new Pestania();
        configurarPestania(nuevaPestania);

        agregarPestana(urlNormalizada, nuevaPestania, null);
        pestanas.setSelectedComponent(nuevaPestania);

        cargarPaginaEnComponente(urlNormalizada, nuevaPestania);
    }

    private void cargarPaginaWebEnPestania(Pestania pestania, String url) {
        String urlMostrada = url;

        if (!urlMostrada.startsWith("http://") && !urlMostrada.startsWith("https://")) {
            urlMostrada = "http://" + urlMostrada;
        }

        pestania.setUrlActual(urlMostrada);
        pestania.getHistorial().navegar(urlMostrada);
        pestania.getBarraNavegacion().setURL(urlMostrada);
        mostrarEstadoCargando();

        try {
            String respuesta = clienteHTTP.obtenerRespuesta(urlMostrada);

            JTextPane area = pestania.getAreaContenido();
            area.setText(respuesta);

            renderizador.aplicarTemaTextoCompleto(area, menu.isModoOscuro());
            String estado = clienteHTTP.getEstado();
            pestania.setEstado(estado);
            barraEstado.setText(" " + clienteHTTP.getEstado());

            actualizarTituloPestana(pestania, urlMostrada);
            historial.agregar(urlMostrada, urlMostrada);

        } catch (Exception e) {
            pestania.getAreaContenido().setText(e.getMessage());
            barraEstado.setText(" Error de conexión");
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void cargarPaginaEnComponente(String url, Component componente) {
        String urlNormalizada = normalizarUrl(url);

        mostrarEstadoCargando();

        barraProgreso.setVisible(true);
        barraProgreso.setIndeterminate(true);

        Timer timer = new Timer(350, e -> {
            barraProgreso.setIndeterminate(false);
            barraProgreso.setVisible(false);

            try {
                String ruta = obtenerRutaArchivo(urlNormalizada);

                Pestania pestania = (Pestania) componente;
                JTextPane area = pestania.getAreaContenido();

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

                historial.agregar(urlNormalizada, titulo);
                actualizarTituloPestana(pestania, titulo);

                pestanasPorUrl.put(urlNormalizada, pestania);
                urlPorPestana.put(pestania, urlNormalizada);

                pestania.setUrlActual(urlNormalizada);
                pestania.getBarraNavegacion().setURL(urlNormalizada);
                pestania.getHistorial().navegar(urlNormalizada);

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

            return "file:///" + archivo.getCanonicalPath().replace("\\", "/");
        } catch (Exception e) {
            return url;
        }
    }

    private void mostrarEstadoCargando() {
        barraEstado.setText(" Cargando...");
    }

    private void agregarPestana(String titulo, Pestania pestania, String url) {
        pestanas.addTab(titulo, pestania);

        if (url != null) {
            String urlNormalizada = normalizarUrl(url);
            pestanasPorUrl.put(urlNormalizada, pestania);
            urlPorPestana.put(pestania, urlNormalizada);
            pestania.setUrlActual(urlNormalizada);
            pestania.getBarraNavegacion().setURL(urlNormalizada);
        }

        actualizarBarraPestanasVisual();
    }

    private void agregarBotonNuevaPestana() {
        btnNuevaPestana = new JButton("+");
        btnNuevaPestana.setFocusPainted(false);
        btnNuevaPestana.setBorderPainted(false);
        btnNuevaPestana.setPreferredSize(new Dimension(36, 24));
        btnNuevaPestana.setBackground(new Color(255, 210, 220));
        btnNuevaPestana.setForeground(Color.BLACK);

        btnNuevaPestana.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnNuevaPestana.setBackground(new Color(255, 185, 200));
            }

            public void mouseExited(MouseEvent e) {
                btnNuevaPestana.setBackground(new Color(255, 210, 220));
            }
        });

        btnNuevaPestana.addActionListener(e -> crearNuevaPestanaVacia());

        actualizarBarraPestanasVisual();
    }

    private void actualizarBarraPestanasVisual() {
        if (barraPestanasVisual == null) {
            return;
        }

        barraPestanasVisual.removeAll();

        int i = 0;
        while (i < pestanas.getTabCount()) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof Pestania) {
                String titulo = pestanas.getTitleAt(i);
                JPanel tabVisual = crearBotonPestanaSuperior(comp, titulo);
                barraPestanasVisual.add(tabVisual);
            }

            i++;
        }

        if (btnNuevaPestana != null) {
            barraPestanasVisual.add(btnNuevaPestana);
        }

        barraPestanasVisual.revalidate();
        barraPestanasVisual.repaint();
    }

    private JPanel crearBotonPestanaSuperior(Component componentePestana, String titulo) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setPreferredSize(new Dimension(170, 24));

        boolean seleccionada = pestanas.getSelectedComponent() == componentePestana;

        if (seleccionada) {
            panel.setBackground(Color.WHITE);
        } else {
            panel.setBackground(new Color(255, 225, 235));
        }

        JLabel lblTitulo = new JLabel(titulo);

        if (titulo.length() > 16) {
            lblTitulo.setText(titulo.substring(0, 16) + "...");
        }

        JButton btnCerrar = new JButton("x");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setMargin(new Insets(0, 4, 0, 4));

        if (seleccionada) {
            btnCerrar.setBackground(Color.WHITE);
        } else {
            btnCerrar.setBackground(new Color(255, 225, 235));
        }

        btnCerrar.setForeground(Color.BLACK);

        MouseAdapter seleccionar = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pestanas.setSelectedComponent(componentePestana);
                actualizarBarraPestanasVisual();
            }
        };

        panel.addMouseListener(seleccionar);
        lblTitulo.addMouseListener(seleccionar);

        btnCerrar.addActionListener(e -> cerrarPestana(componentePestana));

        btnCerrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnCerrar.setBackground(new Color(255, 160, 175));
            }

            public void mouseExited(MouseEvent e) {
                if (pestanas.getSelectedComponent() == componentePestana) {
                    btnCerrar.setBackground(Color.WHITE);
                } else {
                    btnCerrar.setBackground(new Color(255, 225, 235));
                }
            }
        });

        panel.add(lblTitulo);
        panel.add(btnCerrar);

        return panel;
    }

    private void crearNuevaPestanaVacia() {
        Pestania nueva = new Pestania();
        configurarPestania(nueva);

        agregarPestana("Nueva pestaña", nueva, null);
        pestanas.setSelectedComponent(nueva);
    }

    private JPanel crearEncabezadoPestana(Component componentePestana, String titulo) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        JButton btnCerrar = new JButton("x");

        btnCerrar.setMargin(new Insets(0, 4, 0, 4));
        btnCerrar.setFocusable(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setBackground(new Color(255, 210, 220));
        btnCerrar.setForeground(Color.BLACK);

        btnCerrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnCerrar.setBackground(new Color(255, 160, 175));
            }

            public void mouseExited(MouseEvent e) {
                btnCerrar.setBackground(new Color(255, 210, 220));
            }
        });

        btnCerrar.addActionListener(e -> cerrarPestana(componentePestana));

        MouseAdapter seleccionarPestana = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
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
        if (!(componente instanceof Pestania)) {
            return;
        }

        int index = pestanas.indexOfComponent(componente);

        if (index == -1) {
            return;
        }

        int indexUltimaPestanaReal = obtenerIndexUltimaPestanaReal();

        if (index == indexUltimaPestanaReal) {
            JOptionPane.showMessageDialog(
                    this,
                    "No puedes eliminar la última pestaña"
            );
            return;
        }

        Pestania pestania = (Pestania) componente;

        String url = urlPorPestana.get(pestania);

        if (url != null) {
            pestanasPorUrl.remove(url);
            urlPorPestana.remove(pestania);
        }

        etiquetasPestanas.remove(pestania);
        pestanas.remove(pestania);
        actualizarBarraPestanasVisual();

        pestania.liberarRecursos();

        System.gc();
    }

    private int contarPestanasReales() {
        int contador = 0;
        int i = 0;

        while (i < pestanas.getTabCount()) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof Pestania) {
                contador++;
            }

            i++;
        }

        return contador;
    }

    private int obtenerIndexUltimaPestanaReal() {
        int i = pestanas.getTabCount() - 1;

        while (i >= 0) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof Pestania) {
                return i;
            }

            i--;
        }

        return -1;
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

        actualizarBarraPestanasVisual();
    }

    private JTextPane getAreaDeComponente(Component comp) {
        if (comp instanceof Pestania) {
            return ((Pestania) comp).getAreaContenido();
        }

        return pestaniaInicial.getAreaContenido();
    }

    private void aplicarTemaArea(JTextPane area) {
        if (menu != null) {
            menu.aplicarTemaActual(area);
            renderizador.aplicarTemaTextoCompleto(area, menu.isModoOscuro());
        }
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
            fondoPrincipal = new Color(255, 210, 220);
            fondoSecundario = new Color(255, 240, 245);
            textoPrincipal = Color.BLACK;
            fondoPestanas = new Color(255, 225, 235);
        }

        getContentPane().setBackground(fondoSecundario);

        panelSuperior.setBackground(fondoSecundario);
        barraTitulo.setBackground(fondoPrincipal);
        tituloVentana.setForeground(textoPrincipal);
        barraBotones.setBackground(fondoPrincipal);

        pestanas.setBackground(fondoPestanas);
        pestanas.setForeground(textoPrincipal);

        panelEstado.setBackground(fondoPrincipal);
        barraEstado.setForeground(textoPrincipal);

        actualizarColoresBotones(modoOscuro);

        int i = 0;
        while (i < pestanas.getTabCount()) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                p.aplicarTema(modoOscuro, fondoArea, textoArea, renderizador);
            }

            i++;
        }

        for (JLabel lbl : etiquetasPestanas.values()) {
            if (lbl != null) {
                lbl.setForeground(textoPrincipal);
            }
        }

        repaint();
    }

    private void actualizarColoresBotones(boolean modoOscuro) {
        Color fondoBoton;
        Color textoBoton;

        if (modoOscuro) {
            fondoBoton = new Color(65, 65, 65);
            textoBoton = Color.WHITE;
        } else {
            fondoBoton = new Color(255, 210, 220);
            textoBoton = Color.BLACK;
        }

        btnMin.setBackground(fondoBoton);
        btnMin.setForeground(textoBoton);

        btnMax.setBackground(fondoBoton);
        btnMax.setForeground(textoBoton);

        btnClose.setBackground(fondoBoton);
        btnClose.setForeground(textoBoton);
    }

    private void configurarHoverBotonesVentana() {
        JButton[] botones = {btnMin, btnMax, btnClose};

        for (JButton b : botones) {
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (b == btnClose) {
                        b.setBackground(new Color(255, 120, 140));
                    } else {
                        b.setBackground(new Color(255, 185, 200));
                    }
                }

                public void mouseExited(MouseEvent e) {
                    actualizarColoresBotones(menu != null && menu.isModoOscuro());
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

                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getRootPane());

                actualizarDireccionResize(p);
                actualizarCursor();
            }

            public void mousePressed(MouseEvent e) {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    return;
                }

                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getRootPane());

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

                    Component seleccionada = pestanas.getSelectedComponent();

                    if (seleccionada instanceof Pestania) {
                        ((Pestania) seleccionada).getBarraNavegacion().setURL(urlNormalizada);
                    }

                    Pestania pestaniaExistente = pestanasPorUrl.get(urlNormalizada);

                    if (pestaniaExistente != null) {
                        pestanas.setSelectedComponent(pestaniaExistente);
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

    private void configurarAtajosTeclado() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    public boolean dispatchKeyEvent(KeyEvent e) {

                        if (e.getID() != KeyEvent.KEY_PRESSED) {
                            return false;
                        }

                        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_TAB && !e.isShiftDown()) {
                            seleccionarSiguientePestanaReal();
                            return true;
                        }

                        if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_TAB) {
                            seleccionarAnteriorPestanaReal();
                            return true;
                        }

                        return false;
                    }
                });
    }

    private void seleccionarSiguientePestanaReal() {
        int total = pestanas.getTabCount();
        if (total == 0) {
            return;
        }

        int actual = pestanas.getSelectedIndex();
        int i = actual + 1;

        while (i != actual) {
            if (i >= total) {
                i = 0;
            }

            Component comp = pestanas.getComponentAt(i);
            if (comp instanceof Pestania) {
                pestanas.setSelectedIndex(i);
                return;
            }

            i++;
        }
    }

    private void seleccionarAnteriorPestanaReal() {
        int total = pestanas.getTabCount();
        if (total == 0) {
            return;
        }

        int actual = pestanas.getSelectedIndex();
        int i = actual - 1;

        while (i != actual) {
            if (i < 0) {
                i = total - 1;
            }

            Component comp = pestanas.getComponentAt(i);
            if (comp instanceof Pestania) {
                pestanas.setSelectedIndex(i);
                return;
            }

            i--;
        }
    }
}
