import java.io.*;
import java.util.*;


public class VerifyResult {
	static final String resultFilename = "result5.txt"; // change to your result file name
	public static void main(String[] args) throws Exception {
		Map<Integer, Integer> pidCnt = new HashMap<Integer, Integer>();
		Set<Integer> wids = new HashSet<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(resultFilename)); 
		String line = br.readLine();
		
		while ((line = br.readLine()) != null) {
			StringTokenizer token = new StringTokenizer(line);
			int pid = Integer.parseInt(token.nextToken());
			int wid = Integer.parseInt(token.nextToken());
			
			if(wids.contains(wid)) {
				System.err.println("Invalid WID: " + wid);
			}
			
			wids.add(wid);
			if (pidCnt.containsKey(pid)) {
				int occurences = pidCnt.get(pid) + 1;
				if(occurences > 3)
					System.err.println("Invalid pid: " + pid + " occur #: " + occurences);
				pidCnt.put(pid, occurences);
				
			} else
				pidCnt.put(pid, 1);
		}
		
		System.out.println("PID Cnt: " + pidCnt.size());
		System.out.println("WID Cnt: " + wids.size());
	}
}
