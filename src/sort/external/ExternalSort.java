package sort.external;

import java.io.IOException;
import java.util.Comparator;

public interface ExternalSort {

	public <T> long sort(String inPath, String outPath, long availableMemBytes, Comparator<T> comp) throws IOException;
	
//	void cleanUp(String outPath);
	
}
