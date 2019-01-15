package ga.julen.busemt;

public class Parada {

    private int id;
    private String direccion;
    private double latitud;
    private double longitud;
    private String lineas;

    public Parada(int id, String direccion, double latitud, double longitud, String lineas) {
        this.id = id;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.lineas = lineas;
    }

    public int getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getLineas() {
        return lineas;
    }
}
