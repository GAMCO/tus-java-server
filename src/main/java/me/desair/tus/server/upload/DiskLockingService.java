package me.desair.tus.server.upload;

import me.desair.tus.server.exception.TusException;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.UUID;

/**
 * {@link UploadLockingService} implementation that uses the file system for implementing locking
 *
 * File locking can also apply to shared network drives. This way the framework supports clustering as long as
 * the upload storage directory is mounted as a shared (network) drive.
 *
 * File locks are also automatically released on application (JVM) shutdown. This means the file locking is not
 * persistent and prevents cleanup and stale lock issues.
 */
public class DiskLockingService extends AbstractDiskBasedService implements UploadLockingService {

    private static final String LOCK_SUB_DIRECTORY = "locks";

    private UploadIdFactory idFactory;

    public DiskLockingService(final UploadIdFactory idFactory, final String storagePath) {
        super(storagePath + File.pathSeparator + LOCK_SUB_DIRECTORY);
        Validate.notNull(idFactory, "The IdFactory cannot be null");
        this.idFactory = idFactory;
    }

    @Override
    public UploadLock lockUploadByUri(final String requestURI) throws TusException {

        UUID id = idFactory.readUploadId(requestURI);

        UploadLock lock = null;
        FileChannel fileChannel = null;

        Path lockPath = getLockPath(id);
        //If lockPath is not null, we know this is a valid Upload URI
        if (lockPath != null) {
            lock = new FileBasedLock(requestURI, lockPath);
        }
        return lock;
    }

    @Override
    public void cleanupStaleLocks() throws IOException {
        //TODO
    }

    private Path getLockPath(final UUID id) {
        return getPathInStorageDirectory(id);
    }

}