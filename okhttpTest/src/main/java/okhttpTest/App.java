/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package okhttpTest;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App {
    
    OkHttpClient client = new OkHttpClient();
 
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
 
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());

        String url = "https://165.243.46.12:8443/mc2/rest/users/A00220190028/tags?apiKey=b0d6b650-41fc-48c1-bf76-1432ab77b2b1";
        try {
            String responseString = new App().run(url);
            System.out.println(responseString);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
