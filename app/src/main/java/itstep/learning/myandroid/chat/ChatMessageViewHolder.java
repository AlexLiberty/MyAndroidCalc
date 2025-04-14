package itstep.learning.myandroid.chat;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import itstep.learning.myandroid.R;
import itstep.learning.myandroid.orm.ChatMessage;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder
{
    public static final SimpleDateFormat momentFormat =
            new SimpleDateFormat("dd.MM HH:mm", Locale.ROOT );
    private ChatMessage chatMessage;
    private final TextView tvAuthor;
    private final TextView tvText;
    private final TextView tvMoment;

//    public void setChatMessage(ChatMessage chatMessage) {
//        this.chatMessage = chatMessage;
//        tvAuthor.setText(this.chatMessage.getAuthor());
//        tvText.setText(this.chatMessage.getText());
//        tvMoment.setText(momentFormat.format(chatMessage.getMoment()));
//    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        tvAuthor.setText(this.chatMessage.getAuthor());
        tvText.setText(this.chatMessage.getText());

        Date moment = chatMessage.getMoment();
        String displayTime;

        Calendar msgCal = Calendar.getInstance();
        msgCal.setTime(moment);

        Calendar now = Calendar.getInstance();

        long diffMillis = now.getTimeInMillis() - msgCal.getTimeInMillis();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (DateUtils.isToday(moment.getTime()))
        {
            displayTime = android.text.format.DateFormat.format("HH:mm", moment).toString();
        }
        else if (diffDays == 1)
        {
            displayTime = "Вчора, " + android.text.format.DateFormat.format("HH:mm", moment);
        }
        else if (diffDays <= 6)
        {
            displayTime = diffDays + " дн. тому, " +
                    android.text.format.DateFormat.format("HH:mm", moment);
        }
        else
        {
            displayTime = android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", moment).toString();
        }

        tvMoment.setText(displayTime);
    }

    public ChatMessageViewHolder(@NonNull View itemView)
    {
        super(itemView);
        tvAuthor = itemView.findViewById(R.id.chat_msg_author);
        tvText = itemView.findViewById(R.id.chat_msg_text);
        tvMoment = itemView.findViewById(R.id.chat_msg_moment);
    }
}
