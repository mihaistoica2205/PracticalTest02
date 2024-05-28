package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            String word = bufferedReader.readLine();
            if (word != null && !word.isEmpty()) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Received word: " + word);
                String result;
                if (serverThread.getData().containsKey(word)) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting data from the cache...");
                    result = serverThread.getData().get(word);
                } else {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting data from the web service...");
                    result = getAnagramsFromWebService(word);
                    serverThread.setData(word, result);
                }
                printWriter.println(result);
                printWriter.flush();
            }
            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
        }
    }

    private String getAnagramsFromWebService(String word) {
        String result = "";
        try {
            URL url = new URL("http://www.anagramica.com/best/" + word);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNextLine()) {
                result += scanner.nextLine();
            }
            scanner.close();

            // Process JSON response
            JSONObject jsonResponse = new JSONObject(result);
            JSONArray anagramsArray = jsonResponse.getJSONArray("best");
            StringBuilder anagrams = new StringBuilder();
            for (int i = 0; i < anagramsArray.length(); i++) {
                anagrams.append(anagramsArray.getString(i)).append(" ");
            }
            result = anagrams.toString().trim();

        } catch (Exception e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + e.getMessage());
        }
        return result;
    }
}
