package itstep.learning.myandroid;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import itstep.learning.myandroid.chat.ChatMessageAdapter;
import itstep.learning.myandroid.orm.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private static final String chatUrl = "https://chat.momentfor.fun/";
    private ExecutorService pool;
    private final List<ChatMessage> messages = new ArrayList<>();
    private EditText etAuthor;
    private RecyclerView rvContent;
    private EditText etMessage;
    private ChatMessageAdapter chatMessageAdapter;
    private final Handler handler = new Handler();
    private SwitchCompat scRemember;
    private boolean isFirstSend;
    private boolean isAuthorLocked = false;
    private final String authorFileName = "author.name";
    private final String appDatabase = "chat_db";
    private void saveMessages()
    {
        try(
        SQLiteDatabase db = openOrCreateDatabase(appDatabase, Context.MODE_PRIVATE, null))
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS chat_history(" +
                    "id ROWID, " +
                    "author VARCHAR(128), " +
                    "text VARCHAR(512), " +
                    "moment DATETIME ) "
            );

            db.execSQL("DELETE FROM chat_history");

            for (ChatMessage chatMessage : messages) {
                db.execSQL("INSERT INTO chat_history VALUES(?,?,?,?)",
                        new Object[]
                                {
                                       Integer.parseInt(chatMessage.getId()),
                                        chatMessage.getAuthor(),
                                        chatMessage.getText(),
                                        chatMessage.getMoment()
                                });
            }
        }
        catch (Exception ex)
        {
            Log.e("saveMessage", ex.getClass().getName() + " " + ex.getMessage());
        }
    }

    private void restoreMessagesAsync() {
        CompletableFuture.runAsync(() -> {
            try (SQLiteDatabase db = openOrCreateDatabase(appDatabase, Context.MODE_PRIVATE, null);
                 Cursor cursor = db.rawQuery("SELECT * FROM chat_history", null)) {

                if (cursor.moveToFirst()) {
                    synchronized (messages) {
                        do {
                            ChatMessage message = ChatMessage.fromCursor(cursor);

                            if (messages.stream().noneMatch(m -> m.getId().equals(message.getId()))) {
                                messages.add(message);
                            }

                        } while (cursor.moveToNext());

                        messages.sort(Comparator.comparing(ChatMessage::getMoment));

                        runOnUiThread(() -> {
                            chatMessageAdapter.notifyDataSetChanged();
                            rvContent.scrollToPosition(messages.size() - 1);
                        });
                    }
                }

            } catch (Exception ex) {
                Log.e("restoreMessages", ex.getClass().getName() + " " + ex.getMessage());
            }
        }, pool);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, imeBars.bottom));
            return insets;
        });

        pool = Executors.newFixedThreadPool( 3 );

        updateChat();

        etAuthor = findViewById(R.id.chat_et_author);
        etAuthor.setText(loadAuthor());
        rvContent = findViewById(R.id.chat_rv_contant);
        etMessage = findViewById(R.id.chat_et_message);
        scRemember = findViewById(R.id.chat_switch_remember);
        scRemember.setChecked(true);
        isFirstSend = true;

        chatMessageAdapter = new ChatMessageAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvContent.setLayoutManager(layoutManager);
        rvContent.setAdapter(chatMessageAdapter);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        restoreMessagesAsync();
    }

    @Override
    protected void onDestroy()
    {
        handler.removeMessages(0);
        pool.shutdownNow();
        saveMessages();
        super.onDestroy();
    }

    private void processChatResponse(List<ChatMessage> parsedMessages)
    {
        int oldSize = messages.size();

        for(ChatMessage m:parsedMessages)
        {
            if(messages.stream().noneMatch(cm->cm.getId().equals(m.getId())))
            {
                messages.add(m);
            }
        }

        int newSize = messages.size();

        if(newSize>oldSize)
        {
            messages.sort(Comparator.comparing(ChatMessage::getMoment));

            runOnUiThread(()->
            {
                chatMessageAdapter.notifyItemRangeChanged(oldSize, messages.size());
                rvContent.scrollToPosition(newSize-1);
            } );
        }
    }

    private List<ChatMessage> parseChatResponse(String body)
    {
        List<ChatMessage> res = new ArrayList<>();
        try
        {
            JSONObject root = new JSONObject(body);
            int status = root.getInt("status");

            if (status != 1)
            {
                Log.w("parseChatResponse", "Request finished with status " + status);
                return res;
            }

            JSONArray arr = root.getJSONArray("data");

            int len = arr.length();

            for (int i = 0; i < arr.length(); i++)
            {
                res.add(ChatMessage.fromJsonObject(arr.getJSONObject(i)));
            }
        }
        catch (JSONException ex)
        {
            Log.d("parseChatResponse", "JSONException: " + ex.getMessage());
        }

        return res;
    }

    private void onSendClick(View view)
    {
        String alertMessage = null;
        String author = etAuthor.getText().toString();
        String message = etMessage.getText().toString();

        if(author.isBlank())
        {
            alertMessage = getString ((R.string.chat_msg_no_name));
        }

        else if (message.isBlank())
        {
            alertMessage = getString ((R.string.chat_msg_no_msg));
        }

        if(alertMessage!=null)
        {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle(R.string.chat_msg_stop )
                    .setMessage(alertMessage)
                    .setIcon(android.R.drawable.ic_delete)
                    .setPositiveButton(R.string.chat_msg_btn ,
                            (dlg, btn)->{})
                    .setCancelable(false)
                    .show();
            return;
        }

        if (!isAuthorLocked) {
            etAuthor.setEnabled(false);
            isAuthorLocked = true;
        }

        if(isFirstSend)
        {
            isFirstSend = false;

            if(scRemember.isChecked())
            {
                saveAuthor(author);
            }
        }

        CompletableFuture.runAsync(
                ()->sendChatMessage(new ChatMessage(author, message)),
                pool
        );
    }

    private void saveAuthor(String name)
    {
        try(FileOutputStream fos =
        openFileOutput(authorFileName, Context.MODE_PRIVATE))
        {
            fos.write(name.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException ex)
        {
            Log.e("saveAuthor", "IOException" + ex.getMessage() );
        }
    }

    private String loadAuthor()
    {
        try(FileInputStream fis =
                    openFileInput(authorFileName))
        {
            return Services.readAllText(fis);
        }
        catch (IOException ex)
        {
            Log.e("loadAuthor", "IOException" + ex.getMessage() );
        }

        return "";
    }

    private void sendChatMessage(ChatMessage chatMessage)
    {
        String charset = StandardCharsets.UTF_8.name();
        try {
            String body = String.format(Locale.ROOT,
                    "author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), charset),
                    URLEncoder.encode(chatMessage.getText(), charset)
            );
            URL url = new URL(chatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true); // очікуємо відповідь (можна читати)
            connection.setDoOutput(true); // буде передача (тіло)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content_Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("X-Powered_By", "MyAndroid");
            connection.setChunkedStreamingMode(0);

            OutputStream bodyStream = connection.getOutputStream();
            bodyStream.write(body.getBytes(charset));
            bodyStream.flush();
            bodyStream.close();

            int statusCode = connection.getResponseCode();
            if(statusCode == 201)
            {
                connection.getInputStream();
                //даний сервер не надає тіло, якщо воно потрібно, то читємо getInputStream();
                runOnUiThread(() -> etMessage.setText(""));  // Очищаємо поле повідомлення
                updateChat();
            }
            else
            {
                InputStream errorStream = connection.getErrorStream();
                Log.e("sendChatMessage", Services.readAllText(errorStream));
                errorStream.close();
            }
            connection.disconnect();
        }
        catch (UnsupportedEncodingException ex)
        {
            Log.e("sendChatMessage", "UnsupportedEncodingException" + ex.getMessage());
        }
        catch (MalformedURLException ex)
        {
            Log.e("sendChatMessage", "MalformedURLException" + ex.getMessage());
        }
        catch (IOException ex)
        {
            Log.e("sendChatMessage", "IOException" + ex.getMessage());
        }
    }

    private void updateChat()
    {
        CompletableFuture
                .supplyAsync(()->Services.fetchUrl(chatUrl), pool)
                .thenApply(this::parseChatResponse)
                .thenAccept(this::processChatResponse);
//Log.i("updateChat","updated");
        handler.postDelayed(this::updateChat, 2000);

    }
}