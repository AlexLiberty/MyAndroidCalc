package itstep.learning.myandroid.nbu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itstep.learning.myandroid.R;
import itstep.learning.myandroid.orm.NbuRate;

public class NbuRateAdapter extends RecyclerView.Adapter<NbuRateViewHolder>
{
    private List<NbuRate> nbuRates; //не нова колекція, а посилання на активність

    public NbuRateAdapter(List<NbuRate> nbuRates) {
        this.nbuRates = nbuRates;
    }

    @NonNull
    @Override
    public NbuRateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.nbu_rate_layout, parent, false);

        return new NbuRateViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NbuRateViewHolder holder, int position) {
        holder.setNbuRate(nbuRates.get(position));
    }

    @Override
    public int getItemCount() {
        return nbuRates.size();
    }

    public void setNbuRates(List<NbuRate> nbuRates)
    {
        int oldSize = this.nbuRates.size();
        int newSize = nbuRates.size();
        if(newSize>oldSize)
        {
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize-oldSize);
        }
        else
        {
            notifyItemRangeChanged(0, newSize);
            notifyItemRangeRemoved(newSize, oldSize-newSize);
        }

        this.nbuRates = nbuRates;
    }
}
