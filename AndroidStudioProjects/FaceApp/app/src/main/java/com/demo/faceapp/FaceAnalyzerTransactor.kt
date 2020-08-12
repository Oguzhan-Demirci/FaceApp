package com.demo.faceapp

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.SparseArray
import android.view.SurfaceHolder
import androidx.core.util.valueIterator
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.face.MLFace

class FaceAnalyzerTransactor : MLAnalyzer.MLTransactor<MLFace> {

    private var mOverlay: SurfaceHolder? = null

    fun setOverlay(surfaceHolder: SurfaceHolder) {
        mOverlay = surfaceHolder
    }

    override fun transactResult(result: MLAnalyzer.Result<MLFace>?) {
        draw(result?.analyseList)
    }

    private fun draw(faces: SparseArray<MLFace>?) {
        val canvas = mOverlay?.lockCanvas()

        if (canvas != null && faces != null) {

            //Clear the canvas
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)

            for (face in faces.valueIterator()) {

                //Draw all 855 points of the face. If Front Lens is selected, change x points side.
                for (point in face.allPoints) {
                    val x = mOverlay?.surfaceFrame?.right?.minus(point.x)
                    if (x != null) {
                        Paint().also {
                            it.color = Color.YELLOW
                            it.style = Paint.Style.FILL
                            it.strokeWidth = 16F
                            canvas.drawPoint(x, point.y, it)
                        }
                    }
                }

                //Prepare a string to show if the user smiles or not and draw a text on the canvas.
                val smilingString = if (face.emotions.smilingProbability > 0.5) "SMILING" else "NOT SMILING"

                Paint().also {
                    it.color = Color.RED
                    it.textSize = 60F
                    it.textAlign = Paint.Align.CENTER
                    canvas.drawText(smilingString, face.border.exactCenterX(), face.border.exactCenterY(), it)
                }

            }

            mOverlay?.unlockCanvasAndPost(canvas)
        }
    }

    override fun destroy() {

    }
}