package com.example.weatherapp.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.weatherapp.R
import com.google.android.material.card.MaterialCardView
import java.net.URLEncoder

class AvatarAdapter(
    private val onAvatarSelected:
        (String) -> Unit
) : RecyclerView.Adapter<
        AvatarAdapter.AvatarViewHolder>() {

    private val avatarSeeds =
        mutableListOf<String>()

    private var selectedSeed:
            String? = null

    private var imageLoader:
            ImageLoader? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AvatarViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_avatar,
                    parent,
                    false
                )

        if (imageLoader == null) {
            imageLoader =
                ImageLoader.Builder(
                    parent.context
                )
                    .components {
                        add(
                            SvgDecoder.Factory()
                        )
                    }
                    .build()
        }

        return AvatarViewHolder(
            cardAvatar =
            view.findViewById(
                R.id.cardAvatar
            ),
            ivAvatar =
            view.findViewById(
                R.id.ivAvatarItem
            )
        )
    }

    override fun onBindViewHolder(
        holder: AvatarViewHolder,
        position: Int
    ) {
        val seed =
            avatarSeeds[position]

        val isSelected =
            seed == selectedSeed


        val density =
            holder.cardAvatar.resources
                .displayMetrics
                .density

        holder.cardAvatar.strokeColor =
            Color.parseColor(
                if (isSelected) {
                    "#C65CF4"
                } else {
                    "#F1F1F1"
                }
            )

        holder.cardAvatar.strokeWidth =
            (
                    if (isSelected) {
                        1.5f * density
                    } else {
                        1.5f * density
                    }
                    ).toInt()



        val encodedSeed =
            URLEncoder.encode(
                seed,
                "UTF-8"
            )

        val avatarUrl =
            "https://api.dicebear.com/10.x/" +
                    "toon-head/svg" +
                    "?seed=$encodedSeed"

        val imageRequest =
            ImageRequest.Builder(
                holder.ivAvatar.context
            )
                .data(avatarUrl)
                .crossfade(true)
                .target(
                    holder.ivAvatar
                )
                .build()

        imageLoader?.enqueue(
            imageRequest
        )

        holder.cardAvatar.setOnClickListener {
            val previousSeed =
                selectedSeed

            selectedSeed =
                seed

            previousSeed?.let {
                val previousPosition =
                    avatarSeeds.indexOf(it)

                if (previousPosition >= 0) {
                    notifyItemChanged(
                        previousPosition
                    )
                }
            }

            notifyItemChanged(
                holder.bindingAdapterPosition
            )

            onAvatarSelected(
                seed
            )
        }
    }

    override fun getItemCount(): Int {
        return avatarSeeds.size
    }

    fun submitAvatars(
        seeds: List<String>,
        currentSeed: String?
    ) {
        avatarSeeds.clear()
        avatarSeeds.addAll(seeds)

        selectedSeed =
            currentSeed

        notifyDataSetChanged()
    }

    class AvatarViewHolder(
        val cardAvatar:
        MaterialCardView,

        val ivAvatar:
        ImageView
    ) : RecyclerView.ViewHolder(
        cardAvatar
    )
}