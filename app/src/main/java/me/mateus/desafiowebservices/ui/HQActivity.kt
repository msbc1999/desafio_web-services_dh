package me.mateus.desafiowebservices.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import me.mateus.desafiowebservices.R
import me.mateus.desafiowebservices.models.HQ
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HQActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hq)

        val spinner = CircularProgressDrawable(this).apply {
            setColorSchemeColors(
                resources.getColor(R.color.colorPrimary, resources.newTheme()),
                resources.getColor(R.color.colorPrimary, resources.newTheme())
            )
            strokeCap = Paint.Cap.ROUND
            centerRadius = 40f
            strokeWidth = 15f
            start()
        }

        val hq = intent.extras?.getSerializable("hq") as HQ

        findViewById<ImageView>(R.id.activityHQ_imgBack).setOnClickListener {
            finish()
        }


        findViewById<ImageView>(R.id.activityHQ_imgLarge).also { imgView ->
            Glide
                .with(this)
                .load(hq.capaUrl)
                .placeholder(spinner)
                .error(R.drawable.ic_crop)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgView)
        }

        findViewById<ImageView>(R.id.activityHQ_imgSmall).also { imgView ->
            Glide
                .with(this)
                .load(hq.capaUrl)
                .placeholder(spinner)
                .error(R.drawable.ic_crop)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgView)
        }

        findViewById<ImageView>(R.id.activityHQ_imgZoom).also { imgView ->
            Glide
                .with(this)
                .load(hq.capaUrl)
                .placeholder(spinner)
                .error(R.drawable.ic_crop)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgView)
        }

        findViewById<TextView>(R.id.activityHQ_tvTitle).text = hq.titulo.let { txt ->
            if (txt.isEmpty()) {
                "No title avaliable"
            } else {
                txt
            }
        }
        findViewById<TextView>(R.id.activityHQ_tvDescription).text = hq.descricao.let { txt ->
            if (txt.isEmpty()) {
                "No description avaliable"
            } else {
                txt
            }
        }
        findViewById<TextView>(R.id.activityHQ_tvPublished).text = hq.dataPublicacao.let { txt ->
            if (txt.isEmpty()) {
                "No publish date avaliable"
            } else {
                val ldt = LocalDateTime.parse(
                    txt.subSequence(0, txt.lastIndexOf('-')),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                if (ldt.year > LocalDate.now().year || ldt.year < 1800) {
                    "No publish date avaliable"
                } else {
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US).format(ldt)
                }
            }
        }
        findViewById<TextView>(R.id.activityHQ_tvPrice).text = hq.preco.let { txt ->
            Log.i("HQ", txt)
            if (txt.isEmpty()) {
                "No price avaliable"
            } else if (txt.toDouble() <= 0.0) {
                "No price avaliable"
            } else {
                NumberFormat.getCurrencyInstance(Locale.US).also { nf ->
                    nf.maximumFractionDigits = 2
                    nf.currency = Currency.getInstance(Locale.US)
                }.format(txt.toDouble())
            }
        }
        findViewById<TextView>(R.id.activityHQ_tvPages).text = hq.paginas.let { qt ->
            if (qt <= 0) {
                "No page count avaliable"
            } else {
                "$qt"
            }
        }


        val animShort = resources.getInteger(android.R.integer.config_shortAnimTime)
        imgExpandido = findViewById(R.id.activityHQ_imgZoom)
        findViewById<ImageView>(R.id.activityHQ_imgSmall).also { miniImg ->
            miniImg.setOnClickListener {
                zoomImage(
                    findViewById(R.id.activityHQ_layoutRoot),
                    miniImg,
                    imgExpandido!!,
                    animShort
                )
            }
        }

    }


    override fun onBackPressed() {
        findViewById<ImageView>(R.id.activityHQ_imgZoom).also { view ->
            if (view.visibility == View.GONE) {
                super.onBackPressed()
            } else {
                view.callOnClick()
            }
        }
    }

    private var imgExpandido: ImageView? = null


    // REFERENCIA https://developer.android.com/training/animation/zoom.html
    private var animator: Animator? = null
    fun zoomImage(view: View, miniImg: View, zoomImg: ImageView, animDuration: Int) {
        animator?.cancel() // CANCELA A ANIMAÇÃO ATUAL

        findViewById<View>(R.id.activityHQ_vBackground).also { v ->
            v.visibility = View.VISIBLE
            v.alpha = 0f
            v.animate().alpha(1f)
        }

        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        miniImg.getGlobalVisibleRect(startBoundsInt)
        view.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)


        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        zoomImg.visibility = View.VISIBLE

        zoomImg.pivotX = 0f
        zoomImg.pivotY = 0f

        animator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    zoomImg,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(ObjectAnimator.ofFloat(zoomImg, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(zoomImg, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(zoomImg, View.SCALE_Y, startScale, 1f))
            }
            duration = animDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    animator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    animator = null
                }
            })
            start()
        }

        zoomImg.setOnClickListener {
            animator?.cancel()

            animator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(zoomImg, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(zoomImg, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(zoomImg, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(zoomImg, View.SCALE_Y, startScale))
                }
                duration = animDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        miniImg.alpha = 1f
                        zoomImg.visibility = View.GONE
                        animator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        miniImg.alpha = 1f
                        zoomImg.visibility = View.GONE
                        animator = null
                    }
                })
                start()
            }
            findViewById<View>(R.id.activityHQ_vBackground).also { v ->
                v.alpha = 1f
                v.animate().alpha(0f).withEndAction {
                    v.visibility = View.GONE
                }
            }
        }

    }

}