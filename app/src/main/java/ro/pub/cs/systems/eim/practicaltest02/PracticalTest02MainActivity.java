package ro.pub.cs.systems.eim.practicaltest02;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.ServerThread;

public class PracticalTest02MainActivity extends AppCompatActivity {

    private EditText portEditText, wordEditText;
    private TextView resultTextView;
    private Button startServerButton, getAnagramsButton;
    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        portEditText = findViewById(R.id.portEditText);
        wordEditText = findViewById(R.id.wordEditText);
        resultTextView = findViewById(R.id.resultTextView);
        startServerButton = findViewById(R.id.startServerButton);
        getAnagramsButton = findViewById(R.id.getAnagramsButton);

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int port = Integer.parseInt(portEditText.getText().toString());
                serverThread = new ServerThread(port);
                serverThread.start();
            }
        });

        getAnagramsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = wordEditText.getText().toString();
                new Thread(() -> {
                    try {
                        Socket socket = new Socket("localhost", Integer.parseInt(portEditText.getText().toString()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        out.println(word);
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line).append("\n");
                        }

                        runOnUiThread(() -> resultTextView.setText(response.toString()));
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}
