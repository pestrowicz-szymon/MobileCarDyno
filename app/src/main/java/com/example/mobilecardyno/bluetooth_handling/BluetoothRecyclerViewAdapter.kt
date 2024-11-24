package com.example.mobilecardyno.bluetooth_handling

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilecardyno.R

class BluetoothRecyclerViewAdapter (
    private val dataSet: List<BluetoothDeviceInfo>,
    private val context: Context,
    private val onDeviceSelected: (String) -> Unit
) : RecyclerView.Adapter<BluetoothRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val button: Button = view.findViewById(R.id.button3)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.text_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position].name ?: context.getString(R.string.PL_bluetooth_unknown_device)
        viewHolder.button.setOnClickListener {
            onDeviceSelected(dataSet[position].macAddress)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
