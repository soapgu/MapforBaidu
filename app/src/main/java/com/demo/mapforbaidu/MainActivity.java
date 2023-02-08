package com.demo.mapforbaidu;

import android.graphics.Point;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PrismOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.building.BuildingSearch;
import com.baidu.mapapi.search.building.BuildingSearchOption;
import com.baidu.mapapi.search.core.BuildingInfo;
import com.baidu.mapapi.search.core.SearchResult;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private BuildingSearch mBuildingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mMapView = findViewById(R.id.mapView);
        LatLng centerPoint = new LatLng(31.170474,121.401209);
        MapStatus mapStatus = new MapStatus.Builder().target(centerPoint).zoom(18).overlook(-30.0f).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //renderSimpleMarkPoint();
        BaiduMap map = this.mMapView.getMap();
        map.setMapStatus(mapStatusUpdate);
        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setOverlookingGesturesEnabled(true);
        //this.findViewById(R.id.button_building).setOnClickListener( v -> renderBuilding() );
        //renderBuilding();
        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
           @Override
           public void onMapLoaded() {
                render3DMarkPoint();
           }
        });
        renderPrism();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mMapView.onDestroy();
    }

    private void render3DMarkPoint(){
        //定义Maker坐标点
        Projection projection = this.mMapView.getMap().getProjection();
        LatLng point = new LatLng(31.170503, 121.402091);
        Point srcPoint = projection.geoPoint3toScreenLocation(point, 20);
        LatLng latLng = projection.fromScreenLocation(srcPoint);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.mark);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);


        OverlayOptions mTextOptions = new TextOptions()
                .text("我的起点") //文字内容
                //.bgColor(0xAAFFFF00) //背景色
                .fontSize(24) //字号
                .fontColor(0xFF000000) //文字颜色
                .position(latLng);

        //在地图上添加Marker，并显示
        this.mMapView.getMap().addOverlay(option);
        this.mMapView.getMap().addOverlay(mTextOptions);
    }

    private void renderPrism(){
        List<LatLng> locations = new ArrayList<>();
        locations.add(new LatLng(31.170561,121.40181));
        locations.add(new LatLng(31.170642,121.402066));
        locations.add(new LatLng(31.170437,121.402156));
        locations.add(new LatLng(31.170352,121.401905));

        PrismOptions prismOptions = new PrismOptions();
        prismOptions.setHeight(20);
        prismOptions.setPoints(locations);
        prismOptions.setSideFaceColor(0xAAFF0000);
        prismOptions.setTopFaceColor(0xAA00FF00);
        // 控制3D建筑物单体动画
        prismOptions.setAnimation(true);
        // 设置3D建筑物开始显示层级
        prismOptions.setShowLevel(4);
        mMapView.getMap().addOverlay(prismOptions);
    }

    private void renderBuilding() {
        // 设置3D建筑物的属性
        LatLng requestLatlng = new LatLng(31.170503, 121.402091);
        BuildingSearchOption buildingSearchOption = new BuildingSearchOption();
        buildingSearchOption.setLatLng(requestLatlng);
        // 检索对应的建筑物
        mBuildingSearch = BuildingSearch.newInstance();
        // 在检索成功的回调中绘制建筑物
        mBuildingSearch.setOnGetBuildingSearchResultListener(result -> {
            if (null == result || result.error != SearchResult.ERRORNO.NO_ERROR) {
                return;
            }

            // 楼面外接矩形建造器
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            // 获取建筑物的各个楼面
            for (int i = 0; i < result.getBuildingList().size(); i++) {
                BuildingInfo buildingInfo = result.getBuildingList().get(i);
                // 创建3D棱柱覆盖物选类配置参数
                PrismOptions prismOptions = new PrismOptions();
                prismOptions.setBuildingInfo(buildingInfo);
                prismOptions.setSideFaceColor(0xAAFF0000);
                prismOptions.setTopFaceColor(0xAA00FF00);
                // 控制3D建筑物单体动画
                prismOptions.setAnimation(true);
                // 设置3D建筑物开始显示层级
                prismOptions.setShowLevel(17);
                LatLngBounds latLngBounds = mMapView.getMap().getOverlayLatLngBounds(prismOptions);
                if (latLngBounds != null) {
                    boundsBuilder.include(latLngBounds.northeast).include(latLngBounds.southwest);
                }
                // 添加3D棱柱
                mMapView.getMap().addOverlay(prismOptions);
            }

            // 获取3D建筑物外接矩形
            LatLngBounds latLngBounds = boundsBuilder.build();
            // 令3D建筑物适应地图展示
            if (latLngBounds != null) {
                mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLngBounds(latLngBounds));
            }
        });
        mBuildingSearch.requestBuilding(buildingSearchOption);
    }
}