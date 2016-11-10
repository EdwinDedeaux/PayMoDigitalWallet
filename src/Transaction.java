/**
 * Class to store each field of an individual transaction for both the batch and stream files.
 * 
 * @author Edwin
 *
 */
public class Transaction {

	public Transaction() {
	}
	//
	// individual fields of a transaction.
	private String time;
	private Integer id1;
	private Integer id2;
	private String amount;
	private String message;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Integer getId1() {
		return id1;
	}
	public void setId1(Integer id1) {
		this.id1 = id1;
	}
	public Integer getId2() {
		return id2;
	}
	public void setId2(Integer id2) {
		this.id2 = id2;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
