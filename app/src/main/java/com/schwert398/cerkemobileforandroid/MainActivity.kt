package com.schwert398.cerkemobileforandroid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.piece_view.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.grid_view.*
import kotlin.math.min

private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

class MainActivity : AppCompatActivity() {
    private lateinit var pieceList: Array<ImageView>
    private lateinit var gridList: Array<Button>
    private var resizedImageList = mutableMapOf<String, Bitmap>()

    private val matrix180 = Matrix().apply {postRotate(180f)}
    private val matrix90 = Matrix().apply {postRotate(90f)}
    // parameter
    private var chosenPiece: ImageView? = null
    // private var origin: ImageView? = null
    // private var viaPoint: ImageView? = null
    // private var dest: ImageView? = null
    // private var judgeMove: Int? = null
    // private var judgeWater: Int? = null

    // function for debugging.
    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun toast(text: ArrayList<String>) {
        var str = ""
        for (elm in text) str += "$elm "
        toast(str)
    }

    private fun toasty(array: ArrayList<IntArray>){
        var str = ""
        for (elm in array){
            str += "${elm[0]} ${elm[1]}, "
        }
        toast(str)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        field.afterMeasured {
            min(field.width, field.height).also {
                gridResize(it)
                imageResize(it)
            }
            pieceImageAssigner(resizedImageList)
        }

        // odd is black's, even is red's.
        pieceList = arrayOf(
            btam, bio, rio, bnuak, rnuak, buai1, buai2, ruai1, ruai2,
            btuk1, btuk2, rtuk1, rtuk2, bkua1, bkua2, rkua1, rkua2,
            bmaun1, bmaun2, rmaun1, rmaun2, bdau1, bdau2, rdau1, rdau2,
            bkaun1, bkaun2, rkaun1, rkaun2, bgua1, bgua2, rgua1, rgua2,
            bkauk1, bkauk2, bkauk3, bkauk4, bkauk5, bkauk6, bkauk7, bkauk8,
            rkauk1, rkauk2, rkauk3, rkauk4, rkauk5, rkauk6, rkauk7, rkauk8
        )
        gridList = arrayOf(
            ka, la, na, ta, za, xa, ca, ma, pa,
            ke, le, ne, te, ze, xe, ce, me, pe,
            ki, li, ni, ti, zi, xi, ci, mi, pi,
            ku, lu, nu, tu, zu, xu, cu, mu, pu,
            ko, lo, no, to, zo, xo, co, mo, po,
            ky, ly, ny, ty, zy, xy, cy, my, py,
            kai, lai, nai, tai, zai, xai, cai, mai, pai,
            kau, lau, nau, tau, zau, xau, cau, mau, pau,
            kia, lia, nia, tia, zia, xia, cia, mia, pia
        )
        pieceLocator()
    }

    private fun resize(view: View, size: Int){
        val lp: ViewGroup.LayoutParams = view.layoutParams
        lp.width = size
        lp.height = size
        view.layoutParams = lp
    }

    private fun gridResize(fieldSize: Int) {
        for (grid in gridList) {
            resize(grid, fieldSize/9)
        }
        for (piece in pieceList) {
            resize(piece, fieldSize/9)
        }
        var lp = blackHandQueue.layoutParams
        lp.width = fieldSize /9
        blackHandQueue.layoutParams = lp

        lp = redHandQueue.layoutParams
        lp.width = fieldSize /9
        redHandQueue.layoutParams = lp
    }

    private fun imageResize(fieldSize: Int) {
        val imageResourceId = mapOf(
            "btam" to R.drawable.btam, "rtam" to R.drawable.rtam,
            "bio" to R.drawable.bio, "rio" to R.drawable.rio,
            "bnuak" to R.drawable.bnuak, "rnuak" to R.drawable.rnuak,
            "buai" to R.drawable.buai, "ruai" to R.drawable.ruai,
            "btuk" to R.drawable.btuk, "rtuk" to R.drawable.rtuk,
            "bkua" to R.drawable.bkua, "rkua" to R.drawable.rkua,
            "bmaun" to R.drawable.bmaun, "rmaun" to R.drawable.rmaun,
            "bdau" to R.drawable.bdau, "rdau" to R.drawable.rdau,
            "bkaun" to R.drawable.bkaun, "rkaun" to R.drawable.rkaun,
            "bgua" to R.drawable.bgua, "rgua" to R.drawable.rgua,
            "bkauk" to R.drawable.bkauk, "rkauk" to R.drawable.rkauk
        )
        for ((key, imageId) in imageResourceId) {
            val bitmap = BitmapFactory.decodeResource(resources, imageId)
            resizedImageList[key] = Bitmap.createScaledBitmap(
                    bitmap,
                    fieldSize / 9 - 8, fieldSize / 9 - 8,
                    false
                )
        }
    }

    private fun pieceLocator() {
        val initPieceLocation = arrayOf(
            zo, zia, za, zai, zi, xia, ta, tia, xa,
            kau, pe, pau, ke, pia, ka, kia, pa,
            mia, la, lia, ma, tau, xe, xau, te,
            cia, na, nia, ca, lau, me, mau, le,
            kai, pi, nai, ci, cai, ni, pai, ki,
            lai, mi, tai, xi, xai, ti, mai, li
        )
        for ((piece, location) in pieceList.zip(initPieceLocation)) {
            move(piece, location)
        }
    }

    private fun pieceImageAssigner(resizedImageList: MutableMap<String, Bitmap>) {
        for (piece in pieceList) {
            val image = resizedImageList[piece.contentDescription]
            if (null != image){
                when (piece.tag) {
                    "1" -> {
                        piece.setImageBitmap(Bitmap.createBitmap(
                            image, 0, 0, image.width, image.height, matrix180, false
                        ))
                    }
                    "2" -> {
                        piece.setImageBitmap(Bitmap.createBitmap(
                            image, 0, 0, image.width, image.height, matrix90, false
                        ))
                    }
                    else -> {
                        piece.setImageBitmap(image)
                    }
                }
            }
        }
    }

    fun onClickGrid(view: View) {
        if (null != chosenPiece) {
            if (chosenPiece!!.parent != piece_frame) {
                parachute(chosenPiece, view)
            }else{
                move(chosenPiece, view)
            }
            chosenPiece = null
        } else {
            toast(view.contentDescription.toString())
        }
    }

    fun onClickPiece(view: View) {
        if (view is ImageView) {
            if (null != chosenPiece) {
                if (chosenPiece!!.parent != piece_frame) {
                    toast("Can't parachute hand where other piece is!")
                }else{
                    gain(chosenPiece, view)
                }
                chosenPiece = null
            } else {
                chosenPiece = view
                // toast(view.contentDescription.toString())
            }
        }
    }

    fun onClickButton(view: View){
        when(view.id) {
            R.id.initButton -> pieceLocator()
            R.id.checkButton -> toast(chosenPiece?.contentDescription.toString())
            R.id.cancelButton -> chosenPiece = null
            R.id.rotateButton -> {
                if (chosenPiece != null) {
                    rotate(chosenPiece)
                    chosenPiece = null
                }
            }
        }
    }

    // Actually piece do not be null
    private fun move(piece: ImageView?, dest: View) {
        val destLocation = IntArray(2)
        val rect = Rect().also{
            window.decorView.getWindowVisibleDisplayFrame(it)
        }
        dest.getLocationOnScreen(destLocation)
        piece!!.x = destLocation[0].toFloat()
        piece.y = (destLocation[1]-rect.top).toFloat()
    }

    // Actually getter do not be null
    private fun gain(getter: ImageView?, target: ImageView) {
        move(getter, target)
        val targetView = copyImageView(target)
        piece_frame.removeView(target)
        when (getter!!.tag) {
            "0" -> blackHandQueue.addView(targetView)
            "1" -> redHandQueue.addView(targetView)
        }
        toast("Took ${target.contentDescription} by ${getter.contentDescription}")
    }

    // Actually view do not be null
    private fun parachute(view: ImageView?, dest: View){
        if (null != view) {
            val parachutedView = copyImageView(view)
            piece_frame.addView(parachutedView)
            move(parachutedView, dest)
            if (view.parent == blackHandQueue){
                blackHandQueue.removeView(view)
            }else{
                redHandQueue.removeView(view)
            }
        }
    }

    private fun rotate(view: ImageView?){
        if (null != view) {
            val image = resizedImageList[view.contentDescription]
            when (view.tag) {
                "0" -> {view.setImageBitmap(Bitmap.createBitmap(
                            image!!, 0, 0, image.width, image.height, matrix180, false
                        ))
                    view.tag = "1"
                }
                "1" -> {
                    view.setImageBitmap(image)
                    view.tag = "0"
                }
            }
        }
    }

    private fun copyImageView(view: ImageView): ImageView{
        // actually image do not be null (unless contentDescription is invalid)
        val image = resizedImageList[view.contentDescription]
        return ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(view.width, view.height).apply {
                setPadding(3)
            }
            setOnClickListener{
                onClickPiece(it)
            }
            tag = when {
                view.parent != piece_frame -> view.tag
                view.tag == "0" -> "1"
                view.tag == "1" -> "0"
                else -> "2"
            }
            if (tag == "1"){
                setImageBitmap(Bitmap.createBitmap(
                    image!!, 0, 0, image.width, image.height, matrix180, false
                ))
            }else {
                setImageBitmap(image)
            }
            contentDescription = view.contentDescription
        }
    }
}
