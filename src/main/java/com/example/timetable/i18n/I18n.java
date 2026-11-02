package com.example.timetable.i18n;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.Property;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.geometry.NodeOrientation;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.DecimalStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/** Application-wide presentation locale; stored values always remain locale independent. */
public final class I18n {
    public static final Locale ENGLISH = Locale.UK;
    public static final Locale FINNISH = Locale.forLanguageTag("fi-FI");
    public static final Locale ARABIC = Locale.forLanguageTag("ar-EG");
    public static final Locale JAPANESE = Locale.JAPAN;
    public static final List<Locale> LANGUAGES = List.of(ENGLISH, FINNISH, ARABIC, JAPANESE);
    private static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(ENGLISH) {
        @Override protected void invalidated() {
            Locale.setDefault(Locale.Category.FORMAT, get());
        }
    };

    private I18n() { }

    public static Locale locale() { return LOCALE.get(); }
    public static ObjectProperty<Locale> localeProperty() { return LOCALE; }

    public static void orient(Node node) {
        node.nodeOrientationProperty().bind(Bindings.createObjectBinding(() ->
                locale().getLanguage().equals("ar") ? NodeOrientation.RIGHT_TO_LEFT
                        : NodeOrientation.LEFT_TO_RIGHT, LOCALE));
    }

    public static void setLocale(Locale locale) {
        if (!LANGUAGES.contains(locale)) throw new IllegalArgumentException("Unsupported locale: " + locale);
        // JavaFX's built-in DatePicker popup reads the default FORMAT locale.
        Locale.setDefault(Locale.Category.FORMAT, locale);
        LOCALE.set(locale);
    }

    public static void restoreLanguage() {
        try {
            String tag = Preferences.userNodeForPackage(I18n.class).get("language", ENGLISH.toLanguageTag());
            Locale saved = Locale.forLanguageTag(tag);
            setLocale(LANGUAGES.contains(saved) ? saved : ENGLISH);
        } catch (SecurityException ignored) { setLocale(ENGLISH); }
    }

    private static void selectLanguage(Locale locale) {
        setLocale(locale);
        try { Preferences.userNodeForPackage(I18n.class).put("language", locale.toLanguageTag()); }
        catch (SecurityException ignored) { /* Selection still works for this session. */ }
    }

    public static String text(String key, Object... arguments) {
        if (key == null || key.isEmpty()) return "";
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale(),
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        return new MessageFormat(bundle.getString(key), locale()).format(arguments);
    }

    public static void bind(Property<String> property, String key, Object... arguments) {
        bind(property, () -> text(key, arguments));
    }

    public static void bind(Property<String> property, Supplier<String> value) {
        property.unbind();
        property.bind(Bindings.createStringBinding(value::get, LOCALE));
    }

    public static Label label(String key) {
        Label label = new Label();
        bind(label.textProperty(), key);
        label.setWrapText(true);
        return label;
    }

    public static Button button(String key) {
        Button button = new Button();
        bind(button.textProperty(), key);
        button.setWrapText(true);
        button.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        return button;
    }

    public static MenuItem menuitem(String key) {
        MenuItem item = new MenuItem();
        bind(item.textProperty(), key);
        return item;
    }

    public static HBox languageSelector() {
        ComboBox<Locale> choices = new ComboBox<>();
        choices.getItems().setAll(LANGUAGES);
        choices.setId("language-selector");
        choices.setConverter(new StringConverter<>() {
            public String toString(Locale value) { return value == null ? "" : text("language." + value.getLanguage()); }
            public Locale fromString(String value) { throw new UnsupportedOperationException(); }
        });
        choices.valueProperty().bindBidirectional(LOCALE);
        choices.setOnAction(event -> { if (choices.getValue() != null) selectLanguage(choices.getValue()); });
        bind(choices.accessibleTextProperty(), "language.label");
        Label label = label("language.label");
        label.setLabelFor(choices);
        HBox box = new HBox(8, label, choices);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        return box;
    }

    public static String number(Number number) { return NumberFormat.getNumberInstance(locale()).format(number); }
    public static DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale())
                .withDecimalStyle(DecimalStyle.of(locale()));
    }
    public static String date(LocalDate date) { return dateFormatter().format(date); }
    public static String time(LocalTime time) {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale())
                .withDecimalStyle(DecimalStyle.of(locale())).format(time);
    }

    public static void localize(DatePicker picker) {
        picker.converterProperty().bind(Bindings.createObjectBinding(() -> new StringConverter<LocalDate>() {
            public String toString(LocalDate value) { return value == null ? "" : date(value); }
            public LocalDate fromString(String value) {
                return value == null || value.isBlank() ? null : LocalDate.parse(value.trim(), dateFormatter());
            }
        }, LOCALE));
        picker.valueProperty().addListener((observable, oldValue, value) ->
                picker.getEditor().setText(picker.getConverter().toString(value)));
        picker.converterProperty().addListener((observable, oldValue, value) -> {
            picker.hide();
            // Dispose only after all converter listeners on the old skin have finished.
            javafx.application.Platform.runLater(() -> {
                if (picker.getSkin() != null) picker.setSkin(new javafx.scene.control.skin.DatePickerSkin(picker));
            });
            picker.getEditor().setText(value.toString(picker.getValue()));
        });
        picker.setShowWeekNumbers(false);
        bind(picker.promptTextProperty(), () -> date(LocalDate.of(2026, 12, 31)));
    }

    public static void localizeTimes(ComboBox<String> choices) {
        localizeCells(choices, value -> time(LocalTime.parse(value)));
        choices.converterProperty().bind(Bindings.createObjectBinding(() -> new StringConverter<String>() {
            public String toString(String value) { return value == null ? "" : time(LocalTime.parse(value)); }
            public String fromString(String value) { throw new UnsupportedOperationException(); }
        }, LOCALE));
    }

    public static void localizeColors(ComboBox<String> choices) {
        localizeCells(choices, value -> text("color." + value));
        choices.converterProperty().bind(Bindings.createObjectBinding(() -> new StringConverter<String>() {
            public String toString(String value) { return value == null ? "" : text("color." + value); }
            public String fromString(String value) { throw new UnsupportedOperationException(); }
        }, LOCALE));
    }

    private static void localizeCells(ComboBox<String> choices, java.util.function.Function<String, String> display) {
        Supplier<ListCell<String>> cell = () -> new ListCell<>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                textProperty().unbind();
                if (empty || value == null) setText(null);
                else bind(textProperty(), () -> display.apply(value));
            }
        };
        choices.setCellFactory(list -> cell.get());
        choices.setButtonCell(cell.get());
    }

    public static void localizeAlert(Alert alert) {
        orient(alert.getDialogPane());
        alert.getButtonTypes().setAll(new ButtonType(text("common.ok"), ButtonBar.ButtonData.OK_DONE));
        Button button = (Button) alert.getDialogPane().lookupButton(alert.getButtonTypes().getFirst());
        bind(button.textProperty(), "common.ok");
    }
}
