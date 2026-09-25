package com.example.timetable.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {

    public SettingsView() {
        setSpacing(24);
        setPadding(new Insets(32));

        Label title = new Label("System settings");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Manage your timetable and display preferences"
        );
        subtitle.getStyleClass().add("muted");

        HBox cards = new HBox(24);
        cards.setFillHeight(true);

        VBox profile = createProfileCard();
        VBox notifications = createNotificationCard();

        cards.getChildren().addAll(profile, notifications);

        getChildren().addAll(title, subtitle, cards);
    }

    private VBox createProfileCard() {
        return createSettingsCard(
                "About the profile",
                "Basic information used by the timetable.",
                field("Username", "student"),
                field("Email", "student@example.com"),
                field("Course", "INSINÖÖRI?")
        );
    }

    private VBox createNotificationCard() {
        return createSettingsCard(
                "Notification settings",
                "Choose how you want to be notified.",
                toggleRow("Email notifications", true),
                toggleRow("Schedule reminders", true)
        );
    }

    private VBox createSettingsCard(
            String title,
            String subtitle,
            Node... nodes
    ) {
        VBox card = new VBox(14);
        card.getStyleClass().addAll("card", "settings-card");
        card.setPadding(new Insets(22));
        card.setPrefWidth(420);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("section-title", "card-heading");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted");

        card.getChildren().addAll(titleLabel, subtitleLabel);
        card.getChildren().addAll(nodes);

        return card;
    }

    private VBox field(String label, String value) {
        VBox box = new VBox(6);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().addAll("small-label", "field-label");

        TextField field = new TextField(value);
        field.getStyleClass().add("field-input");

        box.getChildren().addAll(labelNode, field);
        return box;
    }

    private HBox toggleRow(String text, boolean selected) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        CheckBox check = new CheckBox();
        check.setSelected(selected);

        row.getChildren().addAll(label, spacer, check);
        return row;
    }
}
