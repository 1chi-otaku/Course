package itstep.learning.services.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.filename.FileNameService;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
public class LocalStorageService implements StorageService {
    private final static String storagePath = "I:/storage/Java213";
    private final static int bufferSize = 4096;

    private final FileNameService fileNameService;

    @Inject
    public LocalStorageService(FileNameService fileNameService) {
        this.fileNameService = fileNameService;
    }

    @Override
    public File getFile(String fileName) {
        if( fileName == null ) {
            return null;
        }
            File file = new File( storagePath, fileName );
            if( ! file.exists() ) {
                return null;
            }
                return file;
    }

    @Override
    public String saveFile(FileItem fileItem) throws IOException {
        if (fileItem == null) {
            throw new IOException("FileItem is null");


        }
        if (fileItem.getSize() == 0) {
            throw new IOException("FileItem is empty");
        }

        String fileName = fileItem.getName();
        if (fileName == null) {
            throw new IOException( "FileItem has no name" );

        }
        // виділяємо розширення з початкового імені файлу
        int dotIndex = fileName.lastIndexOf( '.' );
        if( dotIndex == -1 ) {
            throw new IOException("FileItem has no extension");
        }
            String extension = fileName. substring( dotIndex );
        if(".".equals(extension)) {
            throw new IOException("FileItem has empty extension");
        }
        // генеруємо нове ім'я файлу, перевіряємо що такого немає у сховищі
        String savedName;
        File file;
        do {
            savedName = fileNameService.generateRandomFileName() + extension;
            file = new File( storagePath, savedName );
        } while( file.exists() );

        long size = fileItem.getSize();
        if( size > bufferSize ) {
            size = bufferSize;
        }
        byte[] buffer = new byte[(int)size];
        int len;
        try(FileOutputStream fos = new FileOutputStream( file );
            InputStream in = fileItem.getInputStream()
        ) {
            while( ( len = in.read( buffer ) ) > 0 ) {
                fos.write( buffer, 0, len );
            }
        }
        return savedName;
    }
}