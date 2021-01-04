package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
    public static void main(String[] args) {
        var input = new ClientArgs();
        try {
            JCommander.newBuilder().addObject(input).args(args).build();
        } catch (ParameterException e) {
            System.out.println("Error: incorrect input.\n" + e.getMessage());
            e.usage();
            return;
        }

        new Client().run("localhost", 9889, input.toString());
    }
}