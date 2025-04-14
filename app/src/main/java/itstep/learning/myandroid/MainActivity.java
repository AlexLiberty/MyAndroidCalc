package itstep.learning.myandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button calcButton = findViewById(R.id.main_btn_calc);
        calcButton.setOnClickListener(this::onCalcButtonClick);
        findViewById(R.id.main_btn_rates).setOnClickListener(this::onRatesButtonClick);

        Button animButton = new Button(this);
        animButton.setText(R.string.main_button_anim);
        animButton.setOnClickListener(this::onAnimButtonClick);
        animButton.setBackground(calcButton.getBackground());
        animButton.setTextColor(calcButton.getTextColors());
        animButton.setFontFeatureSettings(calcButton.getFontFeatureSettings());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = 25;
        animButton.setLayoutParams(layoutParams);
        LinearLayout container = findViewById(R.id.main_ll_container);
        container.addView(animButton);

        Button chatButton = findViewById(R.id.main_btn_chat);
        calcButton.setOnClickListener(this::onCalcButtonClick);
        findViewById(R.id.main_btn_chat).setOnClickListener(this::onChatButtonClick);
    }

    private void onCalcButtonClick(View view)
    {
        startActivity(new Intent(this, CalcActivity.class));
    }

    private void onRatesButtonClick(View view)
    {
        startActivity(new Intent(this, RatesActivity.class));
    }

    private void onAnimButtonClick(View view)
    {
        startActivity(new Intent(this, AnimActivity.class));
    }

    private void onChatButtonClick(View view)
    {
        startActivity(new Intent(this, ChatActivity.class));
    }
}