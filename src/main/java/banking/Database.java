package banking;

import java.sql.*;

class Database { // SQLite JDBC
	Connection	con;
	Statement	stmnt;
	ResultSet	rs;

	public Database() {
		con = null;
		stmnt = null;
		rs = null;
	}

	/**
	 * insert into table card
	 * @param number
	 * @param pin
	 * @return
	 */
	protected int insertInto(String number, String pin) {
		try {
			String createAcc = String.format(	"INSERT INTO card (number, pin)\n"
											+	"	VALUES ('%s', '%s');", number, pin);

			stmnt.execute(createAcc);
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 1;
		}
		return 0;
	}

	/**
	 * update column with value
	 * where number = 'number'
	 * @param column
	 * @param value
	 * @return
	 */
	protected int update(String number, String column, String value) {
		try {
			String createAcc = String.format(	"UPDATE card SET\n"
											+	"	%s = %s WHERE number = %s;", column, value, number);

			stmnt.execute(createAcc);
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 1;
		}
		return 0;
	}

	/**
	 * delete row with this number
	 * @param number
	 * @return
	 */
	protected int delete(String number) {
		try {
			String createAcc = String.format(	"DELETE FROM card\n"
											+	"WHERE number = %s;", number);

			stmnt.execute(createAcc);
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 1;
		}
		return 0;
	}

	/**
	 * read balance of card from database
	 * and return it
	 * @param number
	 * @param pin
	 * @return
	 */
	protected int[] balance(String number, String pin) {
		int[] res = new int[]{1, 0};
		try {
			String correct_pin;
			String query = String.format(	"SELECT * FROM card \n"
										+	"WHERE number = %s;", number);
						
			rs = stmnt.executeQuery(query); // select from database table
			//reading from set
			correct_pin = rs.getString("pin");
			if (pin.equals(correct_pin)) { // if pin correct
				res[0] = 0; // return code 0
				res[1] = rs.getInt("balance");
			}
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			res[0] = 1; // error code
		}
		return res;
	}

	/**
	 * check if pin correct for card
	 * @param dbase
	 * @param number
	 * @param pin
	 * @return
	 */
	protected int checkPin(Database dbase, String number, String pin) {
		try {
			String correct_pin;
			String query = String.format(	"SELECT * FROM card \n"
										+	"WHERE number = %s;", number);
						
			rs = stmnt.executeQuery(query); // select from database table
			//reading from set
			correct_pin = rs.getString("pin");
			if (pin.equals(correct_pin)) { // if pin correct
				return 0; // pin correct
			}
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 2; // error
		}
		return 1; // not correct pin
	}

	/**
	 * check new card number for existing in database
	 * @param new_number
	 * @return
	 */
	protected int checkIfNumberExists(String new_number) {
		try {
			String query = String.format(	"SELECT number FROM card \n"
										+	"WHERE number = %s;", new_number);

			rs = stmnt.executeQuery(query); // select from database table
			if (rs.next() == true) // if this cardNumber exist
				return 1; // finded
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 2; // error
		}
		return 0; // not finded
	}

	/**
	 * create connection to database
	 * with sqlite drivers
	 * and create statement
	 * @param database
	 * @param out
	 * @return
	 */
	protected int connect(String database) {
		try {
			String url = "jdbc:sqlite:" + database;
			//create connection to the database
			con = DriverManager.getConnection(url);
			//create statement
			stmnt = con.createStatement();
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\n");
			return 1;
		}
		if (createTableAccounts() == 0) // create table in database
			return 0;
		else
			return 1;
	}

	/**
	 * close realised fields
	 * @return
	 */
	protected int close() {
		try {
			if (con != null)
				con.close();
			if (stmnt != null)
				stmnt.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\n");
		}
		return 0;
	}

	/**
	 * create table if it's not exist
	 */
	protected int createTableAccounts() {
		try {
			String createAcc = "CREATE TABLE IF NOT EXISTS card (\n"
							+  "	id		INTEGER PRIMARY KEY,\n"
							+  "	number	TEXT,\n"
							+  "	pin		TEXT,\n"
							+  "	balance	INTEGER DEFAULT 0\n"
							+  ");";

			stmnt.execute(createAcc);
		} catch (SQLException e) {
			Output.putstr(e.getMessage() + "\ncreateTableAccounts() fails\n");
			return 1;
		}
		return 0;
	}
}