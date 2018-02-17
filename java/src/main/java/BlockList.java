import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class BlockList extends LinkedList<File> {

    private String localPath;
    private BlockList itself;
    Queue<File> lasts = new PriorityQueue<>();

    public BlockList(String localPath) {
        this.localPath = localPath;
        itself = this;
    }

    public String getLastPath() {
        return lasts.element().getName();
    }

    @Override
    public Iterator<File> iterator() {
        return new Iterator<File>() {
            @Override
            public boolean hasNext() {
                return !itself.isEmpty();
            }

            File prev = null;

            @Override
            public File next() {
                File tmp = itself.poll();

                lasts.add(tmp);

                if (lasts.size() > 3)
                    lasts.poll();

                try {
                    Files.copy(tmp.toPath(), new File(localPath + tmp.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final File local = new File(localPath + tmp.getName());


                if (prev != null) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Path thlocal = local.toPath();
                                try {
                                    Thread.sleep(60000);
                                    Files.delete(thlocal);
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                        }
                    }).start();

                }
                prev = local;
                return local;
            }

            @Override
            public void remove() {
                itself.remove();
            }
        };
    }
}
