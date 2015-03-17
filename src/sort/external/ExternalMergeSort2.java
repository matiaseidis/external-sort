package sort.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class ExternalMergeSort2 implements ExternalSort {

	@Override
	public <T> long sort(String inPath, String outPath, long availableMemBytes, Comparator<T> comp) throws IOException {

		long ini = System.currentTimeMillis();
		
		int totalChunks = splitSortedChunks(inPath, outPath, availableMemBytes, comp);
		
		int stepChunks = 0;
		int stepCount = 0;
		int remaining = totalChunks;
		
		while(remaining > 1) {
			
			int totalModulo = remaining % 2;
			int evenTotal = remaining - (totalModulo != 0 ? 1 : 0);
			
			for (int i = 0; i < evenTotal; i += 2, stepChunks++) 
				merge(outPath, stepCount, stepChunks, i, comp);
			
			if (totalModulo != 0) 
				new File(fileName(outPath, stepCount, totalChunks-1)).renameTo(new File(fileName(outPath, stepCount+1, stepChunks)));
			
			System.out.println(String.format("done step %s - result files: %s", stepCount, stepChunks));
			stepCount++;
			remaining = stepChunks;
			stepChunks = 0;
		}
		
		System.out.println("finished: resulting file: " + fileName(outPath, stepCount, 0));
		long tooked = System.currentTimeMillis() - ini;
		cleanUp(outPath, stepCount);
		return tooked;
	}

	private <T> void merge(String outPath, int stepCount, int stepChunk, int i, Comparator<T> comp) throws IOException {
		try (BufferedWriter resultWriter = writer(outPath, stepCount+1, stepChunk++)) {
			
			// por cada dos cachos hago uno nuevo
			BufferedReader reader0 = reader(outPath, stepCount, i);
			BufferedReader reader1 = reader(outPath, stepCount, i+1);

			PriorityQueue<ReaderElem<T>> heap = new PriorityQueue<ReaderElem<T>>((a, b) -> comp.compare(a.value, b.value));
			heap.add(new ReaderElem<T>(0, (T) reader0.readLine()));
			heap.add(new ReaderElem<T>(1, (T) reader1.readLine()));

			while (!heap.isEmpty()) {

				ReaderElem<T> polled = heap.poll();
				resultWriter.write(polled.value.toString());
				resultWriter.newLine();

				BufferedReader reader = polled.idx == 0 ? reader0 : reader1;
				String nextReadersLine = reader.readLine();
				if (nextReadersLine != null)
					heap.add(new ReaderElem(polled.idx, nextReadersLine));
			}
		}
	}

	private void cleanUp(String outPath, int i) {

		for(File toDel : new File(outPath).listFiles((dir, name) -> !name.endsWith(suffix(i, 0)))) 
			toDel.delete();

		System.out.println("clean up completed");
	}

	private BufferedWriter writer(String outPath, int step, int i) throws IOException {
		return new BufferedWriter(new FileWriter(new File(fileName(outPath, step, i))));
	}

	private BufferedReader reader(String inPath, int step, int i) throws IOException {
		return new BufferedReader(new FileReader(new File(fileName(inPath, step, i))));
	}

	private String fileName(String inPath, int step, int i) {
		return String.format("%s/%s", inPath, suffix(step, i));
	}

	private String suffix(int step, int i) {
		return String.format("%s-%s", step, i);
	}

	private <T> int splitSortedChunks(String inPath, String outPath, long availableMemBytes, Comparator<T> comp) {

		int totalChunks = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(new File(inPath)))) {

			List<T> currentChunk = new LinkedList<T>();
			int currentChunkBytes = 0;

			String line = br.readLine();
			while (line != null) {

				currentChunk.add((T) line);
				currentChunkBytes += (line.length() * 4);

				if (currentChunkBytes >= availableMemBytes) {
					sortAndWrite(currentChunk, outPath, totalChunks++, comp);
					currentChunkBytes = 0;
				}

				line = br.readLine();
			}
			sortAndWrite(currentChunk, outPath, totalChunks++, comp);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("first pass done. " + totalChunks + " created");
		return totalChunks;
	}

	private <T> void sortAndWrite(List<T> currentChunk, String path, int i, Comparator<T> comp) {

		if (!currentChunk.isEmpty())
			Collections.sort(currentChunk, comp);

		try (BufferedWriter fileWriter = writer(path, 0, i)) {

			for (T chunkLine : currentChunk) {
				fileWriter.write(chunkLine.toString());
				fileWriter.newLine();
			}

			currentChunk.clear();

		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
	}

}