package me.mateus.desafiowebservices.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.mateus.desafiowebservices.models.HQ
import me.mateus.desafiowebservices.models.Pagina
import me.mateus.desafiowebservices.services.marvelRepository
import retrofit2.http.HTTP
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.math.ceil

class MainViewModel : ViewModel() {


    var dataCharacters = MutableLiveData<ArrayList<Int>>()

    private var dataPaginas = Collections.synchronizedMap(hashMapOf<Int, Pagina?>())

    private val hqLoadListener = Collections.synchronizedMap(hashMapOf<Int, BiConsumer<Int, HQ>>())

    fun registerHQListener(position: Int, listener: BiConsumer<Int, HQ>) {
        synchronized(hqLoadListener) {
            hqLoadListener.put(position, listener)
        }
    }

    private fun callHQLoaded(position: Int, hq: HQ) {
        val listener = synchronized(hqLoadListener) {
            hqLoadListener.remove(position)
        }
        if (listener != null) {
            listener.accept(position, hq)
        }
    }

    fun loadCharacters(startsWith: String, callback: Runnable = Runnable {}) {
        dataCharacters.value = arrayListOf()
        getCharactersID(
            startsWith = startsWith,
            callback = callback
        )
    }

    private fun getCharactersID(
        startsWith: String,
        offset: Int = 0,
        callback: Runnable = Runnable {}
    ) {
        try {
            viewModelScope.launch {
                marvelRepository.getCharactersCollection(
                    startsWith,
                    offset = offset
                ).data.also { charData ->
                    charData.results.forEach { char ->
                        dataCharacters.value?.add(char.id)
                        while (dataCharacters.value?.size!! > 10) {
                            dataCharacters.value?.removeLast()
                        }
                    }
                    if (charData.offset + charData.count < charData.total) {
                        getCharactersID(startsWith, offset + charData.count, callback)
                    } else {
                        callback.run()
                    }

                    val remain = charData.total - charData.count
                    val quant = Math.ceil(remain.toDouble() / charData.limit).toInt()

                    (1..quant).forEach { pag ->
                        getCharactersID(startsWith, offset + (pag * charData.count))
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("MainViewModel", "Erro ao carregar o id dos caracteres!", ex)
        }
    }


    @Volatile
    var paginaQuant = -1

    @Volatile
    var comicQuant = -1

    @Volatile
    var paginaAtual = 0

    private fun loadComicsPage(page: Int, callback: Consumer<Int> = Consumer {}) {
        try {
            synchronized(dataPaginas) {
                dataPaginas[page] = null
            }
            viewModelScope.launch {
                dataCharacters.value?.joinToString(",")?.let { chars ->
                    marvelRepository.getComicsCollection(
                        characters = chars,
                        offset = page * 20
                    ).data.also { comicData ->
                        paginaQuant = ceil(
                            comicData.total.toDouble() / comicData.limit
                        ).toInt()
                        comicQuant = comicData.total


                        val pag = Pagina().apply { pagina = page }
                        comicData.results.forEach { comic ->
                            pag.hqs.add(HQ().apply {
                                id = comic.id
                                capaUrl =
                                    "${comic.thumbnail.path}.${comic.thumbnail.extension}".replace(
                                        "http://",
                                        "https://"
                                    )
                                try {
                                    titulo = comic.title
                                } catch (ex: Exception) {
                                    titulo = ""
                                }
                                try {
                                    descricao = comic.description
                                } catch (ex: Exception) {
                                    descricao = ""
                                }

                                try {
                                    dataPublicacao = comic.dates.find { cd ->
                                        cd.type.equals("onsaledate", ignoreCase = true)
                                    }?.date.toString()


                                } catch (ex: Exception) {
                                    dataPublicacao = ""
                                }
                                try {
                                    preco = comic.prices.first().price
                                } catch (ex: Exception) {
                                    preco = ""
                                }
                                try {
                                    paginas = comic.pageCount
                                } catch (ex: Exception) {
                                    paginas = 0
                                }
                            })
                        }
                        synchronized(dataPaginas) {
                            dataPaginas[page] = pag
                        }
                        callback.accept(page)
                        pag.hqs.forEachIndexed { index, hq ->
                            callHQLoaded(page * 20 + index, hq)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("MainViewModel", "Erro ao carregar o id dos caracteres!", ex)
        }
    }

    fun setCurrentPage(page: Int, callback: Runnable = Runnable {}) {
        val span = 1
        paginaAtual = page
        val min: Int = if (page - span < 0) {
            0
        } else {
            page - span
        }
        val max: Int = if (paginaQuant < 0) {
            page
        } else if (page + span < paginaQuant) {
            page + span
        } else if (page < paginaQuant) {
            page
        } else {
            paginaQuant - 1
        }

        val range = (min..max)

        val loadedPages = Collections.synchronizedList(arrayListOf<Int>())

        synchronized(dataPaginas) {
            dataPaginas.entries.parallelStream().forEach { entry ->
                loadedPages.add(entry.key)
            }
        }

        range.forEach { p ->
            if (!synchronized(dataPaginas) { dataPaginas.containsKey(p) }) {
                loadComicsPage(p) { loadedPage ->
                    if (synchronized(loadedPages) {
                            loadedPages.add(loadedPage)
                            range.forEach { p ->
                                if (!loadedPages.contains(p)) {
                                    return@synchronized false
                                }
                            }
                            return@synchronized true
                        }) {
                        if (comicQuant > 20 && page == 0 && range.count() <= 1) {
                            setCurrentPage(page, callback)
                        } else {
                            callback.run()
                        }
                    }
                }
            }
        }

        synchronized(dataPaginas) {
            if (paginaAtual != page) {
                return@synchronized
            }
            val rem = arrayListOf<Int>()
            dataPaginas.entries.parallelStream().forEach { entry ->
                if (!range.contains(entry.key) && entry.value != null) {
                    rem.add(entry.key)
                }
            }
            rem.forEach { key ->
                dataPaginas.remove(key)
            }
        }

    }

    fun getHQInPosition(position: Int): HQ? {
        synchronized(dataPaginas) {
            if (dataPaginas.get(position / 20) != null) {
                return dataPaginas.get(position / 20)?.hqs?.get(position % 20)
            }
        }
        return null
    }


}