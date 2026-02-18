package com.deepdefender.nagarsewahackthon.Admin; // <-- Replace correctly

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.deepdefender.nagarsewahackthon.R;

import java.util.ArrayList;


import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class AnalyticsActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required for OSMDroid
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );

        setContentView(R.layout.activity_analytics);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);

        GeoPoint center = new GeoPoint(18.5204, 73.8567); // Example location
        map.getController().setCenter(center);

        // 🔥 Heatmap Points
        addHeatPoint(18.5204, 73.8567, 800);
        addHeatPoint(18.5215, 73.8580, 600);
        addHeatPoint(18.5190, 73.8550, 900);
    }

    private void addHeatPoint(double lat, double lon, double radius) {

        GeoPoint center = new GeoPoint(lat, lon);

        // Get circle points
        ArrayList<GeoPoint> circlePoints =
                Polygon.pointsAsCircle(center, radius);

        // Create polygon
        Polygon circle = new Polygon();
        circle.setPoints(circlePoints);

        circle.setFillColor(Color.argb(100, 255, 0, 0));
        circle.setStrokeColor(Color.TRANSPARENT);
        circle.setStrokeWidth(0f);

        map.getOverlays().add(circle);
    }


    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
