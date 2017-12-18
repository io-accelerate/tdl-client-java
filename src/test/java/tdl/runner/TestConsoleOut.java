package tdl.runner;

import tdl.client.runner.IConsoleOut;


public class TestConsoleOut implements IConsoleOut {
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
