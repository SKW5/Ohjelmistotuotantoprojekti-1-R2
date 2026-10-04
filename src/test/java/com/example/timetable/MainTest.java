package com.example.timetable;

import com.example.timetable.model.UserProfile;
import com.example.timetable.repository.loginRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException alreadyStarted) {
            ready.countDown();
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    void mainIsJavaFxApplicationEntryPoint() throws Exception {
        assertTrue(Application.class.isAssignableFrom(Main.class));
        Method main = Main.class.getMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isStatic(main.getModifiers()));
    }

    @Test
    void handleLoginSuccessLoadsUserAndShowsApplication() throws Exception {
        Main main = new Main();
        set(main, "scene", new Scene(new StackPane()));
        set(main, "loginRepository", loginRepositoryFor("ada@example.com", 42, "Ada"));

        onFxThread(() -> invoke(main, "handleLoginSuccess", new Class<?>[]{String.class}, "ada@example.com"));

        assertEquals(42, get(main, "loggedInUserId", Integer.class).intValue());
        assertEquals("ada@example.com", get(main, "loggedInUserEmail"));
        assertEquals("Ada", get(main, "loggedInUserName"));
        UserProfile profile = get(main, "loggedInUserProfile");
        assertEquals(42, profile.getUserId());
        assertEquals("Ada", profile.getUsername());
        assertEquals("ada@example.com", profile.getEmail());
        assertTrue(get(main, "scene", Scene.class).getRoot() instanceof javafx.scene.layout.BorderPane);
    }

    @Test
    void handleLoginSuccessUsesEmailWhenUsernameIsMissingAndRepositoryIsUnavailable() throws Exception {
        Main main = new Main();
        set(main, "scene", new Scene(new StackPane()));
        onFxThread(() -> invoke(main, "handleLoginSuccess", new Class<?>[]{String.class}, "ada@example.com"));

        assertEquals("ada@example.com", get(main, "loggedInUserName"));
        assertEquals("ada@example.com", get(main, "loggedInUserEmail"));
        assertNull(get(main, "loggedInUserProfile"));
    }

    @Test
    void handleProfileSavedUpdatesUserStateAndHeader() throws Exception {
        Main main = new Main();
        set(main, "scene", new Scene(new StackPane()));
        UserProfile profile = new UserProfile(17, "Grace", "grace@example.com", "CS");

        onFxThread(() -> {
            invoke(main, "showApplication", new Class<?>[0]);
            invoke(main, "handleProfileSaved", new Class<?>[]{UserProfile.class}, profile);
            return null;
        });

        assertEquals(17, get(main, "loggedInUserId", Integer.class).intValue());
        assertEquals("Grace", get(main, "loggedInUserName"));
        assertEquals("grace@example.com", get(main, "loggedInUserEmail"));
        assertSame(profile, get(main, "loggedInUserProfile"));
        Label status = findLabel(get(main, "header"));
        assertEquals("Logged in: Grace", status.getText());
    }

    @Test
    void logoutClearsUserStateAndReturnsToLanding() throws Exception {
        Main main = new Main();
        set(main, "scene", new Scene(new StackPane()));
        set(main, "loggedInUserId", 9);
        set(main, "loggedInUserName", "Ada");
        set(main, "loggedInUserEmail", "ada@example.com");
        set(main, "loggedInUserProfile", new UserProfile(9, "Ada", "ada@example.com", null));

        onFxThread(() -> invoke(main, "logout", new Class<?>[0]));

        assertEquals(0, get(main, "loggedInUserId", Integer.class).intValue());
        assertNull(get(main, "loggedInUserName"));
        assertNull(get(main, "loggedInUserEmail"));
        assertNull(get(main, "loggedInUserProfile"));
        assertEquals("LandingView", get(main, "scene", Scene.class).getRoot().getClass().getSimpleName());
    }

    @Test
    void openDatabaseConnectionUsesConfiguredConnectionProperties() throws Exception {
        AtomicReference<String> actualUrl = new AtomicReference<>();
        AtomicReference<Properties> actualProperties = new AtomicReference<>();
        Connection expectedConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> defaultValue(method.getReturnType()));
        Driver driver = new Driver() {
            @Override public Connection connect(String url, Properties info) {
                if (!acceptsURL(url)) return null;
                actualUrl.set(url);
                actualProperties.set((Properties) info.clone());
                return expectedConnection;
            }
            @Override public boolean acceptsURL(String url) { return url.startsWith("jdbc:mariadb:"); }
            @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
            @Override public int getMajorVersion() { return 1; }
            @Override public int getMinorVersion() { return 0; }
            @Override public boolean jdbcCompliant() { return false; }
            @Override public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(driver);
        try {
            Connection actual = new Main().openDatabaseConnection();
            assertSame(expectedConnection, actual);
            assertEquals(System.getenv().getOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/student_timetable"), actualUrl.get());
            assertEquals(System.getenv().getOrDefault("DB_USER", "student"), actualProperties.get().getProperty("user"));
            assertEquals(System.getenv().getOrDefault("DB_PASSWORD", "student"), actualProperties.get().getProperty("password"));
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }

    private static loginRepository loginRepositoryFor(String expectedEmail, int id, String username) {
        ResultSet result = (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getInt" -> id;
                    case "getString" -> username;
                    default -> defaultValue(method.getReturnType());
                });
        Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> method.getName().equals("prepareStatement")
                        ? preparedStatement(expectedEmail, result) : defaultValue(method.getReturnType()));
        return new loginRepository(connection);
    }

    private static PreparedStatement preparedStatement(String expectedEmail, ResultSet result) {
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setString" -> { assertEquals(expectedEmail, args[1]); yield null; }
                    case "executeQuery" -> result;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Label findLabel(Object header) throws Exception {
        Method getChildren = header.getClass().getMethod("getChildrenUnmodifiable");
        @SuppressWarnings("unchecked")
        java.util.List<javafx.scene.Node> children = (java.util.List<javafx.scene.Node>) getChildren.invoke(header);
        return children.stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> label.getText().startsWith("Logged in:")).findFirst().orElseThrow();
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }

    private static <T> T onFxThread(ThrowingSupplier<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action::get);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = Main.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = Main.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object target, String name) throws Exception {
        return (T) get(target, name, Object.class);
    }

    private static <T> T get(Object target, String name, Class<T> type) throws Exception {
        Field field = Main.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> { T get() throws Exception; }
}
