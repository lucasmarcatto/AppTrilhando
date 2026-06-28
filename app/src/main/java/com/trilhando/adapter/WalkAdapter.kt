package com.trilhando.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trilhando.databinding.ItemCaminhadaBinding
import com.trilhando.model.Caminhada
import java.text.SimpleDateFormat
import java.util.Locale

class WalkAdapter(
    private var walks: List<Caminhada>,
    private val onItemClick: (Caminhada) -> Unit
) : RecyclerView.Adapter<WalkAdapter.WalkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkViewHolder {
        val binding = ItemCaminhadaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WalkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalkViewHolder, position: Int) {
        holder.bind(walks[position])
        holder.itemView.setOnClickListener { onItemClick(walks[position]) }
    }

    override fun getItemCount() = walks.size

    fun updateList(newList: List<Caminhada>) {
        walks = newList
        notifyDataSetChanged()
    }

    inner class WalkViewHolder(private val binding: ItemCaminhadaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(walk: Caminhada) {
            binding.tvTitulo.text = walk.titulo
            binding.tvData.text = dateFormat.format(walk.dataCriacao.toDate())
            binding.tvPassos.text = "Passos: ${walk.quantidadePassos}"
            binding.tvLocal.text = "Lat: %.4f, Lon: %.4f".format(walk.latitude, walk.longitude)
        }
    }
}