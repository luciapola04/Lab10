package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String FILE_NAME = "config.yml";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";
    private static final String ATTEMPTS = "attempts";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public DrawNumberApp(final DrawNumberView... views) throws FileNotFoundException, IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        final Configuration.Builder config = new Configuration.Builder();
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(ClassLoader.getSystemResourceAsStream(FILE_NAME), StandardCharsets.UTF_8))) {
                String line = r.readLine();
                while (line != null) {
                    final String[] divString = line.split(":");
                    switch (divString[0]) {
                        case MINIMUM -> config.putMin(Integer.parseInt(divString[1].trim()));
                        case MAXIMUM -> config.putMax(Integer.parseInt(divString[1].trim()));
                        case ATTEMPTS -> config.putAttempts(Integer.parseInt(divString[1].trim()));
                        default -> throw new AssertionError();
                    }
                    line = r.readLine();
                }
            } catch (final IOException e) {
                System.out.println(e.getMessage()); //NOPMD
            }
        final Configuration configuration = config.build();
        this.model = new DrawNumberImpl(configuration.getMin(), configuration.getMax(), configuration.getAttempts());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
    }

    /**
     * @param args
     *            ignored
     * @throws IOException
     */
    public static void main(final String... args) throws IOException {
        new DrawNumberApp(new DrawNumberViewImpl(), new PrintStreamView(System.out));
    }
}
