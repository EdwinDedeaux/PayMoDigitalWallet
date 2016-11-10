import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * Paymo digital-wallet applications main class. This application creates a
 * repository of previous transactions using the file(batch_payments) the checks
 * that repository against transactions in the file(stream_payments) to
 * determine if various levels of a trusted relation exists between the two
 * users in a streamed transaction.
 * 
 */
public class PayMo {

	/**
	 * Main method to parse the paymo transactions input file and write the
	 * results to the three output files which identify which level of
	 * friendship exists between the two users in a transaction.
	 * 
	 * @param args -- files needed for the paymo repository and searches.
	 */
	public static void main(String[] args) {
		try {

			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			BufferedReader sr = new BufferedReader(new FileReader(args[1]));

			TreeMap<Integer, TreeSet<Integer>> transactionMap = new TreeMap<Integer, TreeSet<Integer>>();
			//
			// process the batch_payments file
			//
			processBatchPaymentsFile(transactionMap, br);
			//
			// process the stream_payments file
			//
			processStreamPaymentsFile(transactionMap, sr, args);

			br.close();
			sr.close();

		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * 
	 * Method to read each entry of streamed data and determine if the users have 
	 * a direct or up to 4th degree relationship with each other.
	 * 
	 * @param tm -- repository of all users in the system along with their list of friends
	 * @param br -- access to the stream_payments.csv file.
	 * @param args -- command line input needed for the path to the output files.
	 * 
	 */
	public static void processStreamPaymentsFile(
			TreeMap<Integer, TreeSet<Integer>> tm, BufferedReader br,
			String[] args) {
		System.out.println("Processing the stream_payment.csv file, this will take a while.");

		String strLine = new String();
		StringTokenizer st = null;
		Transaction transaction = new Transaction();

		try {
			PrintWriter pw1 = new PrintWriter(args[2]);
			PrintWriter pw2 = new PrintWriter(args[3]);
			PrintWriter pw3 = new PrintWriter(args[4]);

			while ((strLine = br.readLine()) != null) {
				// break comma separated line into individual fields.
				st = new StringTokenizer(strLine, ",");
				if (processTransaction(transaction, st)) {
					//
					// one/both users doesn't exist!
					//
					if ((!tm.containsKey(transaction.getId1()))
							| (!tm.containsKey(transaction.getId2()))) {
						pw1.println("unverified");
						pw2.println("unverified");
						pw3.println("unverified");
						continue;
					}

					// check for friend relationship
					if (tm.get(transaction.getId1()).contains(
							transaction.getId2())) {
						pw1.println("trusted");
						continue;
					} else
						pw1.println("unverified");

					// check for friend of friend relationship
					if (isWithinNthDegreeOfFriends(2, 2, tm,
							transaction.getId2(), transaction.getId1())) {
						pw2.println("trusted");
						continue;
					} else
						pw2.println("unverified");

					// check for 4th degree friend relationship
					if (isWithinNthDegreeOfFriends(4, 2, tm,
							transaction.getId2(), transaction.getId1()))
						pw3.println("trusted");
					else {
						pw3.println("unverified");
					}

				}
			}
			pw1.close();
			pw2.close();
			pw3.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Method to determine if a particular user is a trusted friend based on varying
	 * level  of separation. (e.g. friend, friend of friend...)
	 * 
	 * @param maxDegree  -- the outer limit of the degree of friend we'll search for.
	 * @param startingDegree -- the inner limit we want to start our search from.
	 * @param tm -- repository of all users in the system along with their list of friends
	 * @param target -- the user we are searching for
	 * @param friends -- list of friend to be resolved with the target
	 * @return  -- true if they are a friend, false otherwise.
	 */
	public static boolean isWithinNthDegreeOfFriends(int maxDegree,
			int startingDegree, TreeMap<Integer, TreeSet<Integer>> tm,
			Integer target, Integer friends) {

		boolean isAFriend = false;
		int currDegree = startingDegree;
		//
		// iterate over the friends of your friends until a match is found
		// or you've reached the end of the set.
		//
		Iterator<Integer> itr = tm.get(friends).iterator();
		while ((itr.hasNext()) & (isAFriend == false)) {
			Integer id = itr.next();
			if (tm.get(id).contains(target)) {
				isAFriend = true;
			} else if (currDegree < maxDegree) {
				if (isWithinNthDegreeOfFriends(maxDegree, ++currDegree, tm,
						target, id))
					isAFriend = true;
				else
					isAFriend = false;
			}
		}
		return isAFriend;
	}

	/**
	 *  Method to iterate through all the batch records in the batch_payments.csv file
	 *  and build the master repository of users and list of friends
	 * 
	 * @param tm -- treemap to contain the repository
	 * @param br -- handle to the batch_payments.csv file.
	 */
	public static void processBatchPaymentsFile(
			TreeMap<Integer, TreeSet<Integer>> tm, BufferedReader br) {

		System.out.println("Processing the batch_payment.csv file.");
		Transaction transaction;
		String strLine = new String();
		StringTokenizer st = null;

		try {

			while ((strLine = br.readLine()) != null) {
				st = new StringTokenizer(strLine, ",");
				transaction = new Transaction();

				if (processTransaction(transaction, st)) {
					updateTransactionMap(tm, transaction);
				}

			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * 
	 * Method for basic verification of each transaction to make sure each record 
	 * contains the correct # of fields.
	 * 
	 * @param transaction -- object containing each field of a transaction (batch/stream)
	 * @param st -- string tokenizer
	 * @return -- true if the transaction was clean, false if any fields were in missing.
	 */
	public static boolean processTransaction(Transaction transaction,
			StringTokenizer st) {
		try {
			transaction.setTime(st.nextToken());
			transaction.setId1(Integer.valueOf(st.nextToken().trim()));
			transaction.setId2(Integer.valueOf(st.nextToken().trim()));
			transaction.setAmount(st.nextToken());
			String msg = st.nextToken();

			// determine if the message itself contains commas, if so
			// set the message to the remainder of the string.
			if (st.hasMoreTokens()) {
				while (st.hasMoreTokens())
					msg += st.nextToken();
			}
			transaction.setMessage(msg);
		} catch (NoSuchElementException nse) {
			// nse.printStackTrace();
			return false;
		} catch (NumberFormatException nfe) {
			// nfe.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Method to connect users with each other in the master repository.
	 * 
	 * @param tm -- user/list of friends repository
	 * @param t -- information to add/update in the repository
	 */
	public static void updateTransactionMap(
			TreeMap<Integer, TreeSet<Integer>> tm, Transaction t) {
		TreeSet<Integer> fs;
		//
		// determine if the user identified by id1 already exists in the master
		// transaction map, if not add them and update their set of friends to
		// include the user identified by id2.
		//
		if (tm.containsKey(t.getId1())) {
			tm.get(t.getId1()).add(t.getId2());
		} else { // new user
			fs = new TreeSet<Integer>();
			fs.add(t.getId2());
			tm.put(t.getId1(), fs);
		}
		//
		// determine if user2 previously existed, if not add them to the map
		// then update their set of friends to include the user identified by
		// id1.
		//
		if (!tm.containsKey(t.getId2())) {
			fs = new TreeSet<Integer>();
			fs.add(t.getId1());
			tm.put(t.getId2(), fs);
		} else
			tm.get(t.getId2()).add(t.getId1());
	}

}