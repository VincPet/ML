import com.opencsv.CSVWriter;
import org.bitcoinj.core.Block;

import java.io.*;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlocksWriter {
    private static long count = 0;
    private static long currFileCount = 0;
    private static String csvPath = "D:/btc/";
    private final int rowsPerFile = 10000000;
    private final LinkedBlockingQueue<blockRecord> toWrite = new LinkedBlockingQueue<>(15);
    private String lastHashedBlock;
    private String lastFileName;
    private String[] fields;

    BlocksWriter() {
        LinkedList<String> l = new LinkedList<>();
        l.addLast("Day");
        l.addLast("address");
        fields = l.toArray(new String[0]);

        setupProperties();

        this.write();

    }

    private void setupProperties() {
        File file = getPropertiesFile();
        Properties table = new Properties();

        try {
            FileInputStream fi = new FileInputStream(file);
            table.load(fi);
            fi.close();

            if (table.isEmpty())
                return;

            lastHashedBlock = table.getProperty("Hash");
            count = Integer.parseInt(table.getProperty("count"));
            lastFileName = table.getProperty("fileName");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastFileName() {
        return lastFileName;
    }

    private static File getPropertiesFile() {
        File f = new File(csvPath + "last.properties");

        if (f.exists())
            return f;

        try {
            FileWriter fw = new FileWriter(csvPath + "last.properties");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        f = new File(csvPath + "last.properties");
        return f;
    }

    public void addRecord(blockRecord record) {

        try {
            toWrite.put(record);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean stop = false;

    public void end() {
        stop = true;

    }

    private void saveRecord(String record, String fileName) {
        File file = getPropertiesFile();
        Properties table = new Properties();

        table.setProperty("Hash", record);
        table.setProperty("fileName", fileName);
        table.setProperty("count", String.valueOf(count));


        try {
            FileOutputStream fr = new FileOutputStream(file);
            table.store(fr, "Properties");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastSavedRecord() {

        return lastHashedBlock;
    }

    private void write() {
        new Thread(new Runnable() {

            private CSVWriter csvWriter;
            private Writer writer;
            blockRecord record = null;

            private boolean needsNewFile() {
                return currFileCount >= rowsPerFile;
            }

            private void running() throws InterruptedException, IOException {

                if (needsNewFile() && record != null)
                    saveRecord(record.getHash(), record.getFileName());

                record = toWrite.poll(1000, TimeUnit.MILLISECONDS);

                if (record == null)
                    return;

                if (needsNewFile() && csvWriter != null) {
                    csvWriter.close();
                    csvWriter = null;
                }

                if (csvWriter == null) {
                    writer = new BufferedWriter(new FileWriter(csvPath + count + ".csv"));
                    csvWriter = new CSVWriter(writer, '\t');
                    csvWriter.writeNext(fields);
                    currFileCount = 0;
                }

                for (String[] single :
                        record.getRecords()) {

                    csvWriter.writeNext(single);
                    count++;
                    currFileCount++;
                }

            }

            @Override
            public void run() {

                while (!stop || !toWrite.isEmpty()) {
                    try {
                        running();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
