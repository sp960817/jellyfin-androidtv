package org.jellyfin.androidtv.customer.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Function;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.CustomerUserPreferences;

import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;

public interface DanmuSettingActionComponent {
    Object[][] SPEED_VALUES = new Object[][]{
            {"慢", "正常", "快"},
            {1.3f, 1.0f, 0.7f}
    };

    /**
     * 当前存储对象
     * @return 当前存储信息
     */
    CustomerUserPreferences getCustomerUserPreferences();

    DanmakuContext getDanmakuContext();

    IDanmakuView getDanmakuView();

    default void doClick(@NonNull Context context, @NonNull View view) {
        View danmuSetting = view.getRootView().findViewById(R.id.danmu_setting_info);
        if (danmuSetting == null) {
            return;
        }

        CustomerUserPreferences customerUserPreferences = getCustomerUserPreferences();

        // 速度
        SeekBar speed = danmuSetting.findViewById(R.id.danmu_setting_speed);
        TextView speedValue = danmuSetting.findViewById(R.id.danmu_setting_speed_value);
        float originDanmuSpeed = customerUserPreferences.getDanmuSpeed();
        int originProgress = (int) (originDanmuSpeed * 10 - 5);
        speed.setProgress(originProgress);
        Function<Integer, String> speedValueGetter = integer -> String.format("%.1f", (integer + 5) / 10.0f);
        speedValue.setText(speedValueGetter.apply(originProgress));
        speed.setOnSeekBarChangeListener(new TextSeekBarListener(speedValue, speedValueGetter));

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch fps = danmuSetting.findViewById(R.id.danmu_setting_fps);
        fps.setChecked(customerUserPreferences.isDanmuFps());

        // 弹幕行数
        SeekBar row = danmuSetting.findViewById(R.id.danmu_setting_row);
        TextView rowValue = danmuSetting.findViewById(R.id.danmu_setting_row_value);
        Function<Integer, String> rowValueGetter = integer -> String.valueOf(integer + 3);
        int originDanmuRow = customerUserPreferences.getDanmuRows();
        int originDanmuRowProgress = originDanmuRow - 3;
        row.setProgress(originDanmuRowProgress);
        rowValue.setText(rowValueGetter.apply(originDanmuRowProgress));
        row.setOnSeekBarChangeListener(new TextSeekBarListener(rowValue, rowValueGetter));

        // 字体大小
        SeekBar fontSize = danmuSetting.findViewById(R.id.danmu_setting_font_size);
        TextView fontSizeValue = danmuSetting.findViewById(R.id.danmu_setting_font_size_value);
        Function<Integer, String> fontSizeValueGetter = integer -> String.format("%.1f", (integer + 5) / 10.0f);
        float originDanmuFontSize = customerUserPreferences.getDanmuFontSize();
        int originFontSizeProgress = (int) (originDanmuFontSize * 10 - 5);
        fontSize.setProgress(originFontSizeProgress);
        fontSizeValue.setText(fontSizeValueGetter.apply(originFontSizeProgress));
        fontSize.setOnSeekBarChangeListener(new TextSeekBarListener(fontSizeValue, fontSizeValueGetter));

        Button confirm = danmuSetting.findViewById(R.id.confirm);
        confirm.setOnClickListener(v -> {
            DanmakuContext danmakuContext = getDanmakuContext();
            IDanmakuView danmakuView = getDanmakuView();

            /*
             * 0.5 - 2.0
             */
            float danmuSpeed = Float.parseFloat(String.valueOf(speedValue.getText()));
            if (danmuSpeed != originDanmuSpeed) {
                customerUserPreferences.setDanmuSpeed(danmuSpeed);
                danmakuContext.setScrollSpeedFactor(danmuSpeed);
            }

            /*
             * 字体大小缩放
             */
            float danmuFontSize = Float.parseFloat(String.valueOf(fontSizeValue.getText()));
            if (danmuFontSize != originDanmuFontSize) {
                customerUserPreferences.setDanmuFontSize(danmuFontSize);
                danmakuContext.setScaleTextSize(danmuFontSize);
            }

            /*
             * 弹幕行数
             */
            int danmuRow = Integer.parseInt(String.valueOf(rowValue.getText()));
            if (danmuRow != originDanmuRow) {
                customerUserPreferences.setDanmuRows(danmuRow);
                HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
                maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, danmuRow);
                danmakuContext.setMaximumLines(maxLinesPair);
            }

            /*
             * FPS
             */
            boolean checked = fps.isChecked();
            if (checked != customerUserPreferences.isDanmuFps()) {
                customerUserPreferences.setDanmuFps(checked);
                danmakuView.showFPS(checked);
            }
            danmuSetting.setVisibility(View.GONE);
        });

        Button cancel = danmuSetting.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> danmuSetting.setVisibility(View.GONE));

        speed.requestFocus();
        danmuSetting.setVisibility(View.VISIBLE);
    }
}
