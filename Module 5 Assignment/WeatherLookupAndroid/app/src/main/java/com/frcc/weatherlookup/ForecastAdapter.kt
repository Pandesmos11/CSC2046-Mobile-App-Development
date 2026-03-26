// ============================================================
// Name:        Shane Potts
// Date:        03/25/2026
// Course:      CSC2046 - Mobile App Development
// Module:      5 - Weather Lookup
// File:        ForecastAdapter.kt
// Description: RecyclerView adapter for the 5-day forecast list.
//              Each row shows: day name, date, weather emoji,
//              condition description, and high/low temperatures.
// ============================================================

package com.frcc.weatherlookup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ForecastAdapter(
    private var days: List<ForecastDay>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay:   TextView = view.findViewById(R.id.tvForecastDay)
        val tvDate:  TextView = view.findViewById(R.id.tvForecastDate)
        val tvEmoji: TextView = view.findViewById(R.id.tvForecastEmoji)
        val tvDesc:  TextView = view.findViewById(R.id.tvForecastDesc)
        val tvHigh:  TextView = view.findViewById(R.id.tvForecastHigh)
        val tvLow:   TextView = view.findViewById(R.id.tvForecastLow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val day = days[position]
        holder.tvDay.text   = day.dayName
        holder.tvDate.text  = day.dateStr
        holder.tvEmoji.text = iconToEmoji(day.iconCode)
        holder.tvDesc.text  = day.description
        holder.tvHigh.text  = "H: ${day.highF.toInt()}°"
        holder.tvLow.text   = "L: ${day.lowF.toInt()}°"
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<ForecastDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}
