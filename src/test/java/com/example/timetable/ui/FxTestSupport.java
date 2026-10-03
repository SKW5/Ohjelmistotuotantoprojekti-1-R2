package com.example.timetable.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

final class FxTestSupport {
    private static boolean started;

    private FxTestSupport() { }

    static synchronized void startToolkit() throws Exception {
        if (started) return;
        if (System.getenv("DISPLAY") == null || System.getenv("DISPLAY").isBlank()) {
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
        }
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException alreadyStarted) {
            ready.countDown();
        }
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX toolkit did not start");
        }
        Platform.setImplicitExit(false);
        started = true;
    }

    static <T> T onFxThread(CallableWithException<T> action) throws Exception {
        startToolkit();
        FutureTask<T> task = new FutureTask<>(action::call);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    static <T extends Node> T find(Node root, Class<T> type, Predicate<T> match) {
        if (type.isInstance(root) && match.test(type.cast(root))) return type.cast(root);
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                T found = find(child, type, match);
                if (found != null) return found;
            }
        }
        return null;
    }

    static <T extends Node> List<T> findAll(Node root, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(root)) found.add(type.cast(root));
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) found.addAll(findAll(child, type));
        }
        return found;
    }

    static void shutdownToolkit() throws Exception {
        if (!started) return;
        Platform.exit();
        started = false;
    }

    @FunctionalInterface
    interface CallableWithException<T> {
        T call() throws Exception;
    }
}
