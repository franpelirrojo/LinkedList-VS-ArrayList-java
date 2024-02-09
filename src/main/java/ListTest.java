import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ListTest {
	private final static File FILE = new File("./listacorreos.txt");
	private final static String PREFIX = String.valueOf((char)((Math.random() * (122 - 97)) + 97));
	private final static int TEST_LENGTH = 10_000_000;

	public ListTest(){
		System.out.println("TEST bigO");
		System.out.println("El prefijo aleatorio es: " + PREFIX);
		System.out.println("El numero de elementos es: " + TEST_LENGTH);

		testArray();
		testLink();
	}

	public void generateSet(){
		ArrayList<String> set = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		for (int k = 0; k < TEST_LENGTH; k++){
			char c;
			int i = (int)((Math.random() * (30 -6)) + 6);
			for (int j = 0; j <= i; j++){
				c = (char)((Math.random() * (122 - 97)) + 97);
				sb.append(c);
			}
			sb.append("@gmail.com").append('\n');
			set.add(sb.toString());
			sb.delete(0, sb.length());
		}

		try {
			AsynchronousFileChannel afc = AsynchronousFileChannel.open(FILE.toPath()
					, StandardOpenOption.WRITE
					,StandardOpenOption.CREATE
					,StandardOpenOption.TRUNCATE_EXISTING);

			int bufferSize = 0;
			for (String s : set){
				bufferSize += s.getBytes().length;
			}
			byte[] buffer = new byte[bufferSize];

			int l = 0;
			for (int i = 0; i < set.size() && l < bufferSize; i++){
				String s = set.get(i);
				for (byte b : s.getBytes()){
					buffer[l] = b;
					l++;
				}
			}

			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
			Future<Integer> result = afc.write(byteBuffer, 0);
			while (!result.isDone()){
				Thread.sleep(1000);
			}

			int writtenBytes = result.get();
			System.out.format("%s bytes written to %s%n", writtenBytes,
					FILE.toPath());
		} catch (IOException | InterruptedException | ExecutionException e) { throw new RuntimeException(e);
		}
	}

	public void loadData(List<String> list){
		try {
			BufferedReader br = new BufferedReader(new FileReader(FILE));
			while (br.ready()){
				list.add(br.readLine());
			}
		} catch (IOException e) {
			System.out.println("No se pudo leer corectamente los datos del documento " + FILE);
		}
	}

	public void testArray(){
		generateSet();
		ArrayList<String> array = new ArrayList<>();
		loadData(array);

		long nanostart = System.nanoTime();
		for(int i = 0; i < array.size(); i++){
			if (array.get(i).startsWith(PREFIX)){
				array.remove(i);
			}
		}
		long nanoend = System.nanoTime();
		long nanototal = nanoend - nanostart;
		System.out.println("ArrayList: " + (double) nanototal/1_000_000_000 + " segundos");
	}

	public void testLink(){
		generateSet();
		LinkedList<String> linkedList = new LinkedList<>();
		loadData(linkedList);

		long nanostart = System.nanoTime();
		ListIterator<String> pointer = linkedList.listIterator(); //Es importante usar Iterator para tener control sobre el puntero
		while (pointer.hasNext()){
			if (pointer.next().startsWith(PREFIX)) pointer.remove();
		}

		long nanoend = System.nanoTime();
		long nanototal = nanoend - nanostart;
		System.out.println("LinkedList: " + (double) nanototal/1_000_000_000 + " segundos");
	}

	public static void main(String[] args) {
		new ListTest();
	}
}
