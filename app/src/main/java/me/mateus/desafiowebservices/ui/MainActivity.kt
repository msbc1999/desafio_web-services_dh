package me.mateus.desafiowebservices.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import me.mateus.desafiowebservices.R
import me.mateus.desafiowebservices.models.HQ
import me.mateus.desafiowebservices.services.marvelRepository
import me.mateus.desafiowebservices.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hqAdapter = HQAdapter(viewModel)

        findViewById<RecyclerView>(R.id.activityMain_rvHQs).also { rv ->
            rv.adapter = hqAdapter
        }

        Log.i("MainActivity", "Oficial Loading Characters...")
        viewModel.loadCharacters("spider-man") {
            Log.i("MainActivity", "Caracteres carregados!")
            Log.i("MainActivity", viewModel.dataCharacters.value.toString())

            Log.i("MainActivity", "Carregando paginas...")
            viewModel.setCurrentPage(0) {
                Log.i("MainActivity", "Paginas carregadas!")
                hqAdapter.notifyDataSetChanged()
                viewModel.getPaginas { entries ->
                    entries.forEach { e ->
                        Log.i("MainActivity", "${e.key} > ${e.value?.hqs.toString()}")
                    }
                }
            }
        }

//        viewModel.viewModelScope.launch {
//            Log.i("MainActivity", "Loading Comics...")
//            val obj =
//                marvelRepository.getComicsCollectionDEBUG("1016452,1014858,1012200,1017332,1011114,1017305,1016181,1012295,1011377,1011010")
//            Log.i(
//                "MainActivity", obj
//                    .toString()
//            )
//        }
    }
}

class HQAdapter(private val mainViewModel: MainViewModel) : RecyclerView.Adapter<HQViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HQViewHolder(
        mainViewModel,
        LayoutInflater.from(parent.context).inflate(R.layout.item_hq, parent, false),
        parent.context
    )

    override fun onBindViewHolder(holder: HQViewHolder, position: Int) {
        val pagina = position / 20
        mainViewModel.setCurrentPage(pagina)
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
    private val context: Context
) : RecyclerView.ViewHolder(view) {

    private val spinner = CircularProgressDrawable(context).apply {
        setColorSchemeColors(
            R.color.colorPrimary,
            R.color.colorPrimaryVariant,
            R.color.colorSecondary
        )
        centerRadius = 30f
        strokeWidth = 5f
        start()
    }

    var itemPos: Int = 0
    var itemHQ: HQ? = null

    val cpCapa = view.findViewById<ImageView>(R.id.itemHQ_imgCapa)
    val cpID = view.findViewById<TextView>(R.id.itemHQ_tvID)


    fun updateViewData() {
        view.alpha = 0f
        if (itemHQ == null) {
            cpID.text = "Carregando"
            mainViewModel.registerHQListener(itemPos) { pos, hq ->
                if (pos == itemPos) {
                    itemHQ = hq
                    updateViewData()
                }
            }
        } else {
            view.animate().alpha(1f).setDuration(200)
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