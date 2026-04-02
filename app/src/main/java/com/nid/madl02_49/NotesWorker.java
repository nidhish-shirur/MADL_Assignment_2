package com.nid.madl02_49;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotesWorker extends Worker {
    public NotesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Trigger the notification for Roll No 49 (49 % 3 = 1)
        NotificationHelper.showNotification(getApplicationContext());
        return Result.success();
    }
}