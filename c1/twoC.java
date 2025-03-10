package c1;

public class twoC {
    // Chess grid
    public static void main(String[] args) {
        int c_size = Integer.parseInt(args[1]);
        int c_1 = Integer.parseInt(args[2]);
        int c_2 = Integer.parseInt(args[3]);

        c1.main.chessGrid(500, 500, c_size, c_1, c_2, "c1/chess_grid.jpg");
    }
}
