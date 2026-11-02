package com.example.timetable.ui;

import com.example.timetable.i18n.I18n;

import com.example.timetable.model.UserProfile;
import com.example.timetable.repository.profileRepository;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SettingsView extends VBox {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final profileRepository profileRepository;
    private final int loggedInUserId;
    private final Consumer<UserProfile> onProfileSaved;
    private final boolean scheduleRemindersEnabled;
    private final Consumer<Boolean> onScheduleRemindersChanged;
    private TextField usernameField;
    private TextField emailField;
    private TextField majorField;
    private Button saveButton;
    private Label profileMessage;

    public SettingsView() {
        this(null, 0, ignored -> { }, true, ignored -> { });
    }

    public SettingsView(
            profileRepository profileRepository,
            int loggedInUserId,
            Consumer<UserProfile> onProfileSaved
    ) {
        this(profileRepository, loggedInUserId, onProfileSaved, true, ignored -> { });
    }

    public SettingsView(
            profileRepository profileRepository,
            int loggedInUserId,
            Consumer<UserProfile> onProfileSaved,
            boolean scheduleRemindersEnabled,
            Consumer<Boolean> onScheduleRemindersChanged
    ) {
        this.profileRepository = profileRepository;
        this.loggedInUserId = loggedInUserId;
        this.onProfileSaved = onProfileSaved;
        this.scheduleRemindersEnabled = scheduleRemindersEnabled;
        this.onScheduleRemindersChanged = onScheduleRemindersChanged;

        I18n.orient(this);
        setSpacing(24);
        setPadding(new Insets(32));

        Label title = I18n.label("settings.title");
        title.getStyleClass().add("page-title");

        Label subtitle = I18n.label("settings.subtitle");
        subtitle.getStyleClass().add("muted");

        HBox cards = new HBox(24);
        cards.setFillHeight(true);

        VBox profile = createProfileCard();
        VBox notifications = createNotificationCard();

        cards.getChildren().addAll(profile, notifications);

        getChildren().addAll(title, subtitle, I18n.languageSelector(), cards);

        if (isGuest()) {
            setProfileInputsDisabled(true);
            saveButton.setDisable(true);
            showInfo("profile.loginRequired");
        } else {
            loadProfile();
        }
    }

    private VBox createProfileCard() {
        usernameField = profileField("auth.username");
        emailField = profileField("auth.email");
        majorField = profileField("profile.major");

        saveButton = I18n.button("common.save");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(event -> saveProfile());

        profileMessage = new Label();
        profileMessage.getStyleClass().add("muted");
        profileMessage.setWrapText(true);

        return createSettingsCard(
                "profile.title",
                "profile.subtitle",
                field("auth.username", usernameField),
                field("auth.email", emailField),
                field("profile.major", majorField),
                saveButton,
                profileMessage
        );
    }

    private VBox createNotificationCard() {
        return createSettingsCard(
                "notifications.title",
                "notifications.subtitle",
                toggleRow("notifications.email", false, ignored -> { }, true),
                toggleRow("notifications.reminders", scheduleRemindersEnabled, onScheduleRemindersChanged, false)
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

        Label titleLabel = I18n.label(title);
        titleLabel.getStyleClass().addAll("section-title", "card-heading");

        Label subtitleLabel = I18n.label(subtitle);
        subtitleLabel.getStyleClass().add("muted");

        card.getChildren().addAll(titleLabel, subtitleLabel);
        card.getChildren().addAll(nodes);

        return card;
    }

    private TextField profileField(String prompt) {
        TextField field = new TextField();
        I18n.bind(field.promptTextProperty(), prompt);
        field.getStyleClass().add("field-input");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private VBox field(String label, TextField field) {
        VBox box = new VBox(6);

        Label labelNode = I18n.label(label);
        labelNode.getStyleClass().addAll("small-label", "field-label");

        box.getChildren().addAll(labelNode, field);
        return box;
    }

    private void loadProfile() {
        setProfileInputsDisabled(true);
        showInfo("profile.loading");

        Task<UserProfile> task = new Task<>() {
            @Override
            protected UserProfile call() throws SQLException {
                return profileRepository.findById(loggedInUserId);
            }
        };

        task.setOnSucceeded(event -> {
            UserProfile profile = task.getValue();
            if (profile == null) {
                showError("profile.notFound");
                saveButton.setDisable(true);
                return;
            }

            usernameField.setText(profile.getUsername());
            emailField.setText(profile.getEmail());
            majorField.setText(profile.getMajor() == null ? "" : profile.getMajor());
            setProfileInputsDisabled(false);
            showInfo("");
        });

        task.setOnFailed(event -> {
            setProfileInputsDisabled(false);
            showError("profile.loadFailed");
        });

        Thread loader = new Thread(task, "profile-load");
        loader.setDaemon(true);
        loader.start();
    }

    private void saveProfile() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String major = majorField.getText().trim();

        usernameField.setText(username);
        emailField.setText(email);
        majorField.setText(major);

        String validationError = validateProfile(username, email, major);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        UserProfile profile = new UserProfile(
                loggedInUserId,
                username,
                email,
                major.isEmpty() ? null : major
        );

        setProfileInputsDisabled(true);
        showInfo("profile.saving");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return profileRepository.updateProfile(profile);
            }
        };

        task.setOnSucceeded(event -> {
            if (!task.getValue()) {
                setProfileInputsDisabled(false);
                showError("profile.notUpdated");
                return;
            }

            setProfileInputsDisabled(false);
            onProfileSaved.accept(profile);
            showSuccess("profile.saved");
        });

        task.setOnFailed(event -> {
            setProfileInputsDisabled(false);
            showError(profileSaveError(task.getException()));
        });

        Thread saver = new Thread(task, "profile-save");
        saver.setDaemon(true);
        saver.start();
    }

    private String validateProfile(String username, String email, String major) {
        if (username.isEmpty()) {
            return "profile.usernameRequired";
        }
        if (email.isEmpty()) {
            return "profile.emailRequired";
        }
        if (username.length() > 50) {
            return "profile.usernameLength";
        }
        if (email.length() > 100) {
            return "profile.emailLength";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "profile.invalidEmail";
        }
        if (major.length() > 100) {
            return "profile.majorLength";
        }
        return null;
    }

    private String profileSaveError(Throwable error) {
        if (error instanceof SQLException sqlError
                && (sqlError.getErrorCode() == 1062 || "23000".equals(sqlError.getSQLState()))) {
            return "profile.duplicate";
        }
        return "profile.saveFailed";
    }

    private void setProfileInputsDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        emailField.setDisable(disabled);
        majorField.setDisable(disabled);
        saveButton.setDisable(disabled || isGuest());
    }

    private boolean isGuest() {
        return loggedInUserId <= 0 || profileRepository == null;
    }

    private void showInfo(String message) {
        profileMessage.getStyleClass().removeAll("status-error", "status-success");
        I18n.bind(profileMessage.textProperty(), message);
    }

    private void showError(String message) {
        profileMessage.getStyleClass().remove("status-success");
        if (!profileMessage.getStyleClass().contains("status-error")) {
            profileMessage.getStyleClass().add("status-error");
        }
        switch (message) {
            case "profile.usernameLength" -> I18n.bind(profileMessage.textProperty(), message, 50);
            case "profile.emailLength", "profile.majorLength" -> I18n.bind(profileMessage.textProperty(), message, 100);
            default -> I18n.bind(profileMessage.textProperty(), message);
        }
    }

    private void showSuccess(String message) {
        profileMessage.getStyleClass().remove("status-error");
        if (!profileMessage.getStyleClass().contains("status-success")) {
            profileMessage.getStyleClass().add("status-success");
        }
        I18n.bind(profileMessage.textProperty(), message);
    }

    private HBox toggleRow(String text, boolean selected) {
        return toggleRow(text, selected, ignored -> { }, true);
        }

        private HBox toggleRow(
            String text,
            boolean selected,
            Consumer<Boolean> onChanged,
            boolean disabled
        ) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = I18n.label(text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        CheckBox check = new CheckBox();
        check.setSelected(selected);
        check.setDisable(disabled);
        check.setOnAction(event -> onChanged.accept(check.isSelected()));

        row.getChildren().addAll(label, spacer, check);
        return row;
    }
}
