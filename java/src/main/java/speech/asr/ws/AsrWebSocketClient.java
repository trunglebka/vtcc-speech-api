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
    private String model;
    private AsrWebSocketClient(Builder builder){
        sampleRate = builder.sampleRate;
        audioFormat = builder.audioFormat;
        channels = builder.channels;
        token = builder.token;
        url = builder.url;
        Objects.requireNonNull(builder.wsHandler);
        wsHandler = builder.wsHandler;
        this.model = builder.model;
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
        if (this.model!=null && !this.model.trim().equals(""))
            sb.append("&model=").append(this.model);
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
        private String model;
        private Builder() {
        }

        public Builder setSampleRate(float val) {
            sampleRate = val;
            return this;
        }

        public Builder setModel(String val) {
            model = val;
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

}
