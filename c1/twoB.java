package c1;

public class twoB {
    // Color grid
    public static void main(String[] args) {
        int l_width = Integer.parseInt(args[1]);
        int d_x = Integer.parseInt(args[2]);
        int d_y = Integer.parseInt(args[3]);
        int c_grid = Integer.parseInt(args[4]);
        int c_bg = Integer.parseInt(args[5]);

        c1.main.colorGrid(500, 500, l_width, d_x, d_y, c_grid, c_bg, "c1/img/color_grid.jpg");
    }


}
