package banking;

import java.util.Scanner;

public class BankSystem {
	Database			dbase;
	Scanner				scan;

	protected BankSystem() {
		dbase = new Database();
		scan = new Scanner(System.in);
	}

	/**
	 * return database string
	 * if args wrong put "usage"
	 * and return 'null'
	 * @param args
	 * @return
	 */
	private String arguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-filename")) { // if find -filename option
				if (args[i + 1] != null) // if arg exist
					return args[i + 1]; // return arg
				break;
			}
		}
		Output.printUsage();
		return null;
	}

	/**
	 * Just loop startSystem()
	 * until he returns !1
	 * @param args
	 */
	public static void main(String[] args) {
		String		database;
		int			log = 1; // 0 is exit, 1 is continue
		BankSystem	bsystem;

		bsystem = new BankSystem();
		database = bsystem.arguments(args);
		if (database == null) // if we doesn't have "-filename file"
			System.exit(1);
		// connect ot datatbase
		if (bsystem.dbase.connect(database) == 1)
			System.exit(2);
		// main loop
		while (log == 1) {
			log = bsystem.startSystem();
		}
		// close connection with database
		if (log == 0) {
			bsystem.dbase.close();
			Output.printBye();
		}
		/**
		 * some errors handling
		 */
	}

	/**
	 * Put menu, read command and do
	 * what user write or put error massege
	 * @return 0 if we want to exit programm
	 * 1 if we don't want
	 */
	private int startSystem() {
		String	cmd = "";

		while (cmd.length() < 1) {
			Output.printMenu();
			
			//read answer
			cmd = scan.next();
			switch (cmd) {
				case "0":
					return 0; // exit the programm
				case "1":
					 // add new account into database
					if (BankCard.createNew(dbase) != 0) {
						Output.putstr("Cannot create new card...\n");
						return 2; // error code
					}
					break;
				case "2":
					//log in
					if (accountLogging() == 0) //can't log in
						return 0; // exit
					break;
				default:
					// try enter number again
					Output.wrongAnswer();
					cmd = "";
			}
		}

		return 1;
	}

	/**
	 * Read card number, pin.
	 * Return 1 if account is not finded
	 * else call insideAccount()
	 * @return 0 to exit program
	 */
	private int accountLogging() {
		//read input
		Output.putstr("\nEnter your card number:\n");
		String	cardNumber = scan.next();
		Output.putstr("Enter your PIN:\n");
		String	pin = scan.next();

		//logging in
		BankCard card = new BankCard(cardNumber);
		card.logIn(dbase, pin);
		if (card.isLoggedIn()) {
			Output.putstr("\nYou have successfully logged in!\n\n");
			//menu of account
			if (insideAccount(card) == 0)
				return 0; // exit from program
		}
		else
			Output.putstr("\nWrong card number or PIN!\n\n");
		return 1; // return to main menu
	}
	/**
	 * Put account menu.
	 * Read command and:
	 * put acnt balance,
	 * log Output or exit program
	 * @param acnt; account that we logged in
	 * @return
	 */
	private int insideAccount(BankCard card) {
		int log = 1;

		while (log == 1) {
			String cmd = "";

			while (cmd.length() < 1) { // while we don't have right answer
				Output.printAccountMenu();
				//read answer
				cmd = scan.next();

				switch (cmd) {
					case "1":
						int[] balance;
						// get balance
						balance = card.getBalance(dbase);
						if (balance[0] == 0)
							Output.putstr("\nBalance: " + balance[1] + "\n\n");
						else
							Output.putstr("Error\n\n");
						break;
					case "2":
						Output.putstr("\nEnter income:\n");
						//read value of money
						String income = scan.next();
						//add money to the card
						if (card.addIncome(dbase, income) == 0)
							Output.putstr("Income was added!\n\n");
						else
							Output.putstr("Error\n\n");
						break;
					case "3":
						// transfer money from this card to another
						if (transfer(card) == 0)
							Output.putstr("Success!\n\n");
						else
							Output.putstr("Error!\n\n");
						break;
					case "4":
						// delete card from database
						if (card.deleteCard(dbase) == 0)
							Output.putstr("\nThe account has been closed!\n\n");
						else
							Output.putstr("Error\n\n");
						return 1; // return to main menu
					case "5":
						Output.putstr("\nYou have successfully logged Output!\n\n");
						return 1; // log out code
					case "0":
						// exit from account
						return 0;
					default:
						// try again, wrong answer
						Output.wrongAnswer();
						cmd = "";
				}
			}
		}
		return 0;
	}

	/**
	 *  transfer money from this card to another
	 * @param card
	 * @return
	 */
	private int transfer(BankCard card) {
		//reading card number
		String tr_number;
		Output.putstr("\nTransfer\nEnter card number:\n");
		tr_number = scan.next();
		//check if checksum is right
		char checksum = tr_number.charAt(tr_number.length() - 1); // last digit
		char LuhnChecksum = (char)(BankCard.LuhnAlgorithmChecksum(tr_number) + '0');

		if (checksum != LuhnChecksum) {// if checksum wrong
			Output.putstr("Probably you made mistake in the card number. Please try again!\n\n");
			return 1; // error
		}
		//check for card existing
		if (dbase.checkIfNumberExists(tr_number) != 1) {
			Output.putstr("Such a card does not exist.\n\n");
			return 1; // error
		}


		//read transfer sum
		Output.putstr("Enter how much money you want to transfer:\n");
		String money = scan.next();

		//check if we have enough money
		int[] res = card.getBalance(dbase);
		if (res[0] == 1)
			return 1; // error! cannot get balance
		int mon;
		try {
			mon = Integer.parseInt(money);
		} catch (NumberFormatException e) {
			return 1; // error! cannot parse money to int
		}
		if (res[1] < mon) {
			Output.putstr("Not enough money!\n\n");
			return 1; // error! don't have enough money
		}

		//transfer
		BankCard tr_card = new BankCard(tr_number);
		if (card.expenditure(dbase, money) != 0)
			return 1; // error! can't take away money
		if (tr_card.addIncome(dbase, money) != 0) {
			if (card.addIncome(dbase, money) != 0) {// return back money
				Output.putstr("Error, can't return money! Info: ...\n");
				System.exit(3); // handle this situation
			}
			return 1; // error! cannot add income
		}
		return 0;
	}
}