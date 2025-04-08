package itstep.learning.myandroid;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import itstep.learning.myandroid.nbu.NbuRateAdapter;
import itstep.learning.myandroid.orm.NbuRate;

public class RatesActivity extends AppCompatActivity {

    private ExecutorService pool;
    private final List<NbuRate> nbuRates = new ArrayList<>();
    private NbuRateAdapter nbuRateAdapter;
    private RecyclerView rvContainer;
    private TextView tvDate;
    private String exchangeDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, imeBars.bottom));
            return insets;
        });

        tvDate = findViewById(R.id.rates_date);
        rvContainer = findViewById(R.id.rates_rv_container);
        pool = Executors.newFixedThreadPool(3);

        rvContainer.post(() -> {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            rvContainer.setLayoutManager(layoutManager);
            nbuRateAdapter = new NbuRateAdapter(nbuRates);
            rvContainer.setAdapter(nbuRateAdapter);
        });

        SearchView svFilter = findViewById(R.id.rates_sv_filter);
        svFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return onFilterChange(s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return onFilterChange(s);
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
        Date now = new Date();
        exchangeDate = dateFormat.format(now);
        tvDate.setText("Курс на: " + exchangeDate);
        loadRatesForDate(exchangeDate);

        // Date picker setup
        tvDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        String selectedDate = dateFormat.format(calendar.getTime());
                        exchangeDate = selectedDate;
                        tvDate.setText("Курс на: " + selectedDate);
                        loadRatesForDate(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void loadRatesForDate(String dateStr) {
        String url;
        String todayStr = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(new Date());

        if (dateStr.equals(todayStr)) {
            url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
        } else {
            try {
                Date date = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).parse(dateStr);
                String apiDate = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(date);
                url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=" + apiDate + "&json";
            } catch (Exception e) {
                Log.e("loadRatesForDate", "Невірна дата: " + dateStr, e);
                return;
            }
        }

        CompletableFuture
                .supplyAsync(() -> loadRoutes(url), pool)
                .thenAccept(response -> {
                    nbuRates.clear();
                    parseNbuResponse(response);
                    showNbuRates();
                });
    }

    private boolean onFilterChange(String s) {
        nbuRateAdapter.setNbuRates(nbuRates.stream()
                .filter(nbuRate -> nbuRate.getCc().toUpperCase().contains(s.toUpperCase()))
                .collect(Collectors.toList())
        );
        return true;
    }

    @Override
    protected void onDestroy() {
        pool.shutdown();
        super.onDestroy();
    }

    private String loadRoutes(String urlStr) {
        try {
            URL url = new URL(urlStr);
            InputStream urlStream = url.openStream();
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = urlStream.read(buffer)) > 0) {
                byteBuilder.write(buffer, 0, len);
            }
            String data = byteBuilder.toString(StandardCharsets.UTF_8.name());
            urlStream.close();
            return data;
        } catch (MalformedURLException ex) {
            Log.d("loadRoutes", "MalformedURLException " + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadRoutes", "IOException " + ex.getMessage());
        }
        return null;
    }

    private void parseNbuResponse(String body) {
        try {
            JSONArray arr = new JSONArray(body);
            if (arr.length() > 0) {
                exchangeDate = arr.getJSONObject(0).getString("exchangedate");
            }
            for (int i = 0; i < arr.length(); i++) {
                nbuRates.add(NbuRate.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException ex) {
            Log.d("parseNbuResponse", "JSONException: " + ex.getMessage());
        }
    }

    private void showNbuRates() {
        runOnUiThread(() -> {
            tvDate.setText("Курс на: " + exchangeDate);
            nbuRateAdapter.notifyDataSetChanged();
        });
    }
}
