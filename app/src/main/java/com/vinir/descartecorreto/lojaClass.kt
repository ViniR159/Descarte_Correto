package com.vinir.descartecorreto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class lojaClass(val lista: Array<Inicial.lojaItens>, val inventario: MutableList<String>, val click: (Inicial.lojaItens) -> Unit) : RecyclerView.Adapter<lojaClass.ViewHolder>() {
    class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView){
        val img = itemView.findViewById<ImageView>(R.id.imgItem)
        val nome = itemView.findViewById<TextView>(R.id.txtNome)
        val preco = itemView.findViewById<TextView>(R.id.txtPreco)
        val linha = itemView.findViewById<LinearLayout>(R.id.toqueLoja)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.loja_item,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = lista[position]

        holder.nome.text = item.item

        holder.preco.text =
            if(item.item in inventario){
                ""
            } else {
                "${item.preco} pontos"
            }


        holder.img.setImageResource(
            item.imagem
        )
        holder.linha.setOnClickListener {
            click(item)
        }



    }
}