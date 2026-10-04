package com.example.timetable.ui;

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

        if (isGuest()) {
            setProfileInputsDisabled(true);
            saveButton.setDisable(true);
            showInfo("Log in to edit and save your profile.");
        } else {
            loadProfile();
        }
    }

    private VBox createProfileCard() {
        usernameField = profileField("Username");
        emailField = profileField("Email");
        majorField = profileField("Major");

        saveButton = new Button("Save");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(event -> saveProfile());

        profileMessage = new Label();
        profileMessage.getStyleClass().add("muted");
        profileMessage.setWrapText(true);

        return createSettingsCard(
                "About the profile",
                "Basic information used by the timetable.",
                field("Username", usernameField),
                field("Email", emailField),
                field("Major", majorField),
                saveButton,
                profileMessage
        );
    }

    private VBox createNotificationCard() {
        return createSettingsCard(
                "Notification settings",
                "Choose how you want to be notified.",
                toggleRow("Email notifications (not configured)", false, ignored -> { }, true),
                toggleRow("Schedule reminders", scheduleRemindersEnabled, onScheduleRemindersChanged, false)
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

    private TextField profileField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("field-input");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private VBox field(String label, TextField field) {
        VBox box = new VBox(6);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().addAll("small-label", "field-label");

        box.getChildren().addAll(labelNode, field);
        return box;
    }

    private void loadProfile() {
        setProfileInputsDisabled(true);
        showInfo("Loading profile...");

        Task<UserProfile> task = new Task<>() {
            @Override
            protected UserProfile call() throws SQLException {
                return profileRepository.findById(loggedInUserId);
            }
        };

        task.setOnSucceeded(event -> {
            UserProfile profile = task.getValue();
            if (profile == null) {
                showError("Profile was not found. Log in again.");
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
            showError("Could not load profile from MariaDB. Try opening Settings again.");
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
        showInfo("Saving profile...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return profileRepository.updateProfile(profile);
            }
        };

        task.setOnSucceeded(event -> {
            if (!task.getValue()) {
                setProfileInputsDisabled(false);
                showError("Profile was not updated. Log in again and retry.");
                return;
            }

            setProfileInputsDisabled(false);
            onProfileSaved.accept(profile);
            showSuccess("Profile saved.");
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
            return "Username is required.";
        }
        if (email.isEmpty()) {
            return "Email is required.";
        }
        if (username.length() > 50) {
            return "Username must be 50 characters or shorter.";
        }
        if (email.length() > 100) {
            return "Email must be 100 characters or shorter.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address.";
        }
        if (major.length() > 100) {
            return "Major must be 100 characters or shorter.";
        }
        return null;
    }

    private String profileSaveError(Throwable error) {
        if (error instanceof SQLException sqlError
                && (sqlError.getErrorCode() == 1062 || "23000".equals(sqlError.getSQLState()))) {
            return "That username or email is already used by another account.";
        }
        return "Could not save profile to MariaDB. Your changes are still here; try again.";
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
        profileMessage.setText(message);
    }

    private void showError(String message) {
        profileMessage.getStyleClass().remove("status-success");
        if (!profileMessage.getStyleClass().contains("status-error")) {
            profileMessage.getStyleClass().add("status-error");
        }
        profileMessage.setText(message);
    }

    private void showSuccess(String message) {
        profileMessage.getStyleClass().remove("status-error");
        if (!profileMessage.getStyleClass().contains("status-success")) {
            profileMessage.getStyleClass().add("status-success");
        }
        profileMessage.setText(message);
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

        Label label = new Label(text);

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
