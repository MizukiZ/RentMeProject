package Helper;

import android.location.Location;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemFilterHandler {

    private List<HashMap<String, Object>> data;

    public ItemFilterHandler(ArrayList<HashMap<String, Object>> itemListData){

        this.data = itemListData;
    }


    public List<HashMap<String, Object>> nearByFilter(Double cLat,Double cLon,int distance) {

      List<String> nearItemIdsArray = new ArrayList<>();

        for (HashMap<String, Object> item : data ) {
@SuppressWarnings("unchecked")
            Map<String,Double> locationMap = (Map<String,Double>)item.get("location");

            float[] results = new float[1];

                                // compare current location and item's location
                                Location.distanceBetween(
                                        cLat,
                                        cLon,
                                        locationMap.get("lat"),
                                        locationMap.get("lon"),
                                       results
                                       );

                                // get result in km
                                float resultInKm = results[0] / 1000;

                              if(distance >= resultInKm){
                                  // if the result is less than distance set the id to array
                                    nearItemIdsArray.add(item.get("id").toString());
                             }
        }

        // filter the original itemdData by the distance
        List<HashMap<String, Object>> filteredata = Stream.of(data).filter(
                item-> nearItemIdsArray.contains(item.get("id")))
        .collect(Collectors.toList());


        return filteredata;
    }
}
