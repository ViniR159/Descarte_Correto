package com.vinir.descartecorreto

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var email: EditText
    lateinit var senha: EditText
    lateinit var login: Button
    lateinit var cadastro: Button
    lateinit var gmailBtn: ImageButton


    lateinit var cadastroEmail: EditText
    lateinit var cadastroSenha: EditText
    lateinit var fecharCadastro: Button
    lateinit var cadastrar: Button

    lateinit var btnLogar: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            startActivity(
                Intent(this, Inicial::class.java)
            )
            finish()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val cadastrOpen = findViewById<CardView>(R.id.cardCadastro)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.emailCampo)
        senha = findViewById(R.id.senhaCampo)

        login = findViewById(R.id.btnLogin)
        cadastro = findViewById(R.id.btnCadastro)

        gmailBtn = findViewById(R.id.btnGoogle)

        cadastroEmail = findViewById(R.id.cadastroEmail)
        cadastroSenha = findViewById(R.id.cadastroSenha)
        fecharCadastro = findViewById(R.id.btnFecharCadastro)
        cadastrar = findViewById(R.id.btnCriarConta)

        val cardInfo = findViewById<ScrollView>(R.id.cardInfo)
        btnLogar = findViewById(R.id.confirmarLogin)

        login.setOnClickListener {

            val textoEmail = email.text.toString()
            val textoSenha = senha.text.toString()

            if(textoEmail.isEmpty() || textoSenha.isEmpty()){

                Toast.makeText(
                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()

            }else{

                auth.signInWithEmailAndPassword(
                    textoEmail,
                    textoSenha
                ).addOnCompleteListener {

                    if(it.isSuccessful){

                        Toast.makeText(
                            this,
                            "Login realizado!",
                            Toast.LENGTH_SHORT
                        ).show()

                        cardInfo.visibility = View.VISIBLE

                    }else{

                        Toast.makeText(
                            this,
                            "Email ou senha incorretos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        gmailBtn.setOnClickListener {
            Toast.makeText(
                this,
                "Ainda não disponivel",
                Toast.LENGTH_SHORT
            ).show()
        }
        cadastro.setOnClickListener {
            cadastrOpen.visibility = View.VISIBLE
        }

        fecharCadastro.setOnClickListener {

            cadastrOpen.visibility = View.GONE

        }

        cadastrar.setOnClickListener {
            val emailTexto =
                cadastroEmail.text.toString()

            val senhaTexto =
                cadastroSenha.text.toString()

            auth.createUserWithEmailAndPassword(
                emailTexto,
                senhaTexto
            ).addOnCompleteListener {

                if(it.isSuccessful){

                    Toast.makeText(
                        this,
                        "Conta criada!",
                        Toast.LENGTH_SHORT
                    ).show()

                    cadastrOpen.visibility = View.GONE

                }else{

                    Toast.makeText(
                        this,
                        "Erro ao cadastrar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnLogar.setOnClickListener {
            startActivity(
                Intent(this, Inicial::class.java)
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}