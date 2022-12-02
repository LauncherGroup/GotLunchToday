/*
 * MIT License
 *
 * Copyright 2016 Niklas Merkelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.schkola.kitchenscanner.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import de.schkola.kitchenscanner.R;
import de.schkola.kitchenscanner.database.Allergy;
import de.schkola.kitchenscanner.database.Customer;
import de.schkola.kitchenscanner.database.DatabaseAccess;
import de.schkola.kitchenscanner.database.LunchDatabase;
import de.schkola.kitchenscanner.task.TaskRunner;
import de.schkola.kitchenscanner.util.LunchResult;
import de.schkola.kitchenscanner.util.StringUtil;
import de.schkola.kitchenscanner.util.TorchManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> qrScanner = registerForActivityResult(new ScanContract(), this::handleScanResult);
    private ScheduledExecutorService s;
    private TorchManager tm;
    private LunchDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Intent intent = getIntent();
        if (isRescanEnabled()) {
            s = Executors.newSingleThreadScheduledExecutor();
        } else {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(view -> startScan());
        }
        tm = new TorchManager(this);
        if (Objects.equals(intent.getAction(), Intent.ACTION_RUN)) {
            startScan();
        }
        database = new DatabaseAccess(getApplicationContext()).getDatabase();
    }

    @Override
    public void finish() {
        //Shutdown ExecutorService
        if (s != null) {
            s.shutdownNow();
        }
        tm.shutdown();
        super.finish();
    }

    private void clearInformation() {
        TextView name = findViewById(R.id.name);
        TextView clazz = findViewById(R.id.clazz);
        TextView lunch = findViewById(R.id.lunch);
        TextView gotToday = findViewById(R.id.gotToday);
        TextView allergies = findViewById(R.id.allergies);
        name.setText("");
        clazz.setText("");
        lunch.setText("");
        gotToday.setText("");
        allergies.setText("");
    }

    private void fillInformation(@NonNull LunchResult result) {
        clearInformation();
        Customer c = result.getCustomer();
        List<Allergy> a = result.getAllergies();
        TextView name = findViewById(R.id.name);
        TextView clazz = findViewById(R.id.clazz);
        TextView lunch = findViewById(R.id.lunch);
        TextView gotToday = findViewById(R.id.gotToday);
        TextView allergies = findViewById(R.id.allergies);
        if (c != null) {
            c.gotLunch += 1;
            name.setText(c.name);
            if (c.grade.matches("\\d+")) {
                clazz.setText(getString(R.string.x_class, c.grade));
            } else {
                clazz.setText(c.grade);
            }
            lunch.setText(StringUtil.getLunch(c.lunch));
            if (c.gotLunch > 1) {
                gotToday.setText(getString(R.string.gotLunch, c.gotLunch));
            } else {
                gotToday.setText("");
            }
            allergies.setText(StringUtil.getAllergies(a));
            TaskRunner.INSTANCE.executeAsync(() -> database.customerDao().updateCustomer(c));
        } else {
            lunch.setText("X");
        }
    }

    private boolean isRescanEnabled() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref != null) {
            return pref.getBoolean("rescan_enabled", true);
        }
        return true;
    }

    private int getRescanTime() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref != null) {
            String rescan = pref.getString("rescan_time", "2");
            if (rescan != null) {
                return Integer.parseInt(rescan);
            }
        }
        return 2;
    }

    /**
     * Starts barcode scan
     */
    private void startScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.addExtra("RESULT_DISPLAY_DURATION_MS", 10L);
        qrScanner.launch(options);
    }

    private void handleScanResult(@NonNull ScanIntentResult result) {
        if (result.getContents() == null) {
            finish();
            return;
        }
        TaskRunner.INSTANCE.executeAsync(() -> {
            List<Allergy> a = new ArrayList<>();
            String cleanedString = result.getContents().trim().replaceAll("\\D", "");
            try {
                int xba = Integer.parseInt(cleanedString);
                Customer c = database.customerDao().getCustomer(xba);
                if (c != null) {
                    a.addAll(database.allergyDao().getAllergies(c.xba));
                }
                return new LunchResult(c, a);
            } catch (NumberFormatException e) {
                return new LunchResult(null, a);
            }
        }, lunchResult -> {
            fillInformation(lunchResult);
            if (isRescanEnabled()) {
                s.schedule(this::startScan, getRescanTime(), TimeUnit.SECONDS);
            }
        });
    }
}
