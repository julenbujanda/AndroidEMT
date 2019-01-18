package ga.julen.busemt;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class StopInfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public StopInfoWindow(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.info_window, null);
        Parada parada = (Parada) marker.getTag();
        TextView lblNombre = view.findViewById(R.id.name);
        TextView lblDireccion = view.findViewById(R.id.address);
        TextView lblLineas = view.findViewById(R.id.lines);
        lblNombre.setText("Parada " + parada.getId());
        lblDireccion.setText(parada.getDireccion());
        lblLineas.setText("Líneas: " + parada.getLineas());
        return view;
    }

}
