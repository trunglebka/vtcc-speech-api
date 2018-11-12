package speech.asr.ws;

public interface IResponseHandler<T> {
    void onMessage(T msg);

    void onFailure(Throwable cause);

    void onComplete();
}
