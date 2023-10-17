package states;

public interface State {
    State step();

    default void printTitle(String title) {
        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");

        System.out.println("\t* " + title + " *");

        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");
    }

    default void breakLn() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            for (int i = 0; i < 50; i++) System.out.print("\n");
        }
    }
}
