package com.ids.component;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Toast {

    public static void showToast(StackPane root, String message, int durationInMillis) {
        Label toastMessage = new Label(message);
        toastMessage.setStyle("-fx-background-color: black; -fx-text-fill: white; "
                + "-fx-padding: 10px; -fx-border-radius: 5; -fx-background-radius: 5; "
                + "-fx-font-size: 14px; -fx-opacity: 0.9;");

        root.getChildren().add(toastMessage);

        // Set position (Optional: Adjust to fit your layout)
        StackPane.setMargin(toastMessage, new javafx.geometry.Insets(20));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.9);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toastMessage);
        fadeOut.setFromValue(0.9);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(durationInMillis));

        fadeIn.setOnFinished(event -> fadeOut.play());
        fadeOut.setOnFinished(event -> root.getChildren().remove(toastMessage));

        fadeIn.play();
    }
}
