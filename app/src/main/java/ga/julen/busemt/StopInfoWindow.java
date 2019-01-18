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
        TextView lblNombre = view.findViewById(R.id.name);
        TextView lblDetalles = view.findViewById(R.id.details);
        Parada parada = (Parada) marker.getTag();
        lblNombre.setText(marker.getTitle());
        lblDetalles.setText(marker.getSnippet());
        return view;
    }

}
