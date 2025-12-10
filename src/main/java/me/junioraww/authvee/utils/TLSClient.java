package me.junioraww.authvee.utils;

import com.google.gson.Gson;
import me.junioraww.authvee.AuthVee;
import me.junioraww.authvee.network.Request;
import me.junioraww.authvee.network.Response;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TLSClient {
  private SSLSocket socket;
  private BufferedReader in;
  private PrintWriter out;

  private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
  private volatile boolean isRunning = true;
  private SSLContext sslContext;

  private final int[] RETRY_DELAYS = {3, 5, 10}; // secs
  private final int MAX_DELAY = 30; // sex

  public void init() {
    try {
      setupSSLContext();
    } catch (Exception e) {
      System.err.println("Failed to init SSL context:");
      e.printStackTrace();
      return;
    }

    Thread connectionThread = new Thread(this::connectionLoop, "TLS-Connection-Manager");
    connectionThread.setDaemon(true);
    connectionThread.start();
  }

  private void connectionLoop() {
    int attempt = 0;

    while (isRunning) {
      try {
        System.out.println("Connecting to server...");
        connect();

        System.out.println("Connected!");
        attempt = 0;

        readLoop();

      } catch (Exception e) {
        System.err.println("Connection lost: " + e.getMessage());
      } finally {
        close();
      }

      if (!isRunning) break;

      int delay = (attempt < RETRY_DELAYS.length) ? RETRY_DELAYS[attempt] : MAX_DELAY;
      System.out.println("Reconnecting in " + delay + "s...");

      try {
        TimeUnit.SECONDS.sleep(delay);
      } catch (InterruptedException e) {
        break;
      }
      attempt++;
    }
  }

  private void connect() throws IOException {
    String[] address = AuthVee.getConfig().get("address").split(":");
    String host = address[0];
    int port = Integer.parseInt(address[1]);

    SSLSocketFactory factory = sslContext.getSocketFactory();
    socket = (SSLSocket) factory.createSocket(host, port);

    socket.setSoTimeout(6000);

    socket.startHandshake();

    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
  }

  private void readLoop() throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      if ("PING".equals(line)) {
        send("PONG");
      } else if ("PONG".equals(line)) {
        // ignore
      } else {
        responseQueue.offer(line);
      }
    }
    throw new IOException("Server closed connection (EOF)");
  }

  private synchronized void send(String msg) {
    if (out != null && !out.checkError()) {
      out.println(msg);
      out.flush();
    }
  }

  private synchronized void close() {
    try {
      if (out != null) out.close();
      if (in != null) in.close();
      if (socket != null && !socket.isClosed()) socket.close();
    } catch (IOException e) {
      // ignore
    }
  }

  public Response sendRequest(Request request) throws InterruptedException {
    send(request.toString());
    String responseStr = responseQueue.poll(5, TimeUnit.SECONDS);

    if (responseStr == null) {
      return new Response(Response.Result.FAILURE, "Timeout or No Connection");
    }
    return Response.fromString(responseStr);
  }

  private void setupSSLContext() throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (FileInputStream fis = new FileInputStream("client.p12")) {
      ks.load(fis, "changeit".toCharArray());
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, "changeit".toCharArray());

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate caCert;
    try (FileInputStream fis = new FileInputStream("ca.crt")) {
      caCert = cf.generateCertificate(fis);
    }
    KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
    ts.load(null, null);
    ts.setCertificateEntry("ca", caCert);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ts);

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
  }
}