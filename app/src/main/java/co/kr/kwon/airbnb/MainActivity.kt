package co.kr.kwon.airbnb

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import co.kr.kwon.airbnb.Const.Companion.BASE_URL
import co.kr.kwon.airbnb.data.api.HouseService
import co.kr.kwon.airbnb.data.model.HouseDto
import co.kr.kwon.airbnb.data.model.HouseModel
import co.kr.kwon.airbnb.ui.adapter.HouseListAdapter
import co.kr.kwon.airbnb.ui.adapter.HouseViewPagerAdapter
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.houseViewPager)
    }
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recyclerView)
    }
    private val currentLocationBtn: LocationButtonView by lazy {
        findViewById(R.id.currentLocationBtn)
    }
    private  val bottomSheetTitleTxt: TextView by lazy {
        findViewById(R.id.bottomSheetTitleTxt)
    }

    private val viewPagerAdapter = HouseViewPagerAdapter(itemClickListener = {
        val intent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요!!] ${it.title} ${it.price} 사진보기 : ${it.imgUrl}")
                type = "text/plain"
            }
        startActivity(Intent.createChooser(intent, null))
    })
    private val recyclerAdapter = HouseListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)

        viewPager.adapter = viewPagerAdapter

        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouse = viewPagerAdapter.currentList[position]
                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(selectedHouse.lat, selectedHouse.lng))
                        .animate(CameraAnimation.Fly)

                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.maxZoom = 21.0
        naverMap.minZoom = 0.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.502006, 126.767886))
        naverMap.moveCamera(cameraUpdate)

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = false

        currentLocationBtn.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        /*      val marker = Marker()
              marker.position = LatLng(37.505738, 126.753082)
              marker.map = naverMap
              marker.icon = MarkerIcons.BLUE
              marker.iconTintColor = Color.GREEN*/

        getHouseListFromAPI()

    }

    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                .enqueue(object : Callback<HouseDto> {
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if (response.isSuccessful.not()) {
                            return
                        }
                        response.body()?.let { dto ->
                            updateMarker(dto.items)
                            viewPagerAdapter.submitList(dto.items)
                            recyclerAdapter.submitList(dto.items)

                            bottomSheetTitleTxt.text = "${dto.items.size}개의 숙소"
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {

                    }

                })
        }
    }

    private fun updateMarker(house: List<HouseModel>) {
        house.forEach { house ->
            val marker = Marker()
            marker.position = LatLng(house.lat, house.lng)
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = house.id
            marker.icon = MarkerIcons.LIGHTBLUE
            marker.iconTintColor = Color.TRANSPARENT

        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onClick(overlay: Overlay): Boolean {
        val selectModel = viewPagerAdapter.currentList.firstOrNull() { item ->
            item.id == overlay.tag
        }
        selectModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true
    }

}