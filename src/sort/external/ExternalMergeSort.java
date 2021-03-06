package sort.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class ExternalMergeSort implements ExternalSort {

	public <T> long sort(String inPath, String resultDir, long availableMemBytes, Comparator<T> comp) throws IOException {

		long ini = System.currentTimeMillis();
		
		String resultPath = inPath + "result";
		System.out.println("rPath: " + resultPath);
		System.out.println("in: " + inPath + "-" + 0);
		int totalChunks = splitSortedChunks(inPath, availableMemBytes, comp);
		
		

		try (BufferedWriter resultWriter = new BufferedWriter(new FileWriter(new File(resultPath)))) {
		
			BufferedReader[] readers = new BufferedReader[totalChunks];
			
				for (int i = 0; i < totalChunks; i++) 
					readers[i] = new BufferedReader(new FileReader(new File(inPath + "-" + i)));

				PriorityQueue<ReaderElem<T>> heap = new PriorityQueue<ReaderElem<T>>((a,b)-> comp.compare(a.value, b.value));
				
				for (int i = 0; i < readers.length; i++) {
					String line = readers[i].readLine();
					if(line != null)
						heap.add(new ReaderElem(i, line));
				}
				
				while (!heap.isEmpty()) {
				
					ReaderElem<T> polled = heap.poll();
					resultWriter.write(polled.value.toString());
					resultWriter.newLine();

					String nextReadersLine = readers[polled.idx].readLine();
					if (nextReadersLine != null) 
						heap.add(new ReaderElem(polled.idx, nextReadersLine));
				}
				
				System.out.println("done - result file: " + resultPath);

		} 
		long tooked = System.currentTimeMillis() - ini;
		return tooked;
	}

	private <T> int splitSortedChunks(String toWritePath, long availableMemBytes, Comparator<T> comp) {

		int totalChunks = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(new File(toWritePath)))) {

			List<T> currentChunk = new LinkedList<T>();
			int currentChunkBytes = 0;

			String line = br.readLine();
			while (line != null) {

				currentChunk.add((T)line);
				currentChunkBytes += (line.length() * 4);

				if (currentChunkBytes >= availableMemBytes) {
					sortAndWrite(currentChunk, toWritePath, totalChunks++, comp);
					currentChunkBytes = 0;
				}

				line = br.readLine();
			}
			sortAndWrite(currentChunk, toWritePath, totalChunks++, comp);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("first pass done. "+totalChunks+" created");
		return totalChunks;
	}

	private <T> void sortAndWrite(List<T> currentChunk, String path, int identifier, Comparator<T> comp) {

		if (!currentChunk.isEmpty())
			Collections.sort(currentChunk, comp);
		
		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(path + "-"+ identifier)))){

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