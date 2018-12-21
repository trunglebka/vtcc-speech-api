package speech.asr.rest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AudioFileRecognizer {
    private static final String FIELD_NAME = "file";
    private static final String API_URL = "https://vtcc.ai/voice/api/asr/v1/rest/decode_file";
    private Map<String, String> headers;
    private Map<String, String> uriParams;
    private File fileToRecognize;

    public AudioFileRecognizer(File fileToRecognize, Map<String, String> headers, Map<String, String> uriParams) {
        this.fileToRecognize = fileToRecognize;
        this.headers = headers;
        this.uriParams = uriParams;
    }

    public AudioFileRecognizer(File fileToRecognize) {
        this.fileToRecognize = fileToRecognize;
    }

    public AudioFileRecognizer(File fileToRecognize, String token) {
        this.fileToRecognize = fileToRecognize;
        if (token == null)
            return;
        headers = new HashMap<String, String>() {{
            put("token", token);
        }};
    }


    public void setToken(String token) {
        if (token == null)
            return;
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("token", token);
    }

    public String recognize() throws Exception {
        return HttpMultipartUploader.uploadFile(API_URL, FIELD_NAME, this.fileToRecognize, uriParams, headers);
    }


    public static void main(String[] args) throws Exception {
        AudioFileRecognizer audioFileRecognizer = new AudioFileRecognizer(new File("src/main/resources/test_audio.wav"));
        //add your token here
        //audioFileRecognizer.setToken("");
        System.out.println(audioFileRecognizer.recognize());
    }
}
