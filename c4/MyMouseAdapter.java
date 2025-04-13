package c4;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import c4.Editor.PosterElement;

public class MyMouseAdapter extends MouseAdapter {
    private List<PosterElement> elements = new ArrayList<>();
    private PosterElement selectedElement;
    private final Runnable repaintCallback;
    private boolean moving = false;

    MyMouseAdapter(List<PosterElement> elements, Runnable repaintCallback) {
        this.elements = elements;
        this.repaintCallback = repaintCallback;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (selectedElement != null) {
            // Check if in bounds of selected
            if (selectedElement.trySelect(x, y)) {
                moving = true;
                System.out.println("x: " + x + ", y: " + y + ", moving: " + moving + ", selectedElement: " + selectedElement);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (moving) {
            selectedElement.move(x, y);
            System.out.println(
                    "Moving " + selectedElement + " to " + x + ", " + y
            );
            repaintCallback.run();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // If element was moved, do not unselect
        if (moving) {
            moving = false;
        } else {
            if (e.getButton() == MouseEvent.BUTTON1) { // left click
                // Unselect existing
                if (selectedElement != null) {
                    selectedElement.setSelected(false, null);
                    selectedElement = null;
                    repaintCallback.run();
                }

                // Select element
                for (PosterElement pe : elements.reversed()) { // reverse because we want to select element that is closer to front
                    if (pe.trySelect(x, y)) {
                        selectedElement = pe;
                        pe.setSelected(true, elements);
                        repaintCallback.run();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) { // right click
            // Remove selected element
            if (selectedElement != null) {
                elements.remove(selectedElement);
                selectedElement = null;
                repaintCallback.run();
            }
        }
    }
}

