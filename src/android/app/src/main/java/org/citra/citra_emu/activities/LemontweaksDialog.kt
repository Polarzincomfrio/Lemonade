package org.citra.citra_emu.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import org.citra.citra_emu.NativeLibrary
import org.citra.citra_emu.R

class LemontweaksDialog(context: Context) : BaseSheetDialog(context) {

    private lateinit var adapter: SettingsAdapter

    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_lemontweaks)

        val recyclerView: RecyclerView = findViewById(R.id.list_settings)
        recyclerView.layoutManager = LinearLayoutManager(getContext())
        adapter = SettingsAdapter(getContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))
    }

    private data class SettingsItem(val settingId: Int, val name: String, val typeId: Int, var value: Int)

    private abstract class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
            findViews(itemView)
        }

        protected abstract fun findViews(root: View)
        abstract fun bind(item: SettingsItem)
    }

    private inner class SwitchSettingViewHolder(itemView: View) : SettingViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
        private lateinit var item: SettingsItem
        private var textSettingName: TextView? = null
        private var materialSwitch: MaterialSwitch? = null

        override fun findViews(root: View) {
            textSettingName = root.findViewById(R.id.text_setting_name)
            materialSwitch = root.findViewById<SwitchMaterial>(R.id.switch_widget).also {
                it.setOnCheckedChangeListener(this)
            }
        }

        override fun bind(item: SettingsItem) {
            this.item = item
            textSettingName?.text = item.name
            materialSwitch?.isChecked = item.value > 0
        }

        override fun onClick(v: View?) {
            super.onClick(v)
            materialSwitch?.let {
                it.toggle()
                item.value = if (it.isChecked) 1 else 0
            }
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            item.value = if (isChecked) 1 else 0
        }
    }

    private inner class SettingsAdapter(private val context: Context) : RecyclerView.Adapter<SettingViewHolder>() {
        private val settings: MutableList<SettingsItem> = mutableListOf()

        init {
            val tweaks = NativeLibrary.getLemontweaks()
            tweaks.forEachIndexed { index, value ->
                val name = when (index) {
                    0 -> context.getString(R.string.setting_core_ticks_hack)
                    1 -> context.getString(R.string.setting_skip_slow_draw)
                    2 -> context.getString(R.string.setting_skip_texture_copy)
                    else -> ""
                }
                settings.add(SettingsItem(index, name, 0, value))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_ingame_switch, parent, false)
            return SwitchSettingViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            holder.bind(settings[position])
        }

        override fun getItemCount(): Int = settings.size

        fun saveSettings() {
            val newSettings = settings.map { it.value }.toIntArray()
            if (!newSettings.contentEquals(NativeLibrary.getLemontweaks())) {
                NativeLibrary.setLemontweaks(newSettings)
            }
        }
    }
}
