import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BarraNavegacion extends JPanel {

    private JTextField campoURL;
    private JButton botonIr;
    private AccionNavegacion accionNavegacion;
    private JButton botonRecargar;
    private AccionRecargar accionRecargar;

    public BarraNavegacion() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        campoURL = new JTextField(30);
        campoURL.setPreferredSize(new Dimension(320, 28));

        botonIr = new JButton("Ir");
        botonRecargar = new JButton("↻");
        botonRecargar.setPreferredSize(new Dimension(50,28);
        
        botonIr.setEnabled(false);
        
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
        botonRecargar.addActionListener(e-> {
            if(accionRecargar != null){
              accionRecargar.alRecargar();
            }
        });

        add(campoURL);
        add(botonIr);
        add(botonRecargar);
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

    public void setAccionNavegacion(AccionNavegacion accionNavegacion) {
        this.accionNavegacion = accionNavegacion;
    }

    public void setAccionRecargar(AccionRecargar accionRecargar){
        this.accionRecargar = accionRecargar;
    }

    public boolean esURLLocalValida() {
        String url = getURL();
        return !url.isEmpty() && url.startsWith("file:///");
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
            fondo = new Color(245, 245, 245);
            texto = Color.BLACK;
            fondoBoton = new Color(230, 230, 230);
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
    }

    public interface AccionNavegacion {
        void alNavegar(String url);
        void alRecargar();
    }
}
