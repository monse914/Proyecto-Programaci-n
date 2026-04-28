import java.io.File;

public class Main {
    public static void main(String[] args) {
        String urlInicial = null;

        try {
            File index = new File("index.html");
            if (index.exists() && index.isFile()) {
                urlInicial = "file:///" + index.getCanonicalPath().replace("\\", "/");
            }
        } catch (Exception e) {
        }

        new Ventana(urlInicial);
    }
}