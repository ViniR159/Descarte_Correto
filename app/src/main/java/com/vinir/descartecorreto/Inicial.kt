package com.vinir.descartecorreto

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.Manifest
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.provider.Settings
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Inicial : AppCompatActivity() {

    lateinit var aviso: Toast

    lateinit var txtPnts: TextView
    lateinit var btnAbrirLoja: ImageButton
    lateinit var btnAbrirMap: ImageButton
    lateinit var btnFecharJanela: Button
    lateinit var centralizar: ImageButton
    lateinit var mapa: MapView
    lateinit var adapter: lojaClass

    lateinit var btnLigarGps: Button
    var pontos = 0
    lateinit var btnReciclar: ImageButton

    lateinit var chapeu: ImageView
    lateinit var tie: ImageView


    val db = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()

    val usuario = auth.currentUser

    val uid = usuario?.uid

    lateinit var save: SharedPreferences

    val cooldown = 30 * 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_inicial)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osm", MODE_PRIVATE)
        )

        save = getSharedPreferences(
            "dados",
            MODE_PRIVATE
        )

        Configuration.getInstance().userAgentValue =
            packageName

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )

        aviso = Toast.makeText(
            this,
            "",
            Toast.LENGTH_SHORT
        )

        txtPnts = findViewById(R.id.txtPnts)

        atlInvFirebaase("bscPnts")
        atlInvFirebaase("bscInv")


        btnLigarGps = findViewById(R.id.btnLigarGps)
        btnAbrirLoja = findViewById(R.id.btnLoja)
        btnAbrirMap = findViewById(R.id.btnAbrirMap)

        val mapCard = findViewById<CardView>(R.id.mapCard)
        val lojaCard = findViewById<CardView>(R.id.lojaItem)

        testarGps()

        val chapeuSalvo =
            save.getString("hat", null)

        val gravataSalva =
            save.getString("tie", null)

        if(chapeuSalvo == null){
            trocarChapeu("tirar chapeu")
        }else{
            trocarChapeu(chapeuSalvo)
        }

        if(gravataSalva == null){
            trocarTie("tirar gravata")
        }else{
            trocarTie(gravataSalva)
        }

        val recycler =
            findViewById<RecyclerView>(R.id.lojaItensTela)

        recycler.layoutManager =
            LinearLayoutManager(this)

        recycler.layoutManager =
            LinearLayoutManager(this)

        adapter = lojaClass(itensLoja, inventario) { itemClicado ->

            if(itemClicado.item in inventario){
                if(itemClicado.categoria == "hat"){

                    trocarChapeu(itemClicado.item)
                    lojaCard.visibility = View.GONE
                    btnFecharJanela.visibility = View.GONE
                    save.edit()
                        .putString("hat", itemClicado.item)
                        .apply()

                }else{
                    trocarTie(itemClicado.item)
                    lojaCard.visibility = View.GONE
                    btnFecharJanela.visibility = View.GONE
                    save.edit()
                        .putString("tie", itemClicado.item)
                        .apply()
                }
            }else {
                if(pontos >= itemClicado.preco)
                {
                    aviso.setText("Item adiquirido")
                    aviso.show()
                    inventario.add(itemClicado.item)
                    pontos -= itemClicado.preco
                    txtPnts.text = "$pontos"
                    atlInvFirebaase("atlPnts")
                    atlInvFirebaase("atlInv")

                }else{
                    aviso.setText("Falta pontos")
                    aviso.show()
                }
            }

        }

        recycler.adapter = adapter

        btnFecharJanela = findViewById(R.id.btnFecharJanela)
        centralizar = findViewById(R.id.btnCentralizar)


        mapa = findViewById(R.id.mapa)

        mapa.setTileSource(TileSourceFactory.MAPNIK)

        mapa.setMultiTouchControls(true)

        btnAbrirLoja.setOnClickListener {
            lojaCard.visibility = View.VISIBLE
            btnFecharJanela.visibility = View.VISIBLE
        }
        btnAbrirMap.setOnClickListener {

            btnFecharJanela.visibility = View.VISIBLE
            mapa.onResume()
            mapCard.visibility = View.VISIBLE

        }

        btnFecharJanela.setOnClickListener {
            mapa.onPause()
            mapCard.visibility = View.GONE
            lojaCard.visibility = View.GONE
            btnFecharJanela.visibility = View.GONE

        }

        val localizacaoUsuario = MyLocationNewOverlay(
            GpsMyLocationProvider(this),
            mapa
        )

        localizacaoUsuario.enableMyLocation()
        localizacaoUsuario.enableFollowLocation()
        mapa.overlays.add(localizacaoUsuario)

        localizacaoUsuario.runOnFirstFix {

            runOnUiThread {

                val localizacao = localizacaoUsuario.myLocation

                mapa.controller.setZoom(18.0)

                mapa.controller.animateTo(localizacao)

            }
        }

        centralizar.setOnClickListener {
            val localizacao = localizacaoUsuario.myLocation

            mapa.controller.setZoom(18.0)

            mapa.controller.animateTo(localizacao)
        }


        val listaEcopontos = mutableListOf<GeoPoint>()

        db.collection("Ecopontos")
            .document("Ecopontos")
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val dados = document.data

                    dados?.forEach { (_, valor) ->

                        val ecopontoData = valor as Map<String, String>

                        val nome = ecopontoData["nome"].toString()

                        val lat = ecopontoData["lat"]?.toDouble()

                        val lng = ecopontoData["log"]?.toDouble()

                        if (lat != null && lng != null) {

                            val ecoponto = GeoPoint(lat, lng)

                            listaEcopontos.add(ecoponto)

                            val marcador = Marker(mapa)

                            marcador.position = ecoponto

                            marcador.setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )

                            marcador.title = nome

                            mapa.overlays.add(marcador)

                        }

                    }

                    mapa.invalidate()

                }

            }
            .addOnFailureListener { e ->
                aviso.setText("Erro ao carregar ecopontos\"")
                aviso.show()
            }

        btnReciclar = findViewById(R.id.btnReciclar)

        btnReciclar.setOnClickListener {

            val localUsuario =
                localizacaoUsuario.myLocation

            var pertoDeAlgum = false

            for ((index, ponto) in listaEcopontos.withIndex()) {

                val distancia =
                    localUsuario.distanceToAsDouble(ponto)

                if (distancia <= 200) {

                    pertoDeAlgum = true

                    val nomeEcoponto =
                        "ecoponto_$index"

                    val tempoAtual =
                        System.currentTimeMillis()

                    val ultimoUso =
                        save.getLong(nomeEcoponto, 0)

                    val cooldown =
                        30 * 60 * 1000L

                    if (tempoAtual - ultimoUso >= cooldown) {

                        pontos += 10

                        txtPnts.text =
                            "$pontos"

                        save.edit()
                            .putLong(
                                nomeEcoponto,
                                tempoAtual
                            )
                            .apply()

                        aviso.setText(
                            "♻ +10 pontos"
                        )

                        aviso.show()

                        atlInvFirebaase("atlPnts")

                    } else {

                        val restante =
                            (cooldown - (tempoAtual - ultimoUso))/1000 / 60

                        aviso.setText(
                            "Você reciclou recentemente, aguarde $restante minutos"
                        )

                        aviso.show()

                    }

                    break
                }

            }

            if (!pertoDeAlgum) {

                aviso.setText(
                    "Você está longe do ecoponto"
                )

                aviso.show()

            }

        }
    }

    fun testarGps(){
        val ligarGpsCard = findViewById<CardView>(R.id.ligarGpsCard)

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val gpsAtivado = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)

        if(!gpsAtivado){

            ligarGpsCard.visibility = View.VISIBLE

            btnAbrirLoja.isEnabled = false
            btnAbrirMap.isEnabled = false

            btnLigarGps.setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                )
            }
        }
        else{
            ligarGpsCard.visibility = View.GONE

            btnAbrirLoja.isEnabled = true
            btnAbrirMap.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()

        testarGps()
    }

    data class lojaItens(
        val item: String,
        val preco: Int,
        val imagem: Int,
        val categoria: String
    )

    val itensLoja = arrayOf(
        lojaItens(
            "tirar chapeu",
            0,
            R.drawable.nd,
            "hat"
        ),
        lojaItens(
            "tirar gravata",
            0,
            R.drawable.nd,
            "tie"
        ),
        lojaItens(
            "cartola",
            10,
            R.drawable.cartola,
            "hat"
        ),
        lojaItens(
            "coroa",
            20,
            R.drawable.coroa,
            "hat"
        ),
        lojaItens(
            "cowboy",
            20,
            R.drawable.cowboy,
            "hat"
        ),
        lojaItens(
            "garavata amarela",
            20,
            R.drawable.gravata_amarela,
            "tie"
        ),
        lojaItens(
            "garavata preta",
            20,
            R.drawable.gravata_preta,
            "tie"
        ),
        lojaItens(
            "garavata rosa",
            20,
            R.drawable.gravata_rosa,
            "tie"
        ),
        lojaItens(
            "leon",
            20,
            R.drawable.leon,
            "hat"
        ),
        lojaItens(
            "mago",
            20,
            R.drawable.mago,
            "hat"
        ),
        lojaItens(
            "palha",
            15,
            R.drawable.palha,
            "hat"
        ),
        lojaItens(
            "pirata",
            20,
            R.drawable.pirata,
            "hat"
        ),
        lojaItens(
            "policial",
            20,
            R.drawable.policial,
            "hat"
        ),
        lojaItens(
            "viking",
            20,
            R.drawable.viking,
            "hat"
        )


    )
    var inventario = mutableListOf(
        "tirar chapeu",
        "tirar gravata"
    )

    fun trocarChapeu(nome: String){
        chapeu = findViewById(R.id.imgHat)

        val chapeuEncontrado =
            itensLoja.find {
                it.item == nome
            }

        if (chapeuEncontrado != null){
            chapeu.setImageResource(chapeuEncontrado.imagem)
            aviso.setText("Equipado")
            aviso.show()
        }
        else{
            aviso.setText("Item não disponivel")
            aviso.show()
        }
    }

    fun trocarTie(nome: String){

        tie = findViewById(R.id.imgTie)

        val tieEncontrado =
            itensLoja.find {
                it.item == nome
            }

        if(tieEncontrado != null){
            tie.setImageResource(tieEncontrado.imagem)
            aviso.setText("Equipado")
            aviso.show()
        }
        else{
            aviso.setText("Item não disponivel")
            aviso.show()
        }
    }

    fun atlInvFirebaase(motivo: String){

        if (uid == null){
            aviso.setText("Usuário não autenticado")
            aviso.show()
            return
        }

        when (motivo) {
            "bscPnts" -> {

                db.collection("Usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->

                        if (document.exists()) {

                            pontos =
                                document.getLong("pontos")
                                    ?.toInt() ?: 0

                            txtPnts.text = "$pontos"

                        } else {

                            // cria documento caso não exista
                            db.collection("Usuarios")
                                .document(uid)
                                .set(
                                    mapOf(
                                        "pontos" to 0,
                                        "inventario" to inventario
                                    )
                                )

                        }

                    }
                    .addOnFailureListener { e ->

                        aviso.setText(
                            "Erro ao buscar pontos: ${e.message}"
                        )
                        aviso.show()

                    }
            }

            "atlPnts" -> {

                db.collection("Usuarios")
                    .document(uid)
                    .set(
                        mapOf(
                            "pontos" to pontos
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .addOnSuccessListener {

                        println("Pontos atualizados")

                    }
                    .addOnFailureListener { e ->

                        aviso.setText(
                            "Erro ao salvar pontos: ${e.message}"
                        )
                        aviso.show()

                    }
            }
            "bscInv" -> {

                db.collection("Usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->

                        if (document.exists()) {

                            inventario.clear()

                            inventario.addAll(
                                document.get("inventario")
                                        as? MutableList<String>
                                    ?: mutableListOf()
                            )

                            adapter.notifyDataSetChanged()

                        }

                    }
                    .addOnFailureListener { e ->

                        aviso.setText(
                            "Erro ao buscar inventário: ${e.message}"
                        )
                        aviso.show()

                    }
            }
            "atlInv" -> {

                db.collection("Usuarios")
                    .document(uid)
                    .set(
                        mapOf(
                            "inventario" to inventario
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .addOnSuccessListener {

                        adapter.notifyDataSetChanged()

                    }
                    .addOnFailureListener { e ->

                        aviso.setText(
                            "Erro ao salvar inventário: ${e.message}"
                        )
                        aviso.show()

                    }
            }
        }
    }
}