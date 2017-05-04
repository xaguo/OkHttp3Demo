package com.xa.okhttpdemo;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by XA on 3/23/2017.
 *
 */

public class CountingRequestBody extends RequestBody {

    protected RequestBody delegate;
    private Listener listener;
    private CountingSink countingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWriten;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWriten += byteCount;
            listener.onRequestProgress(byteCount, contentLength());
        }
    }

    public static interface Listener {
        void onRequestProgress(long byteWrited, long contentLength);
    }

}
