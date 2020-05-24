
public class App {
    public static void main(String[] args) throws Exception {
        Browser browser = new Browser();
        if (args.length == 1)
            browser.serviceInfo(args[0]);
        else if (args.length == 3)
            new Registrar().register(args[0],args[1],args[2]);
        else System.out.println("1 аргументов ищет сервисы с таким названием и выводит информацию по сервису\n" +
                    "3 аргумента создают новый бизнес из первого параметра, " +
                    "сервис из второго параметра и адрес до сервиса из третьего параметра");
    }

}
