package sort.external;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;


public class ExternalMergeSortTest {

    public static void main(String[] args) throws Exception {
    	
    	test(new ExternalMergeSort2(), 10);
    	test(new ExternalMergeSort2(), 1000);
    	test(new ExternalMergeSort2(), 10000);
    	test(new ExternalMergeSort2(), 100000);
    	test(new ExternalMergeSort2(), 1000000);

    	test(new ExternalMergeSort(), 10);
    	test(new ExternalMergeSort(), 1000);
    	test(new ExternalMergeSort(), 10000);
    	test(new ExternalMergeSort(), 100000);
    	test(new ExternalMergeSort(), 1000000);
    	
    	
//    	only 2 record in mem
//    	[{10,63},{1000,109},{10000,218},{100000,469},{1000000,2493}]
//    	http://www.wolframalpha.com/input/?i=%5B%7B10%2C63%7D%2C%7B1000%2C109%7D%2C%7B10000%2C218%7D%2C%7B100000%2C469%7D%2C%7B1000000%2C2493%7D%5D
//    	k records in mem
//    	[{10,2},{1000,17},{10000,50},{100000,503},{1000000,4519}]
//    	http://www.wolframalpha.com/input/?i=%5B%7B10%2C2%7D%2C%7B1000%2C17%7D%2C%7B10000%2C50%7D%2C%7B100000%2C503%7D%2C%7B1000000%2C4519%7D%5D
    }

    private static void test(ExternalSort externalSort, int n) throws Exception {
    	System.out.println("start for " + externalSort.getClass().getSimpleName() + " n: " + n);
        String pathDir = System.getProperty("user.home") + "/mergesort";
        String fileName = "source";

        File source = setup(pathDir, fileName, n);

        System.out.println("source created: "+source.length()/1024+" MB");
        long availableMemBytes = source.length()/12;
        System.out.println("availableMem: "+availableMemBytes/1024 + " MB");

        Comparator<String> comp = (a,b) -> {
            String[] thisSplit = a.split("-");
            String[] oSplit = b.split("-");
            Integer thisI = Integer.valueOf(thisSplit[thisSplit.length-1]);
            Integer oI = Integer.valueOf(oSplit[oSplit.length-1]);
            return thisI.compareTo(oI);
        };

        System.out.println(String.format("for %s n: %s tooked: %s", externalSort.getClass().getSimpleName(), n, externalSort.sort(source.getAbsolutePath(), pathDir, availableMemBytes, comp)));
        for (File f : new File(pathDir).listFiles())
        	f.delete();
        System.out.println("test cleaned up");
		
	}

	private static File setup(String pathDir, String fileName, int lines)
            throws IOException {
        File sourceDir = new File(pathDir);
        if(sourceDir.exists())
            for(File f : sourceDir.listFiles())
                f.delete();
        else
            sourceDir.mkdir();


        File source = new File(sourceDir, fileName);
        if(source.exists())
            source.delete();
        source.createNewFile();

        BufferedWriter br = new BufferedWriter(new FileWriter(source)) ;
        for(int i = lines; i>0; i--) {
            br.write("line-"+i);
            br.newLine();
        }
        br.close();
        return source;
    }

}