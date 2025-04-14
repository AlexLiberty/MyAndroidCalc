package itstep.learning.myandroid.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itstep.learning.myandroid.R;
import itstep.learning.myandroid.orm.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>
{
    private final List<ChatMessage> messages;

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View itemVieW = LayoutInflater
               .from(parent.getContext())
                       .inflate(R.layout.chat_msg_layout,parent,false);
        return new ChatMessageViewHolder(itemVieW);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position)
    {
        holder.setChatMessage(messages.get(position));
    }

    @Override
    public int getItemCount()
    {
        return messages.size();
    }
}
