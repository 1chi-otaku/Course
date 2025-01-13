package itstep.learning.services.stream;

import com.google.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Singleton
public class StreamServiceImpl1 implements StreamService {
    @Override
    public String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        try {return byteArrayOutputStream.toString("UTF-8");}
        finally { byteArrayOutputStream.close();}
    }

    @Override
    public void pipe(InputStream inputStream, OutputStream outputStream) throws IOException {
        throw new IOException("Not implemented yet.");
    }

    @Override
    public void pipe(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {

    }
}
