package Helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.mizuki.rentmeproject.ItemDetailActivity;

import java.util.List;

public class GeocodeHandler {

    Geocoder gc;
    Context context;
    Double lat, lon;

    public GeocodeHandler(Context context, Double lat, Double lon) {
        this.context = context;
        this.lat = lat;
        this.lon = lon;
    }

    public String getPlaceName() {

        String locationAddress = "";

        // do reverse geocoding here
        Geocoder gc = new Geocoder(context);

        try {
            List<Address> list = gc.getFromLocation(
                    // put post lat and lon
                    lat,
                    lon,
                    1);

            // get first result
            Address address = list.get(0);

            // create template of the address name
            StringBuffer addressStr = new StringBuffer();
            if(!address.getLocality().isEmpty()){
                addressStr.append(address.getLocality() + ", ");
            }

            if(!address.getAdminArea().isEmpty()){
                addressStr.append(address.getAdminArea() + " ");
            }

            if(!address.getPostalCode().isEmpty()){
                addressStr.append(address.getPostalCode());
            }


            locationAddress = addressStr.toString();

            return locationAddress;

        } catch (Exception e) {
            Log.d("Geocoding", e.getMessage());
        }
        return locationAddress;
    }


}
