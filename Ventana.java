import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.net.URL;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class Ventana extends JFrame {

    private Pestania pestaniaInicial;
    private JTabbedPane pestanas;
    private JLabel barraEstado;
    private JProgressBar barraProgreso;
    private Renderizador renderizador;
    private JLabel tituloVentana;
    private MenuSimple menu;
    private Gemini gemini;
    private ClienteHTTP clienteHTTP;
    private Historial historial;
    private Favoritos favoritos;
    private NavegaOffline navegaOffline;

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
    private JButton btnIA;
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
        setMinimumSize(new Dimension(400, 300));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        renderizador = new Renderizador();
        clienteHTTP = new ClienteHTTP();
        historial = new Historial();
        favoritos = new Favoritos();
        navegaOffline = new NavegaOffline();

        pestanasPorUrl = new HashMap<>();
        etiquetasPestanas = new HashMap<>();
        urlPorPestana = new HashMap<>();

        panelSuperior = new JPanel(new BorderLayout());

        barraTitulo = new JPanel(new BorderLayout());
        barraTitulo.setPreferredSize(new Dimension(800, 30));

        tituloVentana = new JLabel("");
        tituloVentana.setFont(new Font("Arial", Font.BOLD, 13));

        barraBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barraBotones.setOpaque(false);

        btnMin = new JButton("—");
        btnMax = new JButton("☐");
        btnClose = new JButton("✕");

        JButton[] botones = {btnMin, btnMax, btnClose};

        for (JButton b : botones) {
            aplicarEstiloBotonIcono(b);
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
        
        btnAtras.setEnabled(false);
        btnAdelante.setEnabled(false);
        
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
                    navegarEnPestaniaActual(p, url, false);
                    actualizarBotonesNavegacion();
                }
            }
        });
        
        btnAdelante.addActionListener(e -> {
            Component comp = pestanas.getSelectedComponent();
            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                String url = p.adelante();
                if (url != null) {
                    navegarEnPestaniaActual(p, url, false);
                    actualizarBotonesNavegacion();
                }
            }
        });

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        izquierda.setOpaque(false);
        izquierda.add(tituloVentana);

        barraPestanasVisual = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        barraPestanasVisual.setOpaque(false);
        JPanel panelTabsYBotones = new JPanel(new BorderLayout());
        panelTabsYBotones.setOpaque(false);

        panelTabsYBotones.add(barraPestanasVisual, BorderLayout.CENTER);
        panelTabsYBotones.add(barraBotones, BorderLayout.EAST);

        barraTitulo.add(panelTabsYBotones, BorderLayout.CENTER);

        panelSuperior.add(barraTitulo, BorderLayout.NORTH);
        add(panelSuperior, BorderLayout.NORTH);

        pestanas = new JTabbedPane();
        pestanas.setBorder(BorderFactory.createEmptyBorder());
        pestanas.setFocusable(false);
        pestanas.setRequestFocusEnabled(false);

        pestanas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {

            @Override
            protected void installDefaults() {
                super.installDefaults();
                contentBorderInsets = new Insets(0, 0, 0, 0);
                tabAreaInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0;
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            }
        });

        pestanas.setBorder(BorderFactory.createEmptyBorder());
        pestanas.setFocusable(false);
        pestanas.setRequestFocusEnabled(false);
        pestanas.setOpaque(false);

        add(pestanas, BorderLayout.CENTER);

        configurarAtajosTeclado();
        agregarBotonNuevaPestana();

        pestaniaInicial = new Pestania();

        menu = new MenuSimple(
                pestaniaInicial.getAreaContenido(),
                this::aplicarTemaGeneral,
                this::mostrarHistorial
        );
        pestaniaInicial.getBarraNavegacion().configurarMenuOpciones(menu);

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
                actualizarEstrellaFavorito(p);
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
        crearBotonFlotanteIA();

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
            navegarEnPestaniaActual(pestania, url, true);
        });

        pestania.getBarraNavegacion().setAccionRecargar(() -> {
            String url = pestania.getUrlActual();

            if (url != null && !url.isEmpty()) {
                navegarEnPestaniaActual(pestania, url, false);
            }
        });

        pestania.getBarraNavegacion().setAccionFavorito(() -> {
            cambiarFavorito(pestania);
        });

        pestania.getBarraNavegacion().setAccionMostrarFavoritos(() -> {
            mostrarFavoritos(pestania);
        });

        aplicarTemaArea(pestania.getAreaContenido());
        actualizarEstrellaFavorito(pestania);

        if (menu != null) {
            pestania.getBarraNavegacion().configurarMenuOpciones(menu);
        }

        pestania.getBarraNavegacion().setAccionModo(() -> {
            cambiarModoNavegacion(pestania);
        });

        pestania.getBarraNavegacion().setAccionAtras(() -> {
            String url = pestania.atras();

            if (url != null) {
                navegarEnPestaniaActual(pestania, url, false);
            }

            actualizarBotonesHistorial(pestania);
        });

        pestania.getBarraNavegacion().setAccionAdelante(() -> {
            String url = pestania.adelante();

            if (url != null) {
                navegarEnPestaniaActual(pestania, url, false);
            }

            actualizarBotonesHistorial(pestania);
        });
    }

    private void actualizarBotonesHistorial(Pestania pestania) {
        if (pestania == null) {
            return;
        }

        pestania.getBarraNavegacion().actualizarBotonesHistorial(
                pestania.puedeAtras(),
                pestania.puedeAdelante()
        );
    }

    private void cambiarModoNavegacion(Pestania pestania) {

        navegaOffline.cambiarModo();
        actualizarTextoBotonesModo();

        if (navegaOffline.estaEnModoOffline()) {

            barraEstado.setText(" Modo Offline");

            String archivo = navegaOffline.seleccionarArchivoHTML(this);

            if (archivo == null) {
                return;
            }

            if (!navegaOffline.esArchivoHTML(archivo)) {
                JOptionPane.showMessageDialog(
                        this,
                        "En modo offline solo se permiten archivos HTML."
                );
                return;
            }

            pestania.getBarraNavegacion().setURL(archivo);
            navegarEnPestaniaActual(pestania, archivo, true);

        } else {

            barraEstado.setText(" Modo Online");
        }
    }

    private void actualizarTextoBotonesModo() {
        int i = 0;

        while (i < pestanas.getTabCount()) {
            Component comp = pestanas.getComponentAt(i);

            if (comp instanceof Pestania) {
                Pestania p = (Pestania) comp;
                p.getBarraNavegacion().setTextoModo(
                        navegaOffline.estaEnModoOffline()
                );
            }

            i++;
        }
    }

    private void cambiarFavorito(Pestania pestania) {
        String url = pestania.getUrlActual();

        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay una URL para guardar.");
            return;
        }

        int index = pestanas.indexOfComponent(pestania);
        String titulo = url;

        if (index >= 0) {
            titulo = pestanas.getTitleAt(index);
        }

        if (favoritos.esFavorito(url)) {
            favoritos.eliminar(url);
        } else {
            favoritos.agregar(url, titulo);
        }

        actualizarEstrellaFavorito(pestania);
    }

    private void actualizarEstrellaFavorito(Pestania pestania) {
        if (pestania == null) {
            return;
        }

        String url = pestania.getUrlActual();

        if (url == null) {
            pestania.getBarraNavegacion().setFavoritoActivo(false);
            return;
        }

        pestania.getBarraNavegacion().setFavoritoActivo(favoritos.esFavorito(url));
    }

    private void mostrarFavoritos(Pestania pestaniaActual) {
        java.util.List<Favoritos.EntradaFavorito> listaFavoritos = favoritos.getFavoritos();

        if (listaFavoritos.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay páginas favoritas guardadas.",
                    "Favoritos",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        DefaultListModel<String> modelo = new DefaultListModel<>();

        int i = 0;
        while (i < listaFavoritos.size()) {
            Favoritos.EntradaFavorito fav = listaFavoritos.get(i);
            modelo.addElement(fav.getTitulo() + " - " + fav.getUrl());
            i++;
        }

        JList<String> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(500, 300));

        JDialog dialogo = new JDialog(this, "Favoritos", true);
        dialogo.setLayout(new BorderLayout());

        JLabel instruccion = new JLabel(" Haz doble click en un favorito para abrirlo");
        dialogo.add(instruccion, BorderLayout.NORTH);
        dialogo.add(scroll, BorderLayout.CENTER);

        JButton btnAbrir = new JButton("Abrir");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnAbrir);
        panelBotones.add(btnCerrar);

        dialogo.add(panelBotones, BorderLayout.SOUTH);

        btnAbrir.addActionListener(e -> {
            abrirFavoritoSeleccionado(lista, listaFavoritos, pestaniaActual);
            dialogo.dispose();
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        lista.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirFavoritoSeleccionado(lista, listaFavoritos, pestaniaActual);
                    dialogo.dispose();
                }
            }
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void abrirFavoritoSeleccionado(
            JList<String> lista,
            java.util.List<Favoritos.EntradaFavorito> listaFavoritos,
            Pestania pestaniaActual
    ) {
        int index = lista.getSelectedIndex();

        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un favorito.");
            return;
        }

        String url = listaFavoritos.get(index).getUrl();

        if (pestaniaActual != null) {
            navegarEnPestaniaActual(pestaniaActual, url, true);
        } else {
            abrirUrlEnPestana(url);
        }
    }

    private void navegarEnPestaniaActual(Pestania pestania, String url, boolean agregarAlHistorial) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        url = url.trim();

        if (navegaOffline.estaEnModoOffline()) {
            if (!url.startsWith("file:///")) {
                JOptionPane.showMessageDialog(this,
                        "Estás en modo offline. Solo puedes abrir archivos HTML del computador."
                );
                return;
            }
            cargarPaginaEnComponente(url, pestania);
            return;
        }
        
        if (!url.startsWith("file:///")) {
            cargarPaginaWebEnPestania(pestania, url, agregarAlHistorial);
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

            cargarPaginaWebEnPestania(nuevaPestania, url, true);
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

    private void cargarPaginaWebEnPestania(Pestania pestania, String url, boolean esta) {
        
        if (esDominio(url)) {
            barraEstado.setText(" Dominio detectado: " + url);
        }else if (esIP(url)) {
            barraEstado.setText(" Dirección IP detectada: " + url);
        }else if (esIPmasPuerto(url)) {
            barraEstado.setText(" IP con puerto detectada: " + url);
        }

        String urlMostrada = url.trim();
        
        if (!urlMostrada.startsWith("http://") && !urlMostrada.startsWith("https://")) {
            if (esDominio(urlMostrada)
                || esIP(urlMostrada)
                || esIPmasPuerto(urlMostrada)) {
                    urlMostrada = "https://" + urlMostrada;
                }
            }

        pestania.setUrlActual(urlMostrada);
        pestania.getBarraNavegacion().setURL(urlMostrada);
        if (esta) {
            pestania.navegar(urlMostrada);
        }
        actualizarBotonesHistorial(pestania);
        actualizarBotonesNavegacion();

        mostrarEstadoCargando();

        String finalUrl = urlMostrada;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return clienteHTTP.obtenerRespuesta(finalUrl);
            }

            @Override
            protected void done() {

                try {
                    String html = get();
                    System.out.println("Tamaño HTML: " + html.length());
                    System.out.println(html.substring(0, Math.min(1000, html.length())));
                    
                    JTextPane area = pestania.getAreaContenido();
                    
                    area.setContentType("text/html");
                    area.setEditable(false);
                    
                    System.out.println("HTML:");
                    System.out.println(html);
                    
                    area.setText(html);
                    area.setCaretPosition(0);

                    String estado = clienteHTTP.getEstado();

                    pestania.setEstado(estado);

                    barraEstado.setText(" " + estado);

                    String tituloPagina = obtenerTituloDesdeHTML(html, finalUrl);

                    historial.agregar(finalUrl, tituloPagina);

                    actualizarTituloPestana(pestania, tituloPagina);

                    actualizarEstrellaFavorito(pestania);

                } catch (Exception e) {

                    JTextPane area = pestania.getAreaContenido();

                    area.setContentType("text/plain");

                    String mensaje = e.getMessage();

                    if (mensaje == null || mensaje.trim().isEmpty()) {
                        mensaje = "Error de conexión";
                    }

                    if (mensaje.startsWith("java.io.IOException: ")) {
                        mensaje = mensaje.substring("java.io.IOException: ".length());
                    }

                    area.setText(
                            "Error de conexión:\n" + mensaje
                    );

                    barraEstado.setText(" " + mensaje);

                    JOptionPane.showMessageDialog(
                            Ventana.this,
                            mensaje
                    );
                }
            }
        };

        worker.execute();
    }

    private void cargarPaginaEnComponente(String url, Component componente) {
        String urlNormalizada = normalizarUrl(url);

        mostrarEstadoCargando();

        Timer timer = new Timer(350, e -> {

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
                pestania.navegar(urlNormalizada);
                actualizarBotonesNavegacion();
                actualizarBotonesHistorial(pestania);
                pestania.getBarraNavegacion().setURL(urlNormalizada);

                actualizarEstrellaFavorito(pestania);

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
        barraProgreso.setVisible(false);
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

        aplicarEstiloBotonIcono(btnNuevaPestana);
        btnNuevaPestana.setOpaque(true);
        btnNuevaPestana.setContentAreaFilled(true);
        btnNuevaPestana.setBackground(Color.WHITE);

        btnNuevaPestana.setPreferredSize(
                new Dimension(28, 22)
        );

        btnNuevaPestana.setFont(
                btnNuevaPestana.getFont().deriveFont(
                        Font.BOLD,
                        14f
                )
        );

        btnNuevaPestana.setMargin(
                new Insets(-1, 0, 0, 0)
        );

        btnNuevaPestana.setBackground(Color.WHITE);
        btnNuevaPestana.setForeground(Color.BLACK);

        btnNuevaPestana.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210),
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                1,
                                5,
                                1,
                                5
                        )
                )
        );

        btnNuevaPestana.addMouseListener(
                new MouseAdapter() {

                    public void mouseEntered(MouseEvent e) {

                        btnNuevaPestana.setBackground(
                                new Color(180, 215, 255)
                        );
                    }

                    public void mouseExited(MouseEvent e) {

                        btnNuevaPestana.setBackground(
                                Color.WHITE
                        );
                    }
                }
        );

        btnNuevaPestana.addActionListener(
                e -> crearNuevaPestanaVacia()
        );

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

                JPanel tabVisual =
                        crearBotonPestanaSuperior(comp, titulo);

                barraPestanasVisual.add(tabVisual);
            }

            i++;
        }

        if (btnNuevaPestana != null) {

            JPanel panelMas =
                    new JPanel(new FlowLayout(
                            FlowLayout.LEFT,
                            0,
                            0
                    ));

            panelMas.setOpaque(false);

            panelMas.setPreferredSize(
                    new Dimension(38, 22)
            );

            panelMas.setMinimumSize(
                    new Dimension(38, 22)
            );

            panelMas.setMaximumSize(
                    new Dimension(38, 22)
            );

            panelMas.add(btnNuevaPestana);

            barraPestanasVisual.add(panelMas);
        }

        barraPestanasVisual.revalidate();
        barraPestanasVisual.repaint();
    }

    private JPanel crearBotonPestanaSuperior(Component componentePestana, String titulo) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));

        panel.setPreferredSize(new Dimension(145, 24));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,210,210), 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 2)
        ));

        boolean seleccionada = pestanas.getSelectedComponent() == componentePestana;

        if (seleccionada) {
            panel.setBackground(Color.WHITE);
        } else {
            panel.setBackground(new Color(230, 230, 230));
        }

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setVerticalAlignment(SwingConstants.CENTER);
        lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);

        if (titulo.length() > 15) {
            lblTitulo.setText(titulo.substring(0, 15) + "...");
        }

        JButton btnCerrar = new JButton("×");
        aplicarEstiloBotonIcono(btnCerrar);
        btnCerrar.setPreferredSize(new Dimension(16, 16));
        btnCerrar.setMargin(new Insets(-3,0,0,0));
        btnCerrar.setVerticalAlignment(SwingConstants.CENTER);
        btnCerrar.setHorizontalAlignment(SwingConstants.CENTER);
        btnCerrar.setFont(
                btnCerrar.getFont().deriveFont(15f)
        );

        btnCerrar.setOpaque(false);
        btnCerrar.setContentAreaFilled(false);

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
                btnCerrar.setOpaque(true);
                btnCerrar.setContentAreaFilled(true);
                btnCerrar.setBackground(new Color(180, 215, 255));
            }

            public void mouseExited(MouseEvent e) {
                btnCerrar.setOpaque(false);
                btnCerrar.setContentAreaFilled(false);
            }
        });

        panel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(btnCerrar, BorderLayout.EAST);

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
            fondoPrincipal = new Color(235, 235, 235);
            fondoSecundario = new Color(240, 240, 240);
            textoPrincipal = Color.BLACK;
            fondoPestanas = new Color(225, 225, 225);
        }

        getContentPane().setBackground(fondoSecundario);
        getRootPane().setBorder(null);

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

    private void mostrarHistorial() {
        java.util.List<Historial.EntradaHistorial> entradas = historial.getEntradas();

        if (entradas.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay páginas visitadas.",
                    "Historial",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        DefaultListModel<String> modelo = new DefaultListModel<>();

        int i = 0;
        while (i < entradas.size()) {
            Historial.EntradaHistorial entrada = entradas.get(i);
            modelo.addElement(entrada.getTitulo() + " - " + entrada.getUrl());
            i++;
        }

        JList<String> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(500, 300));

        JDialog dialogo = new JDialog(this, "Historial de navegación", true);
        dialogo.setLayout(new BorderLayout());

        JLabel instruccion = new JLabel(" Haz doble click en una página para abrirla");
        dialogo.add(instruccion, BorderLayout.NORTH);
        dialogo.add(scroll, BorderLayout.CENTER);

        JButton btnAbrir = new JButton("Abrir");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnAbrir);
        panelBotones.add(btnCerrar);

        dialogo.add(panelBotones, BorderLayout.SOUTH);

        btnAbrir.addActionListener(e -> {
            abrirSeleccionHistorial(lista, entradas);
            dialogo.dispose();
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        lista.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirSeleccionHistorial(lista, entradas);
                    dialogo.dispose();
                }
            }
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private String obtenerTituloDesdeHTML(String html, String url) {
        try {
            String lower = html.toLowerCase();

            int inicio = lower.indexOf("<title>");
            int fin = lower.indexOf("</title>");

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String titulo = html.substring(inicio + 7, fin).trim();

                titulo = titulo.replace("&amp;", "&");
                titulo = titulo.replace("&lt;", "<");
                titulo = titulo.replace("&gt;", ">");
                titulo = titulo.replace("&quot;", "\"");

                if (!titulo.isEmpty()) {
                    return titulo;
                }
            }
        } catch (Exception e) {
        }

        return url;
    }

    private void abrirSeleccionHistorial(
            JList<String> lista,
            java.util.List<Historial.EntradaHistorial> entradas
    ) {
        int index = lista.getSelectedIndex();

        if (index < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una página del historial.",
                    "Historial",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Historial.EntradaHistorial entrada = entradas.get(index);
        String url = entrada.getUrl();

        Component comp = pestanas.getSelectedComponent();

        if (comp instanceof Pestania) {
            Pestania pestaniaActual = (Pestania) comp;
            navegarEnPestaniaActual(pestaniaActual, url, false);
        } else {
            abrirUrlEnPestana(url);
        }
    }

    private void actualizarColoresBotones(boolean modoOscuro) {
        Color fondoBoton;
        Color textoBoton;

        if (modoOscuro) {
            fondoBoton = new Color(65, 65, 65);
            textoBoton = Color.WHITE;
        } else {
            fondoBoton = new Color(235, 235, 235);
            textoBoton = Color.BLACK;
        }

        btnMin.setBackground(fondoBoton);
        btnMin.setForeground(textoBoton);
        btnMin.setOpaque(true);

        btnMax.setBackground(fondoBoton);
        btnMax.setForeground(textoBoton);
        btnMax.setOpaque(true);

        btnClose.setBackground(fondoBoton);
        btnClose.setForeground(textoBoton);
        btnClose.setOpaque(true);
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

    private void configurarHoverBotonesVentana() {
        JButton[] botones = {btnMin, btnMax, btnClose};

        for (JButton b : botones) {
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (b == btnClose) {
                        b.setBackground(new Color(230, 70, 70));
                        b.setForeground(Color.WHITE);
                    } else {
                        b.setBackground(new Color(180, 215, 255));
                        b.setForeground(Color.BLACK);
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
                int pos = area.viewToModel(e.getPoint());
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
                int pos = area.viewToModel(e.getPoint());

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
    private boolean esDominio(String texto) {
        return texto.matches("^(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(:[0-9]{1,5})?$");
    }

    private boolean esIP(String texto) {
        return texto.matches("^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\\.){3}" + "(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})$");
    }

    private boolean esIPmasPuerto(String texto){
        return texto.matches("^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\\.){3}" + "(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2}):[0-9]{1,5}$");
    }
    
    private boolean esDireccionWeb(String texto) {
        return texto.startsWith("http://")
                || texto.startsWith("https://")
                || esDominio(texto)
                || esIPmasPuerto(texto)
                || esIP(texto);
            }

    private void actualizarEstadoModo() {
        if (navegaOffline.estaEnModoOffline()) {
            barraEstado.setText(" Modo Offline");
        } else {
            barraEstado.setText(" Modo Online");
        }
    }
    
    private void crearBotonFlotanteIA() {
        btnIA = new JButton("✦") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(new Color(0, 120, 255));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    
                    int x = (getWidth() - fm.stringWidth("✦")) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2 - 4;
                    
                    g2.setColor(Color.WHITE);
                    g2.drawString("✦", x, y);

                    g2.dispose();
                }
            };
            btnIA.setContentAreaFilled(false);
            btnIA.setBorderPainted(false);
            btnIA.setFocusPainted(false);
            btnIA.setOpaque(false);

            btnIA.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));

            btnIA.setBounds(
                getWidth() - 100,
                getHeight() - 140, 65, 65);
                
                final Point[] click = new Point[1];
                
                btnIA.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        click[0] = e.getPoint();
                    }
                });
                btnIA.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        int x = btnIA.getX() + e.getX() - click[0].x;
                        
                        int y = btnIA.getY() + e.getY() - click[0].y;
                        
                        btnIA.setLocation(x, y);
                    }
                });
                btnIA.addActionListener(e -> abrirAsistenteIA());
                getLayeredPane().add(btnIA, JLayeredPane.DRAG_LAYER);
                getLayeredPane().revalidate();
                getLayeredPane().repaint();
        }

            private void abrirAsistenteIA() {
                JDialog dialogo = new JDialog(this, "Gemini", false);
                dialogo.setSize(500, 400);
                dialogo.setLocationRelativeTo(this);
                dialogo.setLayout(new BorderLayout());
                JTextArea areaChat = new JTextArea();
                areaChat.setEditable(false);
                
                JScrollPane scroll = new JScrollPane(areaChat);
                JTextField campoPregunta = new JTextField();
                
                JButton btnEnviar = new JButton("Enviar");
                
                JPanel panelInferior = new JPanel(new BorderLayout());
                
                panelInferior.add(campoPregunta, BorderLayout.CENTER);
                panelInferior.add(btnEnviar, BorderLayout.EAST);
                
                dialogo.add(scroll, BorderLayout.CENTER);
                dialogo.add(panelInferior, BorderLayout.SOUTH);
                
                btnEnviar.addActionListener(e -> {
                    String pregunta =
                    campoPregunta.getText().trim();
                    
                    if (pregunta.isEmpty()) {
                        return;
                    }
                    
                    areaChat.append("Tú: " + pregunta + "\n\n");
                    campoPregunta.setText("");
                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                        
                        @Override
                        protected String doInBackground() throws Exception {
                            
                            Gemini gemini = new Gemini();
                            
                            return gemini.preguntar(pregunta);
                        }
                        
                        @Override
                        protected void done() {
                            try {
                                
                                String respuesta = get();
                                areaChat.append("Gemini: " + respuesta+ "\n\n"
                                );
                            } catch (Exception ex) {
                                areaChat.append("Error: " + ex.getMessage() + "\n\n");
                                }
                            }
                        };
                        worker.execute();
                    });
                    campoPregunta.addActionListener(e -> btnEnviar.doClick());
                    dialogo.setDefaultCloseOperation(
                        JDialog.DO_NOTHING_ON_CLOSE);
                        dialogo.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                int opcion = JOptionPane.showConfirmDialog(dialogo,
                                    "¿Deseas cerrar el asistente IA?",
                                    "Confirmar cierre",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                                );
                                if (opcion == JOptionPane.YES_OPTION) {
                                    dialogo.dispose();
                                }
                            }
                        });
                dialogo.setVisible(true);
            }

            private void actualizarBotonesNavegacion() {
                Component comp = pestanas.getSelectedComponent();
                if (comp instanceof Pestania) {
                    Pestania p = (Pestania) comp;
                    btnAtras.setEnabled(p.puedeAtras());
                    btnAdelante.setEnabled(p.puedeAdelante());
                } else {
                    btnAtras.setEnabled(false);
                    btnAdelante.setEnabled(false);
                }
            }
        }
