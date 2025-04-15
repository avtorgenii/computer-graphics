package c4;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import c4.Editor.PosterElement;

public class MyMouseAdapter extends MouseAdapter {
    private final List<PosterElement> elements;
    private PosterElement selectedElement;
    private final Runnable repaintCallback;
    private boolean moving = false;
    private boolean scaling = false;
    private Point lastMousePosition;

    MyMouseAdapter(List<PosterElement> elements, Runnable repaintCallback) {
        this.elements = elements;
        this.repaintCallback = repaintCallback;
        this.lastMousePosition = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (selectedElement != null) {
            // Check if in bounds of selected
            if (selectedElement.trySelect(x, y)) {
                if (selectedElement.nearVertex(x, y)) {
                    scaling = true;
                } else {
                    moving = true;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (lastMousePosition != null && selectedElement != null) {
            int deltaX = x - lastMousePosition.x;
            int deltaY = y - lastMousePosition.y;

            if (moving) {
                selectedElement.move(deltaX, deltaY);
//                System.out.println(selectedElement.posX + " " + selectedElement.posY);
            } else if (scaling) {
                selectedElement.scale(deltaX, deltaY, e.getPoint());
            }

            lastMousePosition.setLocation(x, y);
            repaintCallback.run();
        } else if (lastMousePosition == null) {
            lastMousePosition = new Point(x, y);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notch = e.getWheelRotation();

        if (selectedElement != null) {
            selectedElement.rotate(notch, false);
            repaintCallback.run();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        lastMousePosition = null;

        // If element was moved, do not unselect
        if (moving) {
            moving = false;
        } else if (scaling) {
            scaling = false;
        }
        else {
            if (e.getButton() == MouseEvent.BUTTON1) { // left click
                // Unselect existing
                if (selectedElement != null) {
                    selectedElement.setSelected(false, null);
                    selectedElement = null;
                    repaintCallback.run();
                }

                // Select element
                for (PosterElement pe : elements.reversed()) { // reverse because we want to select element that is closer to front
                    if (pe.trySelect(x, y) || pe.nearVertex(x, y)) {
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

