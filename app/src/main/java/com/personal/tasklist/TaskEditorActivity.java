package com.personal.tasklist;

/** Home-screen overlay using the same offline WebView and editor as the app. */
public final class TaskEditorActivity extends MainActivity {
    @Override protected boolean isWidgetEditor() { return true; }
}
