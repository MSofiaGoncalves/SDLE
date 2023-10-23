package project.client.utils;
import java.util.List;

public class TablePrinter {
    public static void printTable(List<List<String>> data) {
        int numColumns = data.get(0).size();

        int[] columnWidths = new int[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columnWidths[i] = data.get(0).get(i).length();
            for (List<String> row : data) {
                columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
            }
        }

        printHorizontalLine(columnWidths);
        printRow(data.get(0), columnWidths);
        printHorizontalLine(columnWidths);
        for (int i = 1; i < data.size(); i++) {
            printRow(data.get(i), columnWidths);
        }
        printHorizontalLine(columnWidths);
    }

    public static void printRow(List<String> row, int[] columnWidths) {
        System.out.print("| ");
        for (int i = 0; i < row.size(); i++) {
            System.out.printf("%-" + (columnWidths[i]) + "s | ", row.get(i));
        }
        System.out.println();
    }

    public static void printHorizontalLine(int[] columnWidths) {
        System.out.print("+");
        for (int width : columnWidths) {
            for (int i = 0; i < width + 2; i++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }
}
