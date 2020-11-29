package br.com.esdras.e024zodiacapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import br.com.esdras.e024zodiacapp.model.Previsao
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.threeten.extra.Interval
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    var mesNascimento = 1
    var diaNascimento = 1
    var date  = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val queue = Volley.newRequestQueue(this)

        // Zona de horario para definir periodo inicial
        val zoneId = ZoneId.of("America/Sao_Paulo")

        //<editor-fold desc="Intervalo de tempo com base nas datas do Zodiaco" defaultstate="collapsed">
        val intervalos = mapOf<String, Interval>(
            "aries".to(
                Interval.of(
                    LocalDate.of(2020, 3, 21).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 4, 20).atStartOfDay(zoneId).toInstant()
                )
            ),

            "touro".to(
                Interval.of(
                    LocalDate.of(2020, 4, 20).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 5, 21).atStartOfDay(zoneId).toInstant()
                )
            ),

            "gemeos".to(
                Interval.of(
                    LocalDate.of(2020, 5, 21).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 6, 22).atStartOfDay(zoneId).toInstant()
                )
            ),

            "cancer".to(
                Interval.of(
                    LocalDate.of(2020, 6, 22).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 7, 23).atStartOfDay(zoneId).toInstant()
                )
            ),

            "leao".to(
                Interval.of(
                    LocalDate.of(2020, 7, 23).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 8, 23).atStartOfDay(zoneId).toInstant()
                )
            ),

            "virgem".to(
                Interval.of(
                    LocalDate.of(2020, 8, 23).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 9, 23).atStartOfDay(zoneId).toInstant()
                )
            ),

            "libra".to(
                Interval.of(
                    LocalDate.of(2020, 9, 23).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 10, 23).atStartOfDay(zoneId).toInstant()
                )
            ),

            "escorpiao".to(
                Interval.of(
                    LocalDate.of(2020, 10, 23).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 11, 22).atStartOfDay(zoneId).toInstant()
                )
            ),

            "sagitario".to(
                Interval.of(
                    LocalDate.of(2020, 11, 22).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 12, 22).atStartOfDay(zoneId).toInstant()
                )
            ),

            "capricornio".to(
                Interval.of(
                    LocalDate.of(2020, 12, 22).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2021, 1, 20).atStartOfDay(zoneId).toInstant()
                )
            ),

            "aquario".to(
                Interval.of(
                    LocalDate.of(2020, 1, 20).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 2, 19).atStartOfDay(zoneId).toInstant()
                )
            ),

            "peixes".to(
                Interval.of(
                    LocalDate.of(2020, 2, 19).atStartOfDay(zoneId).toInstant(),
                    LocalDate.of(2020, 3, 21).atStartOfDay(zoneId).toInstant()
                )
            )
        )
        //</editor-fold>

        buttonPrevisao.setOnClickListener {
            getPrevisao(imageView.tag.toString(), queue)
        }

        /**
         *    Criacao e atribuicao de Listener para selecionar o Signo e
         *    definir sua imagem e descricao conforme a seleção do spinner (lista suspensa)
         */
        spinnerSignos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {
                val signo = (view as TextView).text.toString()
                    .toLowerCase(Locale.ROOT)
                val identifier = resources.getIdentifier(signo,"drawable", packageName)
                imageView.setImageResource(identifier)
                imageView.tag = signo
                textViewSigno.text = signo.toUpperCase()
            }

            /**
             * Selecionar a imagem de Áries ao iniciar
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (parent != null) {
                    parent.setSelection(0)
                }
            }
        }

        /**
         * Criacao de DatePickerDialog para capturar a data e definir o signo do usuario
         */
        val datePickerListener = object : DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                mesNascimento = month.plus(1)
                diaNascimento = dayOfMonth
                definirSigno(zoneId, intervalos)
                Toast.makeText(applicationContext, "Dia: $diaNascimento - Mês: ${mesNascimento}", Toast.LENGTH_LONG).show()
            }
        }

        /**
         * Chamada do DatePickerDialog
         */
        buttonDataNascimento.setOnClickListener {
            DatePickerDialog(this, datePickerListener, date.year, date.month.value - 1, date.dayOfMonth).show()
        }

    }

    /**
     * Funcao que verifica o periodo da data de nascimento do usuario e verifica se o periodo esta contido
     * no intevalo referente aos Signos
     */
    private fun definirSigno(zoneId: ZoneId?, intervalos: Map<String, Interval>) {

        var nascimento = LocalDate.of(date.year, mesNascimento, diaNascimento).atStartOfDay(zoneId).toInstant()

        /**
         * Percorre o intervalo de periodos validando o periodo de nascimento
         */
        intervalos.forEach { k, v ->
            /**
             * Verifica se é o primeiro periodo e altera o ano para para compatibilidade
             */
            if (mesNascimento == 1 && diaNascimento <= 19) {
                nascimento = LocalDate.of(2021, mesNascimento, diaNascimento).atStartOfDay(zoneId).toInstant()
            }

            /**
             * Verifica de o nascimento esta contido no periodo atual da iteração
             */
            if (v.contains(nascimento)) {
                // Obtem nome do recurso
                val resource = resources.getIdentifier(k, "drawable", applicationContext.packageName)
                // Configura a imagem correta do signo
                imageView.setImageResource(resource)
                // Armazena referencia no atributo TAG para ser reutilizado nas demais funções
                imageView.tag = k
                // Seta o signo no TextView
                textViewSigno.text = k.toUpperCase()
                Log.i("SIGNO", k)
            }
        }
    }

    /**
     * Funcao que realiza requisição web à API de previsões de horóscopo
     */
    private fun getPrevisao(signo: String, queue: RequestQueue) {
        // Exibir proressbar
        progressBar.visibility = View.VISIBLE
        val url = "http://babi.hefesto.io/signo/$signo/dia"

        /**
         * Uso da biblioteca voley para tratamento das requisições web
         */
        val requestPrevisao = StringRequest(Request.Method.GET, url, {
            val intent = Intent(applicationContext, PrevisaoActivity::class.java)
            intent.putExtra("previsao", it)

            // Ocultar progressbar
            progressBar.visibility = View.INVISIBLE
            startActivity(intent)
        },
            {
                println("Erro: ${it.localizedMessage}")
            })
        queue.add(requestPrevisao)
    }
}