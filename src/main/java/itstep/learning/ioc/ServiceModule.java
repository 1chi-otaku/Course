package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import itstep.learning.kdf.KdfService;
import itstep.learning.kdf.PbKdf1Service;
import itstep.learning.services.db.DbService;
import itstep.learning.services.db.MySqlDbService;
import itstep.learning.services.filename.FileNameService;
import itstep.learning.services.filename.RandomLength;
import itstep.learning.services.form.FormParseService;
import itstep.learning.services.form.MixedFormParseService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.storage.LocalStorageService;
import itstep.learning.services.storage.StorageService;
import itstep.learning.services.stream.StreamService;
import itstep.learning.services.stream.StreamServiceImpl1;

public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HashService.class).to(Md5HashService.class);
        bind(KdfService.class).to(PbKdf1Service.class);
        bind(FormParseService.class).to(MixedFormParseService.class);
        bind(RandomLength.class).to(FileNameService.class);
        bind(StorageService.class).to(LocalStorageService.class);
        bind(DbService.class).to(MySqlDbService.class);
        bind(StreamService.class).to(StreamServiceImpl1.class);
        //bind(DbService.class).annotatedWith(Names.named("Oracle")).to(OracleDbService.class);
    }
}
