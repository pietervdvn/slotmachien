import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

// Connect streams!
public class Pipe implements Runnable {
    private InputStream in;
    private OutputStream out;

    public Pipe(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run(){
        try (Scanner scanner = new Scanner(new InputStreamReader(in));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            while (scanner.hasNextLine()) {
                writer.write(scanner.nextLine());
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Thread make(InputStream in, OutputStream out){
        Thread t = new Thread(new Pipe(in, out));
        t.start();
        return t;
    }
}
