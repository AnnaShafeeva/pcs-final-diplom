import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SearchServer {

    private int port;

    public SearchServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server at port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
            System.out.println("Server started");
            while (true) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {
                    String clientsRequest = in.readLine();
                    List<PageEntry> searchResult = engine.search(clientsRequest);
                    List<String> answer = new ArrayList<>();
                    for (PageEntry pageEntry : searchResult) {
                        answer.add(gson.toJson(pageEntry));
                    }
                    out.println(answer);
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}