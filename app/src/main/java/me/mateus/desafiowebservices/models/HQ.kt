package me.mateus.desafiowebservices.models

class HQ {
    var id: Int = 0
    var capaUrl: String = ""
    var titulo: String = ""
    var descricao: String = ""
    var dataPublicacao: String = ""
    var preco: Double = 0.0
    var paginas: Int = 0

    override fun toString(): String {
        return "HQ($titulo)"
    }
}