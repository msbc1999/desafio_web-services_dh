package me.mateus.desafiowebservices.models

import java.io.Serializable

class HQ : Serializable {
    var id: Int = 0
    var capaUrl: String = ""
    var titulo: String = ""
    var descricao: String = ""
    var dataPublicacao: String = ""
    var preco: String = ""
    var paginas: Int = 0

    override fun toString(): String {
        return "HQ($titulo)"
    }
}