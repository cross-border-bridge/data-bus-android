package jp.co.dwango.cbb.db;

public interface DataBusErrorListener {
    void onOutOfMemoryError(OutOfMemoryError error);
}
