package br.com.esdras.e024zodiacapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.esdras.e024zodiacapp.model.Previsao
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.android.synthetic.main.activity_previsao.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrevisaoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previsao)

        // Receber o JSON da requisição obtida pela biblioteca Volley
        val previsao = intent.getStringExtra("previsao")

        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        // Converter o JSON no modelo de model class necessário
        val previsaoObjectMapper = mapper.readValue(previsao, Previsao::class.java) as Previsao

        /**
         * Preencher os Widgets de UI
         */
        imageViewSigno.setImageResource(resources.getIdentifier(previsaoObjectMapper.signo, "drawable", packageName))
        textViewSigno.text = previsaoObjectMapper.signo.capitalize()
        // Substituir pontos finais por quebra de linha para melhorar a legibilidade
        textViewPrevisao.text = previsaoObjectMapper.texto.replace(". ", ".\n")
        textViewAutor.text = "Autor: ${previsaoObjectMapper.autor}"
        textViewFonte.text = "Fonte: ${previsaoObjectMapper.urlOrigem}"
        textViewData.text = "Data: ${previsaoObjectMapper.dataAcesso.date}"
        println(previsao)
    }
}