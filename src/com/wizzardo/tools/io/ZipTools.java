package com.wizzardo.tools.io;

import com.wizzardo.tools.WrappedException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    public static class ZipBuilder {
        private List<ZipBuilderEntry> entries = new ArrayList<ZipBuilderEntry>();

        public ZipBuilder append(String name, byte[] bytes) {
            entries.add(new BytesEntry(name, bytes));
            return this;
        }

        public ZipBuilder append(File f) {
            entries.add(new FileEntry(f));
            return this;
        }

        public void zip(OutputStream out) throws IOException {
            ZipOutputStream zipout = new ZipOutputStream(out);
            try {
                for (ZipBuilderEntry entry : entries) {
                    entry.write(zipout);
                }
            } finally {
                IOTools.close(zipout);
            }
        }
    }

    private static interface ZipBuilderEntry {
        public void write(ZipOutputStream out) throws IOException;
    }

    private static class BytesEntry implements ZipBuilderEntry {
        private String name;
        private byte[] bytes;

        private BytesEntry(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            out.putNextEntry(entry);
            out.write(bytes);
        }
    }

    private static class FileEntry implements ZipBuilderEntry {
        private File file;

        private FileEntry(File file) {
            this.file = file;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            if (file.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    ZipEntry entry = new ZipEntry(file.getName());
                    entry.setMethod(ZipEntry.DEFLATED);
                    out.putNextEntry(entry);
                    IOTools.copy(in, out);
                } finally {
                    IOTools.close(in);
                }
            } else {
                zip(out, file);
            }
        }
    }

    public static void unzip(File zipFile, File outDir) {
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] b = new byte[10240];
            int r;
            while ((entry = zip.getNextEntry()) != null) {
                File outFile = new File(outDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                FileOutputStream out = null;
                try {
                    IOTools.copy(zip, out, b);
                } catch (IOException ex) {
                    throw new WrappedException(ex);
                } finally {
                    IOTools.close(out);
                }
            }
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            IOTools.close(zip);
        }
    }

    public static boolean isZip(File f) {
        FileInputStream in = null;

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }

        byte[] b = new byte[2];
        try {
            in.read(b);
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            IOTools.close(in);
        }
        return isZip(b);
    }

    public static boolean isZip(byte[] bytes) {
        return bytes != null && bytes.length >= 2 && bytes[0] == 80 && bytes[1] == 75;
    }

    public static File zip(String file) {
        return zip(new File(file));
    }

    public static File zip(File toZip) {
        ZipOutputStream zipout = null;
        File zip = new File(toZip.getAbsolutePath() + ".zip");
        try {
            zipout = new ZipOutputStream(new FileOutputStream(zip));
            zip(zipout, toZip);
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            IOTools.close(zipout);
        }
        return zip;
    }

    public static void zip(ZipOutputStream out, File toZip) {
        File startDir = toZip.getParentFile();
        zipping(out, FileTools.listRecursive(toZip), startDir);
    }

    public static void zipping(ZipOutputStream zipout, List<File> files, File startDir) {
        FileInputStream in = null;
        byte[] b = new byte[50*1024];
        for (int i = 1; i <= files.size(); i++) {
            try {
                File f = files.get(i - 1);
                in = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getAbsolutePath().substring(startDir.getAbsolutePath().length() + 1));
                entry.setMethod(ZipEntry.DEFLATED);
                zipout.putNextEntry(entry);
                IOTools.copy(in,zipout,b);
            } catch (IOException ex) {
                throw new WrappedException(ex);
            } finally {
                IOTools.close(in);
            }
        }
    }
}
