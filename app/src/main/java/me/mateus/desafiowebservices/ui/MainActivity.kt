package me.mateus.desafiowebservices.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import me.mateus.desafiowebservices.R
import me.mateus.desafiowebservices.models.HQ
import me.mateus.desafiowebservices.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hqAdapter = HQAdapter(viewModel, resources)


        hqAdapter.hqClickListener = object : HQClickListener {
            override fun onHQClick(hq: HQ) {
                startActivity(Intent(this@MainActivity, HQActivity::class.java).apply {
                    putExtra("hq", hq)
                })
            }
        }

        findViewById<RecyclerView>(R.id.activityMain_rvHQs).also { rv ->
            rv.adapter = hqAdapter
            rv.addOnScrollListener(object : OnScrollListener() {

                private val scrollDelay: Long = 50

                private var lastScroll: Long = -1

                private fun criarScrollChecker() = object : Runnable {
                    override fun run() {
                        val now = System.currentTimeMillis()
                        if (now - lastScroll > scrollDelay) {
                            lastScroll = -1
                            updatePage()
                        } else {
                            rv.postDelayed(this, scrollDelay)
                        }
                    }
                }

                override fun onScrolled(rView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rView, dx, dy)
                    if (lastScroll == -1L) {
                        rv.postDelayed(criarScrollChecker(), scrollDelay)
                    }
                    lastScroll = System.currentTimeMillis()
                }

                fun updatePage() {
                    (rv.layoutManager as GridLayoutManager).also { glm ->
                        (((glm.findLastVisibleItemPosition() + glm.findFirstVisibleItemPosition()) / 2) / 20).let { pagina ->
                            viewModel.setCurrentPage(pagina)
                        }
                    }
                }
            })
        }

        viewModel.loadCharacters("spider-man") {
            viewModel.setCurrentPage(0) {
                hqAdapter.notifyDataSetChanged()
                findViewById<ImageView>(R.id.activityMain_imgLoading).visibility = View.GONE
                findViewById<RecyclerView>(R.id.activityMain_rvHQs).visibility = View.VISIBLE
            }
        }
        findViewById<RecyclerView>(R.id.activityMain_rvHQs).visibility = View.GONE
        findViewById<ImageView>(R.id.activityMain_imgLoading).also { loading ->
            loading.visibility = View.VISIBLE
            loading.setImageDrawable(CircularProgressDrawable(this).apply {
                setColorSchemeColors(
                    resources.getColor(R.color.colorPrimary, resources.newTheme()),
                    resources.getColor(R.color.colorPrimary, resources.newTheme())
                )
                strokeCap = Paint.Cap.ROUND
                centerRadius = 80f
                strokeWidth = 45f
                start()
            })
        }
    }
}

class HQAdapter(private val mainViewModel: MainViewModel, private val resources: Resources) :
    RecyclerView.Adapter<HQViewHolder>() {

    var hqClickListener = object : HQClickListener {
        override fun onHQClick(hq: HQ) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HQViewHolder(
        mainViewModel,
        LayoutInflater.from(parent.context).inflate(R.layout.item_hq, parent, false),
        parent.context,
        this,
        resources
    )

    override fun onBindViewHolder(holder: HQViewHolder, position: Int) {
        val hq = mainViewModel.getHQInPosition(position)
        holder.apply {
            itemPos = position
            itemHQ = hq
            updateViewData()
        }
    }

    override fun getItemCount() = mainViewModel.comicQuant

}


class HQViewHolder(
    private val mainViewModel: MainViewModel,
    private val view: View,
    private val context: Context,
    private val hqAdapter: HQAdapter,
    private val resources: Resources
) : RecyclerView.ViewHolder(view) {

    private val spinner = CircularProgressDrawable(context).apply {
        setColorSchemeColors(
            resources.getColor(R.color.colorPrimary, resources.newTheme()),
            resources.getColor(R.color.colorPrimary, resources.newTheme())
        )
        strokeCap = Paint.Cap.ROUND
        centerRadius = 40f
        strokeWidth = 15f
        start()
    }

    var itemPos: Int = 0
    var itemHQ: HQ? = null

    val cpCapa = view.findViewById<ImageView>(R.id.itemHQ_imgCapa)
    val cpID = view.findViewById<TextView>(R.id.itemHQ_tvID)


    init {
        view.setOnClickListener {
            if (itemHQ != null) hqAdapter.hqClickListener.onHQClick(itemHQ!!)
        }
    }


    fun updateViewData(animate: Boolean = true) {
        if (animate) {
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(300)
        }
        if (itemHQ == null) {
            cpCapa.setImageDrawable(spinner)
            cpID.text = "Loading"
            mainViewModel.registerHQListener(itemPos) { pos, hq ->
                if (pos == itemPos) {
                    itemHQ = hq
                    updateViewData(false)
                }
            }
        } else {
            Glide
                .with(context)
                .load(itemHQ?.capaUrl)
                .placeholder(spinner)
                .error(R.drawable.ic_crop)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(cpCapa)

            cpID.text = "#${itemHQ?.id}"
        }
    }
}

interface HQClickListener {
    fun onHQClick(hq: HQ)
}