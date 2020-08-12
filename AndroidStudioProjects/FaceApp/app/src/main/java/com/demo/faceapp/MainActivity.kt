package com.demo.faceapp

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAnalyzer: MLFaceAnalyzer
    private lateinit var mLensEngine: LensEngine
    private lateinit var mFaceAnalyzerTransactor: FaceAnalyzerTransactor

    private var surfaceHolderCamera: SurfaceHolder? = null
    private var surfaceHolderOverlay: SurfaceHolder? = null

    private val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasPermissions(requiredPermissions))
            init()
        else
            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
    }

    private fun hasPermissions(permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty() && hasPermissions(requiredPermissions))
            init()
    }

    private fun init() {
        mAnalyzer = createAnalyzer()
        mFaceAnalyzerTransactor = FaceAnalyzerTransactor()
        mAnalyzer.setTransactor(mFaceAnalyzerTransactor)
        prepareViews()
    }

    private fun prepareViews() {
        surfaceHolderCamera = surfaceViewCamera.holder
        surfaceHolderOverlay = surfaceViewOverlay.holder

        surfaceHolderOverlay?.setFormat(PixelFormat.TRANSPARENT)
        surfaceHolderCamera?.addCallback(surfaceHolderCallback)
    }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            mLensEngine = createLensEngine(width, height)
            surfaceHolderOverlay?.let { mFaceAnalyzerTransactor.setOverlay(it) }
            mLensEngine.run(holder)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mLensEngine.release()
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {

        }

    }

    private fun createLensEngine(width: Int, height: Int): LensEngine {
        val lensEngineCreator = LensEngine.Creator(this, mAnalyzer)
                .applyFps(20F)
                .setLensType(LensEngine.FRONT_LENS)
                .enableAutomaticFocus(true)

        return if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lensEngineCreator.let {
                it.applyDisplayDimension(height, width)
                it.create()
            }
        } else {
            lensEngineCreator.let {
                it.applyDisplayDimension(width, height)
                it.create()
            }
        }
    }

    private fun createAnalyzer(): MLFaceAnalyzer {
        val settings = MLFaceAnalyzerSetting.Factory()
                .allowTracing()
                .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                .setShapeType(MLFaceAnalyzerSetting.TYPE_SHAPES)
                .setMinFaceProportion(.5F)
                .setKeyPointType(MLFaceAnalyzerSetting.TYPE_KEYPOINTS)
                .create()

        return MLAnalyzerFactory.getInstance().getFaceAnalyzer(settings)
    }
}