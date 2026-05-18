import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Favoritos {

    private List<EntradaFavorito> favoritos;
    private static final String ARCHIVO = "favoritos.txt";

    public Favoritos() {
        favoritos = new ArrayList<>();
        cargarDesdeArchivo();
    }

    public boolean esFavorito(String url) {

        int i = 0;

        while (i < favoritos.size()) {

            EntradaFavorito fav = favoritos.get(i);

            if (fav.getUrl().equals(url)) {
                return true;
            }

            i++;
        }

        return false;
    }

    public void agregar(String url, String titulo) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        url = url.trim();

        if (titulo == null || titulo.trim().isEmpty()) {
            titulo = url;
        }

        if (existe(url)) {
            return;
        }

        favoritos.add(new EntradaFavorito(url, titulo));
        guardarEnArchivo();
    }

    public void eliminar(String url) {
        if (url == null) {
            return;
        }

        int i = 0;
        while (i < favoritos.size()) {
            if (favoritos.get(i).getUrl().equals(url)) {
                favoritos.remove(i);
                guardarEnArchivo();
                return;
            }
            i++;
        }
    }

    public boolean existe(String url) {
        if (url == null) {
            return false;
        }

        int i = 0;
        while (i < favoritos.size()) {
            if (favoritos.get(i).getUrl().equals(url)) {
                return true;
            }
            i++;
        }

        return false;
    }

    public List<EntradaFavorito> getFavoritos() {
        return favoritos;
    }

    private void guardarEnArchivo() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO));

            int i = 0;
            while (i < favoritos.size()) {
                EntradaFavorito f = favoritos.get(i);
                pw.println(f.getTitulo() + "|" + f.getUrl());
                i++;
            }

            pw.close();
        } catch (IOException e) {
            System.out.println("Error al guardar favoritos");
        }
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(ARCHIVO);

        if (!archivo.exists()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = br.readLine()) != null) {
                int separador = linea.indexOf("|");

                if (separador != -1) {
                    String titulo = linea.substring(0, separador);
                    String url = linea.substring(separador + 1);
                    favoritos.add(new EntradaFavorito(url, titulo));
                }
            }

            br.close();
        } catch (IOException e) {
            System.out.println("Error al cargar favoritos");
        }
    }

    public static class EntradaFavorito {
        private String url;
        private String titulo;

        public EntradaFavorito(String url, String titulo) {
            this.url = url;
            this.titulo = titulo;
        }

        public String getUrl() {
            return url;
        }

        public String getTitulo() {
            return titulo;
        }
    }
}
