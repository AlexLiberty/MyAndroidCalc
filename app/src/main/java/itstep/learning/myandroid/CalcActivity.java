package itstep.learning.myandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;
    private String zero;

    private String operator = "";
    private double firstOperand = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvExpression = findViewById(R.id.calc_tv_expression);
        tvResult = findViewById(R.id.calc_tv_result);
        zero = getString(R.string.calc_btn_0);

        Button btnC = findViewById(R.id.calc_btn_c);
        btnC.setOnClickListener(this::onClearClick);

        // Digit buttons
        int[] digitButtons = {R.id.calc_btn_0, R.id.calc_btn_1, R.id.calc_btn_2, R.id.calc_btn_3, R.id.calc_btn_4, R.id.calc_btn_5,
                R.id.calc_btn_6, R.id.calc_btn_7, R.id.calc_btn_8, R.id.calc_btn_9};

        for (int id : digitButtons) {
            findViewById(id).setOnClickListener(this::onDigitClick);
        }

        // Operator buttons
        findViewById(R.id.calc_btn_pl).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.calc_btn_min).setOnClickListener(v -> onOperatorClick("-"));
        findViewById(R.id.calc_btn_mul).setOnClickListener(v -> onOperatorClick("*"));
        findViewById(R.id.calc_btn_div).setOnClickListener(v -> onOperatorClick("/"));

        // Equals button
        findViewById(R.id.calc_btn_eq).setOnClickListener(this::onEqualsClick);

        // Backspace
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);

        if (savedInstanceState == null) {
            onClearClick(btnC);
        }
    }

    private void onDigitClick(View view) {
        String result = tvResult.getText().toString();
        if (result.equals(zero)) {
            result = "";
        }
        result += ((Button) view).getText();
        tvResult.setText(result);
    }

    private void onOperatorClick(String op) {
        firstOperand = Double.parseDouble(tvResult.getText().toString());
        operator = op;
        tvExpression.setText(tvResult.getText() + " " + operator);
        tvResult.setText(zero);
    }

    private void onEqualsClick(View view) {
        double secondOperand = Double.parseDouble(tvResult.getText().toString());
        double result = 0;

        switch (operator) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "-":
                result = firstOperand - secondOperand;
                break;
            case "*":
                result = firstOperand * secondOperand;
                break;
            case "/":
                if (secondOperand != 0) {
                    result = firstOperand / secondOperand;
                } else {
                    tvResult.setText("Error");
                    return;
                }
                break;
        }

        tvExpression.setText("");
        tvResult.setText(String.valueOf(result));
    }

    private void onClearClick(View view) {
        tvExpression.setText("");
        tvResult.setText(zero);
        operator = "";
        firstOperand = 0;
    }

    private void onBackspaceClick(View view) {
        String result = tvResult.getText().toString();
        if (result.length() > 1) {
            tvResult.setText(result.substring(0, result.length() - 1));
        } else {
            tvResult.setText(zero);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //Викликається коли змінюється конфігурація (поворот, зміна мови ...)
        //Надає Bundle outState - як "сховище" для збереження
        super.onSaveInstanceState(outState);
        outState.putCharSequence("result", tvResult.getText());
        outState.putCharSequence("expression", tvExpression.getText());
        outState.putString("operator", operator);
        outState.putDouble("firstOperand", firstOperand);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        //Викликається коли впроваджується нова конфігурація
        //передає saveInstanceState == outState, який зберігається при виході
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("result"));
        tvExpression.setText(savedInstanceState.getCharSequence("expression"));
        operator = savedInstanceState.getString("operator");
        firstOperand = savedInstanceState.getDouble("firstOperand");
    }
}