package speech.asr.ws;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import speech.asr.utils.PCMFormat;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AsrWebSocketSample {

    public static void main(String[] args) throws Exception {
        IResponseHandler<WebSocketFrame> handler = new IResponseHandler<WebSocketFrame>() {
            @Override
            public void onMessage(WebSocketFrame frame) {
                if (frame instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                    System.out.println(textFrame.text());
                } else
                    System.out.println(frame);
            }

            @Override
            public void onFailure(Throwable cause) {
                cause.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.err.println("completed");
            }
        };
        try (
                BufferedInputStream bi = new BufferedInputStream(Files.newInputStream(Paths.get("src/main/resources/test_audio.wav")))
        ) {
            AsrWebSocketClient client = AsrWebSocketClient.newBuilder()
                    .setSampleRate(16000)
                    .setAudioFormat(PCMFormat.S16LE)
                    .setChannels(1)
                    .setHandler(handler)
                    .build();
            client.recognize(bi);
        }
    }
}
