package speech.asr.ws;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import speech.asr.utils.PCMFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AsrWebSocketClient {
    private final String url;
    private float sampleRate;
    private PCMFormat audioFormat;
    private int channels;
    private String token;
    private IResponseHandler wsHandler;

    private AsrWebSocketClient(Builder builder){
        sampleRate = builder.sampleRate;
        audioFormat = builder.audioFormat;
        channels = builder.channels;
        token = builder.token;
        url = builder.url;
        Objects.requireNonNull(builder.wsHandler);
        wsHandler = builder.wsHandler;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private String generateQueryStringUri() {
        StringBuilder sb = new StringBuilder(this.url);
        sb.append("?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)")
                .append((int) sampleRate).append(",+format=(string)").append(this.audioFormat)
                .append(",+channels=(int)").append(channels);
        if (this.token != null)
            sb.append("&token=").append(this.token);
        return sb.toString();
    }

    public void recognize(InputStream in) throws Exception {
        try (
                WebSocketClient ws = new WebSocketClient(generateQueryStringUri());
        ) {
            ws.addHandler(wsHandler);
            int nRead;
            byte[] bytes = new byte[(int) sampleRate * audioFormat.getSampleSize() / 32];
            while ((nRead = in.read(bytes)) != -1) {
                ws.sendBinaryMessage(bytes, 0, nRead);
                TimeUnit.MILLISECONDS.sleep(250);
            }
            ws.sendBinaryMessage("EOS".getBytes(CharsetUtil.UTF_8));
        }
    }

    public void recognize(File file) throws Exception {
        try (BufferedInputStream bi = new BufferedInputStream(new FileInputStream(file))) {
            recognize(bi);
        }
    }

    public static final class Builder {
        private float sampleRate;
        private PCMFormat audioFormat;
        private int channels;
        private String token;
        private String url = "wss://vtcc.ai/voice/api/asr/v1/ws/decode_online";
        private IResponseHandler wsHandler;

        private Builder() {
        }

        public Builder setSampleRate(float val) {
            sampleRate = val;
            return this;
        }

        public Builder setAudioFormat(PCMFormat val) {
            audioFormat = val;
            return this;
        }

        public Builder setChannels(int val) {
            channels = val;
            return this;
        }

        public Builder setToken(String val) {
            token = val;
            return this;
        }

        public Builder setUrl(String val) {
            url = val;
            return this;
        }

        public Builder setHandler(IResponseHandler val) {
            this.wsHandler = val;
            return this;
        }

        public AsrWebSocketClient build() throws Exception {
            return new AsrWebSocketClient(this);
        }
    }

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
            AsrWebSocketClient client = AsrWebSocketClient.newBuilder().setSampleRate(16000)
                    .setAudioFormat(PCMFormat.S16LE).setChannels(1).setHandler(handler).build();
            client.recognize(bi);
        }
    }

}
