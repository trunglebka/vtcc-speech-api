package speech.asr.rest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AudioFileRecognizer {
    private static final String FIELD_NAME = "file";
    private Map<String, String> headers;
    private Map<String, String> uriParams;
    private File fileToRecognize;
    private static final String API_URL = "https://vtcc.ai/voice/api/asr/v1/rest/decode_file";
    public AudioFileRecognizer(File fileToRecognize, Map<String, String> headers, Map<String, String> uriParams){
        this.fileToRecognize = fileToRecognize;
        this.headers = headers;
        this.uriParams = uriParams;
    }
    public AudioFileRecognizer(File fileToRecognize){
        this.fileToRecognize = fileToRecognize;
    }
    public AudioFileRecognizer(File fileToRecognize, String token){
        this.fileToRecognize = fileToRecognize;
        if (token == null)
            return;
        headers = new HashMap<String, String>(){{put("token", token);}};
    }
    public void addToken(String token){
        if (token == null)
            return;
        if (headers == null){
            headers = new HashMap<>();
        }
        headers.put("token", token);
    }
    public String recognize() throws Exception{
        return HttpMultipartUploader.uploadFile(this.API_URL, FIELD_NAME, this.fileToRecognize, headers, uriParams);
    }

    public static void main(String[] args) throws Exception{
        AudioFileRecognizer audioFileRecognizer = new AudioFileRecognizer(new File("/home/trungle/Downloads/sound.wav"));
        //add your token here
        //audioFileRecognizer.addToken("");
        System.out.println(audioFileRecognizer.recognize());
    }
}
