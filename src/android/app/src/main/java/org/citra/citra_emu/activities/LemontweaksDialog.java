package org.citra.citra_emu.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.citra.citra_emu.activities.BaseSheetDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import org.citra.citra_emu.NativeLibrary;
import org.citra.citra_emu.R;

import java.util.ArrayList;

public class LemontweaksDialog extends BaseSheetDialog {

    private SettingsAdapter adapter;

    public LemontweaksDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_lemontweaks);

        RecyclerView recyclerView = contents.findViewById(R.id.list_settings);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SettingsAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    private static class SettingsItem {
        private final int settingId;
        private final String name;
        private final int typeId;
        private int value;

        SettingsItem(int settingId, String name, int typeId, int value) {
            this.settingId = settingId;
            this.name = name;
            this.typeId = typeId;
            this.value = value;
        }

        public int getSettingId() {
            return settingId;
        }

        public String getName() {
            return name;
        }

        public int getTypeId() {
            return typeId;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    private abstract static class SettingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SettingViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            findViews(itemView);
        }

        protected abstract void findViews(View root);

        public abstract void bind(SettingsItem item);

        @Override
        public void onClick(View v) {
            // Handle click event
        }
    }

    private class SwitchSettingViewHolder extends SettingViewHolder implements CompoundButton.OnCheckedChangeListener {
        private SettingsItem item;
        private TextView textSettingName;
        private MaterialSwitch materialSwitch;

        SwitchSettingViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void findViews(View root) {
            textSettingName = root.findViewById(R.id.text_setting_name);
            materialSwitch = root.findViewById(R.id.switch_widget);
            materialSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void bind(SettingsItem item) {
            this.item = item;
            if (textSettingName != null) textSettingName.setText(item.getName());
            if (materialSwitch != null) materialSwitch.setChecked(item.getValue() > 0);
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            if (materialSwitch != null) {
                materialSwitch.toggle();
                if (item != null) item.setValue(materialSwitch.isChecked() ? 1 : 0);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (item != null) item.setValue(isChecked ? 1 : 0);
        }
    }

    private class SettingsAdapter extends RecyclerView.Adapter<SettingViewHolder> {
        private final Context context;
        private final ArrayList<SettingsItem> settings = new ArrayList<>();

        SettingsAdapter(Context context) {
            this.context = context;
            int[] tweaks = NativeLibrary.getLemontweaks();
            for (int i = 0; i < tweaks.length; i++) {
                switch (i) {
                    case 0:
                        settings.add(new SettingsItem(0, context.getString(R.string.setting_core_ticks_hack), 0, tweaks[i]));
                        break;
                    case 1:
                        settings.add(new SettingsItem(1, context.getString(R.string.setting_skip_slow_draw), 0, tweaks[i]));
                        break;
                    case 2:
                        settings.add(new SettingsItem(2, context.getString(R.string.setting_skip_texture_copy), 0, tweaks[i]));
                        break;
                }
            }
        }

        @NonNull
        @Override
        public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View itemView = inflater.inflate(R.layout.list_item_ingame_switch, parent, false);
            return new SwitchSettingViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
            holder.bind(settings.get(position));
        }

        @Override
        public int getItemCount() {
            return settings.size();
        }

        public void saveSettings() {
            boolean isChanged = false;
            int[] newSettings = new int[settings.size()];
            for (int i = 0; i < settings.size(); i++) {
                newSettings[i] = settings.get(i).getValue();
                if (newSettings[i] != NativeLibrary.getLemontweaks()[i]) {
                    isChanged = true;
                }
            }
            if (isChanged) {
                NativeLibrary.setLemontweaks(newSettings);
            }
        }
    }
}
