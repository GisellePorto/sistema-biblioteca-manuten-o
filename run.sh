#!/bin/bash
java -Djdk.gtk.version=3 \
  --module-path /usr/share/openjfx/lib:lib \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
  --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
  --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED \
  --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED \
  --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED \
  -cp "bin:lib/*" com.Launcher
