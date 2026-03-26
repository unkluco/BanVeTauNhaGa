package com.modules;

import javax.swing.JPanel;
import java.util.function.Consumer;

public interface AppModule {
    String  getTitle();
    JPanel  getView();
    void    setOnResult(Consumer<Object> cb);
    void    reset();
}
