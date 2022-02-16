import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;

public class Client {
	static Scanner in = new Scanner(System.in);
	static String addr, login, password;
	static Boolean croupier;
	static Requests requests;
	static Response r;
	
	public static void main(String[] args) {
		readUserData();
		
		requests = new Requests(addr);
		
		r = requests.make(Requests.POST, "login", Arrays.asList(
				new BasicNameValuePair("login", login),
				new BasicNameValuePair("password", password),
				new BasicNameValuePair("is_croupier", String.valueOf(croupier))
				));
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		switch (r.status_code) {
			case (200) -> System.out.println("Авторизация пройдена");
			case (401) -> {
				System.out.println("Авторизация не пройдена");
				System.exit(1);
			}
			case (441) -> {
				System.out.println("Ошибка: пользователь под таким логином уже авторизован");
				System.exit(1);
			}
			case (435) -> {
				System.out.println("Ошибка: крупье уже авторизован");
				System.exit(1);
			}
			default -> {
				System.out.println("Ошибка сервера");
				System.exit(1);
			}
		}
		
		System.out.print("\nКоманды: exit, status, results, bets, ");
		if (croupier) {
			System.out.println("spin");
		} else {
			System.out.println("new <ставка> <тип>");
			System.out.println("Виды ставок: -2 (четное), -1 (нечетное), 0-36");
		}
		
		boolean exit = false;
		while (!exit) {
			String[] cmd = readCommand();
			if (cmd == null) {
				System.out.println("Некорректная команда");
				continue;
			}
			switch (cmd[0]) {
				case ("exit"):
					requests.make(Requests.GET, "logout", null);
					requests.close();
					in.close();
					exit = true;
					break;
				case ("status"):
					getStatus();
					break;
				case ("results"):
					getResults();
					break;
				case ("bets"):
					getBets();
					break;
				case ("new"):
					if (!croupier) {
						newBet(cmd[1], cmd[2]);
					} else {
						System.out.println("Ошибка: вы крупье, ваши ставки не принимаются");
					}
					break;
				case ("spin"):
					if (croupier) {
						spin();
					} else {
						System.out.println("Ошибка: вы не крупье");
					}
					break;
			}
		}
	}
	
	private static void readUserData() {
		System.out.print("Сервер (по умолчанию http://127.0.0.1:5000/): ");
		addr = in.nextLine();
		if (addr.length() == 0) addr = "http://127.0.0.1:5000/";
		
		System.out.print("Логин: ");
		login = in.nextLine();
		
		System.out.print("Пароль: ");
		password = in.nextLine();
		
		System.out.print("Войти как крупье? (y/n): ");
		croupier = in.nextLine().equals("y");
	}
	
	private static String[] readCommand() {
		System.out.print("\n>> ");
		String cmd = in.nextLine().strip();
		
		if (Pattern.compile("^(exit|status|results|bets|spin)$").matcher(cmd).matches())
			return new String[]{cmd};
		
		Matcher m = Pattern
				.compile("^new(\\s)+(?<amount>[0-9]+)(\\s)+(?<type>-[12]|[0-9]|[1-2][0-9]|3[0-6])$")
				.matcher(cmd);
		if (m.find())
			return new String[]{"new", m.group("amount"), m.group("type")};
		
		return null;
	}
	
	private static void getStatus() {
		r = requests.make(Requests.GET, "status", null);
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		System.out.print("Пользователь: " + r.getString("username") + "; ");
		if (r.getBoolean("is_croupier")) {
			System.out.println("крупье");
		} else {
			System.out.print("на счету: ");
			System.out.println(r.getInt("money"));
		}
	}
	
	private static void getResults() {
		r = requests.make(Requests.GET, "results", null);
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		switch (r.status_code) {
			case (200) -> {
				System.out.println("Результат розыгрыша: " + r.getInt("number"));
				for (int i = 0; i < r.getArray("usernames").length(); i++) {
					String type = switch (r.getArray("types").getInt(i)) {
						case (-2) -> "четное";
						case (-1) -> "нечетное";
						default -> String.valueOf(r.getArray("types").getInt(i));
					};
					System.out.println("Игрок: " + r.getArray("usernames").getString(i) +
							", Ставка: " + r.getArray("amounts").getInt(i) +
							", Тип ставки: " + type + " -> Итог: " + r.getArray("results").getString(i));
				}
			}
			case (204) -> System.out.println("Пока нет результатов");
			default -> System.out.println("Ошибка сервера");
		}
	}
	
	private static void getBets() {
		r = requests.make(Requests.GET, "bets", null);
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		switch (r.status_code) {
			case (200):
				for (int i = 0; i < r.getArray("usernames").length(); i++) {
					String type = switch (r.getArray("types").getInt(i)) {
						case (-2) -> "четное";
						case (-1) -> "нечетное";
						default -> String.valueOf(r.getArray("types").getInt(i));
					};
					System.out.println("Игрок: " + r.getArray("usernames").getString(i) +
							", Ставка: " + r.getArray("amounts").getInt(i) + ", Тип ставки: " + type);
				}
				break;
			case (204):
				System.out.println("Пока нет ставок");
				break;
			default:
				System.out.println("Ошибка сервера");
		}
	}
	
	private static void newBet(String amount, String type) {
		r = requests.make(Requests.POST, "new", Arrays.asList(
				new BasicNameValuePair("amount", amount),
				new BasicNameValuePair("type", type)
				));
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		switch (r.status_code) {
			case (200) -> System.out.println("Ставка принята");
			case (444) -> System.out.println("Ошибка: некорректная ставка");
			case (445) -> System.out.println("Ошибка: для указанной ставки недостаточно денег");
			default -> System.out.println("Ошибка сервера");
		}
	}
	
	private static void spin() {
		r = requests.make(Requests.GET, "spin", null);
		if (r == null) {
			System.out.println("Потеряна связь с сервером");
			System.exit(1);
		}
		if (r.status_code == 200) {
			System.out.println("Результат розыгрыша: " + r.content);
		} else {
			System.out.println("Ошибка сервера");
		}
	}
}
