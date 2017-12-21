package tdl.client.runner;


public class TestConsoleOut implements ConsoleOut {
    private String total = "";

    public void println(String s) {
        total += s + "\n";
    }

    @Override
    public void printf(String s, String... strings) {
        total += String.format(s, strings);
    }

    String getTotal() {
        return total;
    }
}
