package com.mapzen.location;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LocationHelperTest {
    private LocationHelper locationHelper;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;
    private TestConnectionCallbacks connectionCallbacks;

    @Before
    public void setUp() throws Exception {
        connectionCallbacks = new TestConnectionCallbacks();
        locationHelper = new LocationHelper(application, connectionCallbacks);
        locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        locationHelper.connect();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(locationHelper).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void getLastLocation_shouldThrowExceptionIfNotConnected() throws Exception {
        locationHelper = new LocationHelper(application, connectionCallbacks);
        locationHelper.getLastLocation();
    }

    @Test
    public void connect_shouldCallOnConnected() throws Exception {
        assertThat(connectionCallbacks.connected).isTrue();
    }

    @Test
    public void disconnect_shouldCallOnDisconnected() throws Exception {
        locationHelper.disconnect();
        assertThat(connectionCallbacks.connected).isFalse();
    }

    @Test
    public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
        assertThat(locationHelper.getLastLocation()).isNull();
    }

    @Test
    public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
        Location location = new Location(PASSIVE_PROVIDER);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnMostAccurateResult() throws Exception {
        Location gpsLocation = new Location(GPS_PROVIDER);
        gpsLocation.setAccuracy(1000);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

        Location networkLocation = new Location(NETWORK_PROVIDER);
        networkLocation.setAccuracy(100);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

        Location passiveLocation = new Location(PASSIVE_PROVIDER);
        passiveLocation.setAccuracy(10);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, passiveLocation);

        assertThat(locationHelper.getLastLocation()).isEqualTo(passiveLocation);
    }

    @Test
    public void requestLocationUpdates_shouldRegisterGpsListener() throws Exception {
        LocationListener listener = new TestLocationListener();
        locationHelper.requestLocationUpdates(LocationRequest.create(), listener);
        List<LocationListener> list = shadowLocationManager.getRequestLocationUpdateListeners();
        assertThat(list).hasSize(1);
        assertThat(list).contains(listener);
    }

    class TestConnectionCallbacks implements LocationHelper.ConnectionCallbacks {
        private boolean connected = false;

        @Override
        public void onConnected(Bundle connectionHint) {
            connected = true;
        }

        @Override
        public void onDisconnected() {
            connected = false;
        }
    }

    class TestLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
