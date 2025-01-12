package itstep.learning.services.storage;

import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    File getFile(String fileName);
    String saveFile(FileItem fileItem) throws IOException;
}
