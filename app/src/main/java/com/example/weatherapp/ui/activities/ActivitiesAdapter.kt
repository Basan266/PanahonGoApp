package com.example.weatherapp.ui.activities

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.google.android.material.card.MaterialCardView

class ActivitiesAdapter(
    private val onActivityClicked:
        (ActivityRecommendation) -> Unit = {}
) : ListAdapter<
        ActivityRecommendation,
        ActivitiesAdapter.ActivityViewHolder
        >(ActivityDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivityViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_activity_recommendation,
                    parent,
                    false
                )

        return ActivityViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ActivityViewHolder,
        position: Int
    ) {
        holder.bind(
            recommendation =
            getItem(position),

            onActivityClicked =
            onActivityClicked
        )
    }

    class ActivityViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(
        itemView
    ) {
        private val cardActivity =
            itemView.findViewById<MaterialCardView>(
                R.id.cardActivity
            )

        private val tvActivityIcon =
            itemView.findViewById<TextView>(
                R.id.tvActivityIcon
            )

        private val tvActivityName =
            itemView.findViewById<TextView>(
                R.id.tvActivityName
            )

        private val tvActivityBestTime =
            itemView.findViewById<TextView>(
                R.id.tvActivityBestTime
            )

        private val tvActivityStatus =
            itemView.findViewById<TextView>(
                R.id.tvActivityStatus
            )

        private val tvActivityRecommendation =
            itemView.findViewById<TextView>(
                R.id.tvActivityRecommendation
            )

        fun bind(
            recommendation:
            ActivityRecommendation,

            onActivityClicked:
                (ActivityRecommendation) -> Unit
        ) {
            tvActivityIcon.text =
                recommendation.icon

            tvActivityName.text =
                recommendation.activityName

            tvActivityBestTime.text =
                "Best time: ${recommendation.bestTime}"

            tvActivityRecommendation.text =
                recommendation.recommendation

            configureStatus(
                recommendation.status
            )

            cardActivity.setOnClickListener {
                onActivityClicked(
                    recommendation
                )
            }
        }

        private fun configureStatus(
            status: ActivityStatus
        ) {
            val presentation =
                when (status) {
                    ActivityStatus.GOOD ->
                        StatusPresentation(
                            text = "Good",
                            textColor = "#73E6A0",
                            backgroundColor = "#263D30",
                            strokeColor = "#386348"
                        )

                    ActivityStatus.CAUTION ->
                        StatusPresentation(
                            text = "Caution",
                            textColor = "#FFD166",
                            backgroundColor = "#403820",
                            strokeColor = "#66592D"
                        )

                    ActivityStatus.AVOID ->
                        StatusPresentation(
                            text = "Avoid",
                            textColor = "#FF7777",
                            backgroundColor = "#412626",
                            strokeColor = "#693A3A"
                        )

                    ActivityStatus.UNKNOWN ->
                        StatusPresentation(
                            text = "Unknown",
                            textColor = "#B5B5B5",
                            backgroundColor = "#333333",
                            strokeColor = "#505050"
                        )
                }

            tvActivityStatus.text =
                presentation.text

            tvActivityStatus.setTextColor(
                Color.parseColor(
                    presentation.textColor
                )
            )

            val density =
                tvActivityStatus.resources
                    .displayMetrics
                    .density

            tvActivityStatus.background =
                GradientDrawable().apply {
                    shape =
                        GradientDrawable.RECTANGLE

                    cornerRadius =
                        16f * density

                    setColor(
                        Color.parseColor(
                            presentation.backgroundColor
                        )
                    )

                    setStroke(
                        (1f * density).toInt(),
                        Color.parseColor(
                            presentation.strokeColor
                        )
                    )
                }
        }
    }

    private data class StatusPresentation(
        val text: String,
        val textColor: String,
        val backgroundColor: String,
        val strokeColor: String
    )

    private class ActivityDiffCallback :
        DiffUtil.ItemCallback<
                ActivityRecommendation>() {

        override fun areItemsTheSame(
            oldItem: ActivityRecommendation,
            newItem: ActivityRecommendation
        ): Boolean {
            return oldItem.activityName ==
                    newItem.activityName
        }

        override fun areContentsTheSame(
            oldItem: ActivityRecommendation,
            newItem: ActivityRecommendation
        ): Boolean {
            return oldItem == newItem
        }
    }
}