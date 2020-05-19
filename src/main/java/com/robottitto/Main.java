package com.robottitto;

import com.robottitto.model.Archive;
import com.robottitto.model.Config;
import com.robottitto.model.Directory;
import com.robottitto.util.JsonUtils;
import com.robottitto.util.NewFileChecker;
import com.robottitto.util.NewFileListener;
import com.robottitto.util.PostgreSQLUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.robottitto.util.PostgreSQLUtils.*;

public class Main {

    private static final String CONFIG_JSON = "config.json";
    public static File appDirectory;
    public static String root;

    public static void main(String[] args) throws IOException, SQLException {
        Config config = JsonUtils.readConfig(CONFIG_JSON);
        PostgreSQLUtils.connect(config.getDbConnection());
        appDirectory = new File(config.getApp().getDirectory());
        root = appDirectory.getPath();
        syncAppDirectoryToDb(appDirectory, root);
        syncDbToAppDirectory(appDirectory, root);
        NewFileListener newFileListener = new NewFileListener(PostgreSQLUtils.getConnection());
        newFileListener.start();
        NewFileChecker newFileChecker = new NewFileChecker(PostgreSQLUtils.getConnection());
        newFileChecker.start();
    }

    public static void syncAppDirectoryToDb(File resource, String root) throws SQLException, IOException {
        String resourceName = resource.getPath().replace(root, ".");
        if (resource.isFile()) {
            String directoryName = resource.getParent().replace(root, ".");
            int directoryId = getDirectoryId(directoryName);
            String fileName = resource.getName();
            if (!isArchiveInserted(fileName, directoryId, resource)) {
                PostgreSQLUtils.insertArchive(fileName, directoryId, resource);
                System.out.println("Engadiuse o arquivo " + fileName);
            }
        }
        if (resource.isDirectory()) {
            if (!isDirectoryInserted(resourceName)) {
                PostgreSQLUtils.insertDirectory(resourceName);
                System.out.println("Engadiuse o directorio " + resourceName);
            }
            for (File childFile : resource.listFiles()) {
                syncAppDirectoryToDb(childFile, root);
            }
        }
    }

    public static void syncDbToAppDirectory(File resource, String root) throws SQLException, IOException {
        ArrayList<Directory> directories = PostgreSQLUtils.getDirectories();
        for (Directory directory : directories) {
            if (isDirectoryDeleted(resource, root, directory, true)) {
                rebuildDirectory(directory, root);
            }
        }

        ArrayList<Archive> archives = PostgreSQLUtils.getArchives();
        for (Archive archive : archives) {
            if (isArchiveDeleted(resource, root, archive, true)) {
                archive.setBinaryFile(getArchiveBinaryFile(archive.getId()));
                rebuildArchive(archive, root);
            }
        }
    }

    public static boolean isDirectoryDeleted(File resource, String root, Directory directory, boolean deleted) {
        boolean isDeleted = deleted;
        if (resource.isDirectory()) {
            String resourceName = resource.getPath().replace(root, ".");
            if (resourceName.equals(directory.getName())) {
                isDeleted = false;
            }
            for (File resourceChild : resource.listFiles()) {
                isDirectoryDeleted(resourceChild, root, directory, isDeleted);
            }
        }
        return isDeleted;
    }

    public static boolean isArchiveDeleted(File resource, String root, Archive archive, boolean deleted) throws SQLException {
        boolean isDeleted = deleted;
        if (resource.isFile()) {
            String resourceName = resource.getName();
            if (resourceName.equals(archive.getName())) {
                String resourceParentDirectory = resource.getParent().replace(root, ".");
                String directoryName = getDirectoryName(archive.getDirectoryId());
                if (resourceParentDirectory.equals(directoryName)) {
                    isDeleted = false;
                }
            }
        }
        if (resource.isDirectory()) {
            for (File resourceChild : resource.listFiles()) {
                isArchiveDeleted(resourceChild, root, archive, isDeleted);
            }
        }
        return isDeleted;
    }

    public static void rebuildDirectory(Directory directory, String root) {
        File newDirectory = new File(root + File.separator + directory.getName());
        newDirectory.mkdirs();
    }

    public static void rebuildArchive(Archive archive, String root) throws SQLException, IOException {
        String pathname = root + getDirectoryName(archive.getDirectoryId()).substring(1) + File.separator + archive.getName();
        File file = new File(pathname);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        if (archive.getBinaryFile() != null) {
            fileOutputStream.write(archive.getBinaryFile());
        }
        fileOutputStream.close();
    }

}
