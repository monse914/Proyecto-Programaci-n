import java.util.Stack;
import java.util.ArrayList;

public class Historial {
    private Stack<String>atras;
    private Stack<String>adelante;
    private String actual;
    private ArrayList<String> paginas;

    public Historial(){
        atras = new Stack<>();
        adelante = new Stack<>();
        paginas = new ArrayList<>();
        actual = null;
    }
    
    public void navegar(String url){
        if(actual != null){
            atras.push(actual);
        }
        
        actual = url;
        paginas.add(url);
        
        if(paginas.size() > 10){
            paginas.remove(0);
        }
        adelante.clear();
    }
    
    public String atras(){
        if(atras. isEmpty()){
            return actual;
        }
        adelante.push(actual);
        actual= atras.pop();

        return actual;
    }

    public String adelante(){
        if(adelante. isEmpty()){
            return actual; 
        }
        atras.push(actual);
        actual= adelante.pop();

        return actual;
    }
    public String getActual(){
        return actual;
    }
    public boolean puedeAtras(){
        return !atras.isEmpty();
    }
    public boolean puedeAdelante(){
        return !adelante.isEmpty();
    }
    public ArrayList<String> getPaginas(){
    return paginas;
    }
    public java.util.List<String> obtenerHistorial(){
        java.util.List<String> lista = new java.util.ArrayList<>();
        lista.addAll(atras);
        if(actual != null){
            lista.add(actual);
        }
        return lista;
    }
}
