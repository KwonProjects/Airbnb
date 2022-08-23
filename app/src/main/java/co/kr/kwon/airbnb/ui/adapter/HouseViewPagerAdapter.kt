package co.kr.kwon.airbnb.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import co.kr.kwon.airbnb.R
import co.kr.kwon.airbnb.data.model.HouseModel
import com.bumptech.glide.Glide

class HouseViewPagerAdapter(val itemClickListener: (HouseModel) -> Unit) : ListAdapter<HouseModel, HouseViewPagerAdapter.ItemViewHolder>(diffUtil) {


    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(houseModel: HouseModel) {
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)
            val titleTxt = view.findViewById<TextView>(R.id.titleTxt)
            val priceTxt = view.findViewById<TextView>(R.id.priceTxt)


            Glide
                .with(thumbnailImageView.context)
                .load(houseModel.imgUrl)
                .into(thumbnailImageView)
            titleTxt.text = houseModel.title
            priceTxt.text = houseModel.price

            view.setOnClickListener {
                itemClickListener(houseModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(
            inflater.inflate(R.layout.item_house_detail_for_viewpager, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<HouseModel>() {
            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem == newItem
            }


        }
    }
}