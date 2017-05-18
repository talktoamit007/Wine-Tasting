/**
 * Wine tasing challenge
 * Author: Nam Dang
 */
import java.io.*;
import java.util.*;
import com.almworks.sqlite4java.*;


public class WineTaste {
	static final String problemFilename = "person_wine_4.txt"; /* Path to problem file */
	static final String resultFilename = "result4.txt"; /* Path to result file */
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(problemFilename));
		SQLiteConnection db = new SQLiteConnection(); // in-memory database. For speed
		db.open(true);
		db.exec("CREATE TABLE WishList"
				+ "("
				+ "pid	INT	not null,"
				+ "wid	INT not null"
				+ ");");
		db.exec("CREATE TABLE PersonsRank"
				+ "("
				+ "pid	INT primary key not null,"
				+ "rank			REAL"
				+ ");");
		db.exec("CREATE TABLE WinesRank("
				+ "wid			INT PRIMARY KEY NOT NULL,"
				+ "rank			REAL NOT NULL"
				+ ");");  
		
		db.exec("INSERT INTO WishList(pid, wid) SELECT 110 AS 'pid', 3434 AS 'WID' ");
		
		System.out.println("Done setting up an in-memory database. Loading the data...");
		
		/* Reading the input */
		String line;
		SQLParts sqlParts = new SQLParts("INSERT INTO WishList(pid, wid)");
		StringBuilder cmdBuilder = new StringBuilder("INSERT INTO WishList(pid, wid)");
		int cnt = 0;
		int tracker = 0;
		int maxWid = -1;
		db.exec("BEGIN"); // faster speed (maybe not since this is an in-memory database?)
		while ((line = br.readLine()) != null) {
			StringTokenizer token = new StringTokenizer(line);
			String personID = token.nextToken().replaceFirst("person", "");
			String wineID = token.nextToken().replaceFirst("wine", "");
			int pid = Integer.parseInt(personID);
			int wid = Integer.parseInt(wineID);
			
			if (wid < maxWid)
				wid = maxWid;
			
			if (cnt == 0) {
				cmdBuilder.append(" SELECT " + pid + " AS 'pid', " + wid + " AS 'wid' ");
			}
			else {
				cmdBuilder.append(" UNION SELECT " + pid + "," + wid + " ");
			}
			
			cnt++;
			
			if (cnt == 500) {
				db.exec(cmdBuilder.toString());
				sqlParts.clear();
				cmdBuilder.setLength(0);
				cmdBuilder.append("INSERT INTO WishList(pid, wid)");
				cnt = 0;
				
				// just for visually tracking the progress
				if (tracker % 1000 == 0) System.out.print("."); 
				if (++tracker % 20000 == 0) System.out.println();
			}
		}
		System.out.println();
		br.close(); 
		if (cnt > 0) {  // the remaning data
			db.exec(cmdBuilder.toString());
			cmdBuilder.setLength(0);
		}
		db.exec("COMMIT"); // now we can safely comit
		
		System.out.println("Done reading the file. Creating indices for searching...");
		db.exec("CREATE INDEX WishListPidx ON WishList(pid)");
		db.exec("CREATE INDEX WishListWidx ON WishList(wid)"); 
		
		// Now calculate the ranking for the bottles		
		System.out.println("Done creating indices. Ranking the bottles...");
		db.exec("INSERT INTO WinesRank(wid, rank)"
				+ "SELECT wid AS 'wid', 10.0/COUNT(pid) as 'rank' FROM WishList GROUP BY wid;");
		
		System.out.println("Ranking the buyers...");
		db.exec("INSERT INTO PersonsRank(pid, rank) "
				+ " SELECT WishList.pid as pid, SUM(WinesRank.rank) AS rank FROM WishList, WinesRank"
				+ " WHERE (WishList.wid = WinesRank.wid) GROUP BY WishList.pid");
		System.out.println("DONE PREPARING! \n\nNow assign the bottles to buyers..."); 
		
		/** Now the main job */
		SQLiteStatement st = db.prepare("SELECT PersonsRank.pid, WinesRank.wid FROM PersonsRank, WishList, WinesRank "
				+ " WHERE PersonsRank.pid = WishList.pid AND WishList.wid = WinesRank.wid "
				+ " ORDER BY PersonsRank.rank DESC, PersonsRank.pid ASC, WinesRank.rank DESC");
			
		Writer writer = new BufferedWriter(new FileWriter(new File(resultFilename)));
		writer.write("                                                       \n"); // reserved for the result
		
		BitSet wineTracker = new BitSet(maxWid + 1); // for tracking the bottles
		int bottleCnt = 0;				// Total bottle count
		int curPid = -1;				// Currently active Person ID
		int curCnt = 0;					// Number of bottles currently being sold
		while(st.step()) {
			int pid = st.columnInt(0); // seller id
			if (curPid == pid && curCnt == 3)
				continue; // skip if the person already receives three bottles
			
			if (curPid != pid) {
				curPid = pid;
				curCnt = 0; // reset lastCnt
			}
			
			int wid = st.columnInt(1); // bottle id
			if (wineTracker.get(wid)) 
				continue; // skip if the bottle is taken
			wineTracker.set(wid); // mark the bottle as "sold"
			
			writer.write(pid + "\t" + wid +"\n");
			curCnt++;
			
			bottleCnt++;
		}
		st.dispose();
		db.dispose();
		writer.close();
		
		// Write the count to the first line
		RandomAccessFile resultFile = new RandomAccessFile(new File(resultFilename), "rw");
		resultFile.write((bottleCnt + "").getBytes());
		resultFile.close();
		
		System.out.println("# of bottles sold: " + bottleCnt);		
	}

}
 