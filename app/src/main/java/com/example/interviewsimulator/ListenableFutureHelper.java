package com.example.interviewsimulator;

import android.content.Context;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

public class ListenableFutureHelper {
    public interface Callback { void onReady(ProcessCameraProvider provider); }
    public static void run(Context c, Callback cb) {
        ListenableFuture<ProcessCameraProvider> f = ProcessCameraProvider.getInstance(c);
        f.addListener(() -> { try { cb.onReady(f.get()); } catch (Exception ignored) {} }, ContextCompat.getMainExecutor(c));
    }
}
