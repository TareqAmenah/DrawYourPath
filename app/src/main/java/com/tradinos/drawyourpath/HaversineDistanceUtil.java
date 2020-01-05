package com.tradinos.drawyourpath;

import com.google.android.gms.maps.model.LatLng;

public class HaversineDistanceUtil {


    final static int R = 6371; // Radious of the earth

    public static double betwenToePoint(LatLng latLng1, LatLng latLng2){
        Double lat1 = latLng1.latitude;
        Double lon1 = latLng1.longitude;
        Double lat2 = latLng2.latitude;
        Double lon2 = latLng2.longitude;
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;

        return distance;
    }


    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

}
