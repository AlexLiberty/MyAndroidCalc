package itstep.learning.myandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import itstep.learning.myandroid.nbu.NbuRateAdapter;
import itstep.learning.myandroid.orm.NbuRate;

public class RatesActivity extends AppCompatActivity {

    private static final String nbuRatesUrl= "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDate = findViewById(R.id.rates_date);

        pool = Executors.newFixedThreadPool(3);
        CompletableFuture
                .supplyAsync(this::loadRoutes, pool)
                .thenAccept(this::parseNbuResponse)
                .thenRun(this::showNbuRates);

        rvContainer = findViewById(R.id.rates_rv_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvContainer.setLayoutManager(layoutManager);
        nbuRateAdapter = new NbuRateAdapter(nbuRates);
        rvContainer.setAdapter(nbuRateAdapter);
    }

    @Override
    protected void onDestroy() {
        pool.shutdown();
        super.onDestroy();
    }

    private String loadRoutes() {
        try {
            URL url = new URL(nbuRatesUrl);
            InputStream urlStream =  url.openStream();
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
            Log.d("parseNbuResponse", "JSONException" + ex.getMessage());
        }
    }

    private void showNbuRates() {
        runOnUiThread(() -> {
            if (exchangeDate != null) {
                tvDate.setText("Курс на: " + exchangeDate);
            }
            nbuRateAdapter.notifyItemRangeChanged(0, nbuRates.size());
        });
    }
}
