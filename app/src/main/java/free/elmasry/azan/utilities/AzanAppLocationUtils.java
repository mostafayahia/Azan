package free.elmasry.azan.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import free.elmasry.azan.ui.MainActivity;

public class AzanAppLocationUtils {

    /**
     * return the location of the user if there is a permission to access the location of the user
     * and the process of retrieving the location was successful, otherwise returning null
     * @param activity
     * @return
     */
    public static void processBasedOnLocation(Activity activity,
                                              final LocationSuccessHandler locationSuccessHandler) {
        boolean permissionGranted =
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // ask again
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MainActivity.MY_PERMISSION_LOCATION_REQUEST_CODE);
                return; // no point for continue
            }
        }


        // get the location of the user
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                locationSuccessHandler.onLocationSuccess(location);
            }
        });

    }

    /**
     * return true only if the user gives access to use his location
     * @param activity
     * @return
     */
    public static boolean locationPermissionGranted(Activity activity) {
        boolean permissionGranted =
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // ask again
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MainActivity.MY_PERMISSION_LOCATION_REQUEST_CODE);
                return false;
            }
        }

        return true;
    }

    /**
     * callback for the process if retrieving location was successful
     */
    public interface LocationSuccessHandler {
        void onLocationSuccess(Location location);
    }

    /**
     * this class just wrapper for longitude and latitude
     */
    public static class MyLocation {
        private double longitude;
        private double latitude;

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
