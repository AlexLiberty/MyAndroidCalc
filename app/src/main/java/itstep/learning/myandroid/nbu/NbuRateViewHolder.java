package itstep.learning.myandroid.nbu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import itstep.learning.myandroid.R;
import itstep.learning.myandroid.orm.NbuRate;

public class NbuRateViewHolder extends RecyclerView.ViewHolder
{
    private final TextView tvTxt;
    private final TextView tvRate;
    private final TextView tvCC;
    private NbuRate nbuRate;


    public NbuRate getNbuRate() {
        return nbuRate;
    }

    public void setNbuRate(NbuRate nbuRate) {
        this.nbuRate = nbuRate;
        showData();
    }

    public NbuRateViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTxt = itemView.findViewById(R.id.nbu_rate_txt);
        tvCC = itemView.findViewById(R.id.nbu_rate_cc);
        tvRate = itemView.findViewById(R.id.nbu_rate_rate);
    }

    private void showData()
    {
        tvTxt.setText(nbuRate.getTxt());
        tvCC.setText(nbuRate.getCc());
        String formattedRate = String.format("1 %s = %.4f UAH\n1 UAH = %.4f %s",
                nbuRate.getCc(),
                nbuRate.getRate(),
                (1 / nbuRate.getRate()),
                nbuRate.getCc());
        tvRate.setText(formattedRate);
    }
}
