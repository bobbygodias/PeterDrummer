package com.andrewvox.mega3splitrescue;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rikka.shizuku.Shizuku;

public final class MainActivity extends Activity {
    private static final int SHIZUKU_PERMISSION_REQUEST = 1701;
    private static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView status;
    private Button diagnose;
    private Button repair;
    private Button freeform;

    private final Shizuku.OnBinderReceivedListener binderReceivedListener = this::refreshShizukuState;
    private final Shizuku.OnBinderDeadListener binderDeadListener = this::refreshShizukuState;
    private final Shizuku.OnRequestPermissionResultListener permissionResultListener =
            (requestCode, grantResult) -> {
                if (requestCode == SHIZUKU_PERMISSION_REQUEST) {
                    refreshShizukuState();
                    append(grantResult == PackageManager.PERMISSION_GRANTED
                            ? "Permissão Shizuku concedida."
                            : "Permissão Shizuku negada.");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener);
        Shizuku.addBinderDeadListener(binderDeadListener);
        Shizuku.addRequestPermissionResultListener(permissionResultListener);
        refreshShizukuState();
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener);
        Shizuku.removeBinderDeadListener(binderDeadListener);
        Shizuku.removeRequestPermissionResultListener(permissionResultListener);
        executor.shutdownNow();
        super.onDestroy();
    }

    private View buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.rgb(244, 246, 250));

        TextView title = new TextView(this);
        title.setText("Resgate de Tela — MEGA 3");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(24, 32, 51));
        title.setGravity(Gravity.START);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Sem root, sem formatação e sem tocar nos seus arquivos. Os comandos são fixos e reversíveis.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, dp(8), 0, dp(14));
        root.addView(subtitle, matchWrap());

        root.addView(button("1. Abrir ou instalar Shizuku", v -> openShizuku()), matchWrap());
        root.addView(button("2. Pedir permissão ao Shizuku", v -> requestShizukuPermission()), matchWrap());

        diagnose = button("3. Diagnosticar multi-janela", v -> runDiagnostic());
        root.addView(diagnose, matchWrap());

        repair = button("4. Aplicar reparo seguro", v -> runRepair());
        root.addView(repair, matchWrap());

        freeform = button("Fallback: ativar janelas livres", v -> runFreeformFallback());
        root.addView(freeform, matchWrap());

        root.addView(button("Ativar alternador na Acessibilidade", v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))), matchWrap());

        root.addView(button("Alternar divisão agora", v -> {
            boolean ok = SplitAccessibilityService.toggleSplitScreen();
            append(ok
                    ? "O Android aceitou o comando de alternar divisão."
                    : "O Android recusou o comando. Ative primeiro o serviço de acessibilidade; se já estiver ativo, o DokeOS bloqueou a ação global.");
        }), matchWrap());

        status = new TextView(this);
        status.setTextSize(14);
        status.setTextColor(Color.rgb(20, 25, 35));
        status.setBackgroundColor(Color.WHITE);
        status.setPadding(dp(14), dp(14), dp(14), dp(14));
        status.setTextIsSelectable(true);
        status.setMovementMethod(new ScrollingMovementMethod());
        status.setText("Aguardando Shizuku…");

        ScrollView statusScroll = new ScrollView(this);
        statusScroll.addView(status, matchWrap());
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        statusParams.topMargin = dp(12);
        root.addView(statusScroll, statusParams);
        return root;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setMinHeight(dp(54));
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(6);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void openShizuku() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(SHIZUKU_PACKAGE);
        if (launch != null) {
            startActivity(launch);
            return;
        }
        Intent browser = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://shizuku.rikka.app/download/"));
        startActivity(browser);
    }

    private void requestShizukuPermission() {
        if (!isShizukuAlive()) {
            append("Shizuku ainda não está em execução. Abra-o e faça o pareamento pela notificação.");
            return;
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            append("A permissão Shizuku já está concedida.");
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            append("A permissão foi negada permanentemente. Libere este aplicativo dentro do Shizuku.");
        } else {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST);
        }
    }

    private void refreshShizukuState() {
        runOnUiThread(() -> {
            boolean alive = isShizukuAlive();
            boolean granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            if (diagnose != null) diagnose.setEnabled(granted);
            if (repair != null) repair.setEnabled(granted);
            if (freeform != null) freeform.setEnabled(granted);
            append("Shizuku: " + (alive ? "em execução" : "parado")
                    + " | permissão: " + (granted ? "concedida" : "pendente")
                    + " | UID: " + (alive ? safeUid() : "—"));
        });
    }

    private boolean isShizukuAlive() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String safeUid() {
        try {
            return String.valueOf(Shizuku.getUid());
        } catch (Throwable ignored) {
            return "desconhecido";
        }
    }

    private void runDiagnostic() {
        runCommands("DIAGNÓSTICO", new String[] {
                "id",
                "am supports-multiwindow",
                "am supports-split-screen-multi-window",
                "wm get-multi-window-config",
                "settings get global force_resizable_activities",
                "settings get global enable_non_resizable_multi_window",
                "settings get global enable_freeform_support",
                "wm size",
                "wm density",
                "getprop ro.config.low_ram",
                "pm has-feature android.software.freeform_window_management"
        });
    }

    private void runRepair() {
        runCommands("REPARO", new String[] {
                "settings put global force_resizable_activities 1",
                "settings put global enable_non_resizable_multi_window 1",
                "wm set-multi-window-config --supportsNonResizable 1 --respectsActivityMinWidthHeight -1",
                "settings get global force_resizable_activities",
                "settings get global enable_non_resizable_multi_window",
                "wm get-multi-window-config"
        });
    }

    private void runFreeformFallback() {
        runCommands("JANELAS LIVRES", new String[] {
                "settings put global enable_freeform_support 1",
                "settings get global enable_freeform_support"
        });
    }

    private void runCommands(String label, String[] commands) {
        if (!hasShizukuPermission()) {
            append("Sem permissão Shizuku. Use os passos 1 e 2 primeiro.");
            return;
        }
        setBusy(true);
        append("\n=== " + label + " ===");
        executor.execute(() -> {
            for (String command : commands) {
                String output = execute(command);
                append("$ " + command + "\n" + output);
            }
            append("=== FIM: " + label + " ===\n");
            runOnUiThread(() -> setBusy(false));
        });
    }

    private boolean hasShizukuPermission() {
        return isShizukuAlive()
                && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("deprecation")
    private String execute(String command) {
        Process process = null;
        try {
            // Shizuku 13 keeps this compatibility bridge package-private. Calling it
            // reflectively avoids shipping a broad UserService for six fixed commands.
            java.lang.reflect.Method newProcess = Shizuku.class.getDeclaredMethod(
                    "newProcess", String[].class, String[].class, String.class);
            newProcess.setAccessible(true);
            process = (Process) newProcess.invoke(
                    null,
                    new String[] {"sh", "-c", command},
                    null,
                    null);
            boolean finished = process.waitFor(12, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "ERRO: comando excedeu 12 segundos.";
            }
            String stdout = readAll(process.getInputStream());
            String stderr = readAll(process.getErrorStream());
            String combined = stdout.trim();
            if (!stderr.trim().isEmpty()) {
                combined += (combined.isEmpty() ? "" : "\n") + "stderr: " + stderr.trim();
            }
            if (combined.isEmpty()) combined = "OK (sem saída)";
            return combined + "\nexit=" + process.exitValue();
        } catch (Throwable error) {
            return "ERRO: " + error.getClass().getSimpleName() + ": " + error.getMessage();
        } finally {
            if (process != null) process.destroy();
        }
    }

    private String readAll(InputStream input) throws Exception {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        return result.toString();
    }

    private void setBusy(boolean busy) {
        diagnose.setEnabled(!busy);
        repair.setEnabled(!busy);
        freeform.setEnabled(!busy);
    }

    private void append(String text) {
        runOnUiThread(() -> {
            if (status == null) return;
            CharSequence old = status.getText();
            if (old.length() > 24000) {
                old = old.subSequence(old.length() - 16000, old.length());
            }
            status.setText(old + "\n" + text);
        });
    }
}
