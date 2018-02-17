import java.io.File;
import java.util.*;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;


public class SimpleDailyTxCount {


    // Location of block files. This is where your blocks are located.
    // Check the documentation of Bitcoin Core if you are using
    // it, or use any other directory with blk*dat files.
    private static String PREFIX = "Z:/btc/blocks/";
    private static String tmpFolder = "C:/Users/Universita/Desktop/blk/";
    private NetworkParameters np;
    private BlocksWriter csv;
    private Integer blockCounter = 0;

    // A simple method with everything in it
    private void startElabs() {

        // Just some initial setup
        np = new MainNetParams();
        Context.getOrCreate(MainNetParams.get());

        // We create a BlockFileLoader object by passing a list of files.
        // The list of files is built with the method buildList(), see
        // below for its definition.
        csv = new BlocksWriter();
        BlockList bl = buildList();

        BlockFileLoader loader = new BlockFileLoader(np, bl);

        // A simple counter to have an idea of the progress
        String lastBlok = csv.getLastSavedRecord();

        int n = 0;
        if (lastBlok != null) {
            while (true) {
                Block check = loader.next();

                if (check.getHashAsString().equals(lastBlok))
                    break;
                n++;

            }
        }
        System.out.println(n);


        for (Block block : loader) {
            csv.addRecord(new blockRecord(block, np, bl.getLastPath()));
        } // End of iteration over blocks

        csv.end();
    }

    // The method returns a list of files in a directory according to a certain
    // pattern (block files have name blkNNNNN.dat)
    private BlockList buildList() {
        BlockList list = new BlockList(tmpFolder);
        for (int i = 0; true; i++) {
            File file = new File(PREFIX + String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists())
                break;
            list.add(file);
        }
        System.out.println(csv.getLastFileName());
        if (csv.getLastFileName() != null && !csv.getLastFileName().equals("")) {
            while (!list.getFirst().getName().equals(csv.getLastFileName()))
                list.removeFirst();
        }


        return list;
    }


    // Main method: simply invoke everything
    public static void main(String[] args) {
        SimpleDailyTxCount tb = new SimpleDailyTxCount();
        tb.startElabs();
    }

}