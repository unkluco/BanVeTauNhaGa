package com.modules;

import com.entity.DoanTau;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class ChinhSuaDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final DoanTau doanTau;

    public ChinhSuaDoanTauModule(DoanTau doanTau) {
        this.doanTau = doanTau;
        setLayout(new BorderLayout());
        setBackground(new Color(0xF8, 0xFA, 0xFC));
        buildUI();
    }

    private void buildUI() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel lbl = new JLabel("<html><div style='text-align:center;'>"
                + "<div style='font-size:15px;font-weight:bold;color:#191C1E;'>Chỉnh sửa đoàn tàu</div>"
                + "<div style='font-size:12px;color:#64748B;margin-top:8px;'>Chức năng đang được phát triển.</div>"
                + "</div></html>");
        center.add(lbl);
        add(center, BorderLayout.CENTER);
    }

    @Override public String getTitle() { return "Chỉnh sửa đoàn tàu"; }
    @Override public JPanel  getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {}
}
