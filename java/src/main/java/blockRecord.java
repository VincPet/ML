import org.bitcoinj.core.*;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

public class blockRecord {
    private Block block;
    private NetworkParameters np;
    private LinkedList<String[]> fields;
    private String day;
    private String fileName;

    blockRecord(Block block, NetworkParameters np, String fileName) {
        this.block = block;
        this.np = np;
        this.fileName = fileName;
        fields = new LinkedList<>();
        day = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(block.getTime());
        startElab();
    }

    public String getFileName() {
        return fileName;
    }

    public String getHash() {
        return block.getHashAsString();
    }

    public String getDate() {
        return day;
    }

    public LinkedList<String[]> getRecords() {
        return fields;
    }

    private void startElab() {

        for (Transaction tx : block.getTransactions()) {
            elabTrasactions(tx);
        }

    }

    private void elabTrasactions(Transaction tx) {
        for (TransactionOutput out : tx.getOutputs())
            elabTransOut(out);
    }

    private void elabTransOut(TransactionOutput out) {

        Address tmp;

        try {
            tmp = out.getAddressFromP2PKHScript(np);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (tmp == null)
            tmp = out.getAddressFromP2SH(np);

        if (tmp == null)
            return;

        LinkedList<String> record = new LinkedList<>();
        record.addLast(day);
        record.addLast(tmp.toBase58());
        fields.add(record.toArray(new String[2]));
    }


}
