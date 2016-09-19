package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.util.LinkedList;


import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;



public class Editor extends Application {

	private static int WINDOW_WIDTH = 500;
    private static int WINDOW_HEIGHT = 500;
    private static int TEXT_POSITION_X = 5;
    private static int TEXT_POSITION_Y = 0;
    private static int XRightMargin;

    ScrollBar scrollBar;
    private LinkedListDeque<Text> text = new LinkedListDeque<>();
    private ArrayLine<LinkedListDeque.Node> Lines = new ArrayLine<>();
    private Group Groot;
    private Rectangle cursor;
    private int fontHeight;
    private int MaxLineNum = 0;
    private String font;
    private int FSize;
    private static String fileName;
    private int ScrollBarWidth;
    private UndoRedoStacks stacks;
    private Group childroot;
    private static int LowestWordPos;

    private class KeyEventHandler implements EventHandler<KeyEvent> {

        private static final int STARTING_FONT_SIZE = 20;


        private Text displayText = new Text(TEXT_POSITION_X, TEXT_POSITION_Y, "");
        private int fontSize = STARTING_FONT_SIZE;

        private String fontName = "Verdana";


        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {

            displayText = new Text(TEXT_POSITION_X, TEXT_POSITION_Y, "");

            LowestWordPos = 0;
            childroot = new Group();
            stacks = new UndoRedoStacks();
            FSize = fontSize;
            font = fontName;

            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));

            Groot = root;
            Groot.getChildren().add(childroot);

            cursor = new Rectangle(1, STARTING_FONT_SIZE);
            cursor.setX(TEXT_POSITION_X);
            cursor.setY(TEXT_POSITION_Y);

            Text sampleLetter = new Text("l");
            sampleLetter.setFont(Font.font(fontName, fontSize));
            fontHeight = (int) Math.round(sampleLetter.getLayoutBounds().getHeight());

            root.getChildren().add(displayText);
        }

        @Override
        public void handle(KeyEvent keyEvent) {

            if (keyEvent.isShortcutDown()) {

                if (keyEvent.getCode() == KeyCode.P) {

                } else if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {

                    FSize += 4;
                    Text sampleLetter = new Text("l");
                    sampleLetter.setFont(Font.font(font, FSize));
                    fontHeight = (int) Math.round(sampleLetter.getLayoutBounds().getHeight());
                    Render();
                    cursor.setHeight(FSize);
                    updateCursor(text.currentNode.item.getX() +
                            text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());

                } else if (keyEvent.getCode() == KeyCode.MINUS) {

                    if (FSize > 4) {

                        FSize -= 4;
                        Text sampleLetter = new Text("l");
                        sampleLetter.setFont(Font.font(font, FSize));
                        fontHeight = (int) Math.round(sampleLetter.getLayoutBounds().getHeight());
                        Render();
                        cursor.setHeight(FSize);
                        updateCursor(text.currentNode.item.getX() +
                                text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());

                    }
                } else if (keyEvent.getCode() == KeyCode.S) {

                    try {

                        LinkedListDeque.Node temp = text.sentinel.next;
                        FileWriter writer = new FileWriter(fileName);
                        while (temp != text.sentinel && temp != null) {
                            char CharRead = temp.item.getText().charAt(0);
                            writer.write(CharRead);
                            temp = temp.next;
                        }
                        writer.close();

                    } catch (IOException ioException) {
                        System.out.println("Error when copying; exception was: " + ioException);
                    }
                } else if (keyEvent.getCode() == KeyCode.Z) {
                    Undo();
                } else if (keyEvent.getCode() == KeyCode.Y) {
                    Redo();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {

                String characterTyped = keyEvent.getCharacter();
                Text letter = new Text(TEXT_POSITION_X, TEXT_POSITION_Y, characterTyped);

                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {

                    if (characterTyped.equals("\r")) {

                        stacks.AddUndo(text.currentNode);
                        Text enter = new Text(TEXT_POSITION_X, TEXT_POSITION_Y, "\r");
                        text.addCurrent(enter);
                        TEXT_POSITION_X = 5;

                        updateCursor(TEXT_POSITION_X, cursor.getY() + fontHeight);
                        Render();
                        stacks.NewRedo();

                    } else {

                        text.addCurrent(letter);
                        text.currentNode.operation = 1;
                        stacks.AddUndo(text.currentNode);
                        letter.setTextOrigin(VPos.TOP);
                        letter.setFont(Font.font(fontName, fontSize));
                        childroot.getChildren().add(letter);
                        Render();
                        updateCursor(text.currentNode.item.getX() +
                                letter.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                        stacks.NewRedo();

                    }
                }
            } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                if (text.currentNode != text.sentinel) {
                    text.currentNode.operation = -1;
                    stacks.AddUndo(text.currentNode);
                    Text last = text.removeCurrent();
                    Render();
                    updateCursor(text.currentNode.item.getX() +
                            text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                    childroot.getChildren().remove(last);
                    stacks.NewRedo();
                }
            } else if (keyEvent.getCode() == KeyCode.RIGHT) {

                double temp = cursor.getY();
                text.moveRight();

                if (cursor.getY() != temp) {
                    updateCursor(text.currentNode.item.getX(), text.currentNode.item.getY());
                } else {
                    updateCursor(text.currentNode.item.getX() +
                            text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                }

            } else if (keyEvent.getCode() == KeyCode.LEFT) {

                text.moveLeft();
                updateCursor(text.currentNode.item.getX() +
                        text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());

            } else if (keyEvent.getCode() == KeyCode.UP) {
                if (cursor.getY() > 0) {
                    int Target = (int) Math.round(cursor.getY() / fontHeight) - 1;
                    text.currentNode = Lines.get(Target);
                    double TargetHeight = text.currentNode.item.getY();
                    int CountWidth = 5;
                    if (cursor.getX() == 5) {
                        updateCursor(5, Target * fontHeight);
                        text.currentNode = text.currentNode.prev;
                    } else if (text.currentNode.item.getText().equals("\r")) {
                        text.currentNode = text.currentNode.prev;
                        updateCursor(5, TargetHeight - fontHeight);
                    } else {

                        while (CountWidth < (int) Math.round(cursor.getX())
                                && text.currentNode.next.item.getY() == TargetHeight) {
                            CountWidth += text.currentNode.item.getLayoutBounds().getWidth();
                            text.currentNode = text.currentNode.next;
                        }

                        if (text.currentNode.next.item.getX() < text.currentNode.item.getX()) {

                            if (text.currentNode.item.getX() +
                                    (text.currentNode.item.getLayoutBounds().getWidth() / 2) > cursor.getX()) {
                                text.moveLeft();
                            }
                            updateCursor(text.currentNode.item.getX() +
                                    text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                        } else {

                            FindClosest(cursor.getX());
                            text.currentNode = text.currentNode.prev;
                            updateCursor(text.currentNode.item.getX() +
                                    text.currentNode.item.getLayoutBounds().getWidth(),
                                    text.currentNode.item.getY());
                        }
                    }
                }
            } else if (keyEvent.getCode() == KeyCode.DOWN) {

                if (cursor.getY() / fontHeight < Lines.size() - 1) {
                    int Target = (int) Math.round(cursor.getY() / fontHeight) + 1;
                    text.currentNode = Lines.get(Target);
                    double TargetHeight = text.currentNode.item.getY();
                    int CountWidth = 5;

                    if (cursor.getX() == 5) {
                        updateCursor(5, Target * fontHeight);
                        text.currentNode = text.currentNode.prev;
                    } else if (text.currentNode.item.getText().equals("\r")) {
                        text.currentNode = text.currentNode.prev;
                        updateCursor(5, TargetHeight - fontHeight);
                    } else {

                        while (CountWidth < (int) Math.round(cursor.getX())
                                && text.currentNode.next.item.getY() == TargetHeight) {
                            CountWidth += text.currentNode.item.getLayoutBounds().getWidth();
                            text.currentNode = text.currentNode.next;
                        }
                        if (text.currentNode.next.item.getX() < text.currentNode.item.getX()) {

                            if (text.currentNode.item.getX() +
                                    (text.currentNode.item.getLayoutBounds().getWidth() / 2) > cursor.getX()) {
                                text.moveLeft();
                            }
                            updateCursor(text.currentNode.item.getX() +
                                    text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                        } else {
                            FindClosest(cursor.getX());
                            text.currentNode = text.currentNode.prev;
                            updateCursor(text.currentNode.item.getX() +
                                    text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                        }

                    }
                }
            }
        }
    }

    private void updateCursor(double X, double Y) {
        cursor.setX(X);
        cursor.setY(Y);
        updateWindow();
    }

    private void Undo() {
        if (stacks.UndoStack.peek() != null) {
            LinkedListDeque.Node Undid = stacks.RemoveUndo();
            stacks.AddRedo(Undid);
            if (Undid != null) {
                if (Undid.operation < 0) {
                    Undid.prev.next = Undid;
                    Undid.next.prev = Undid;
                    text.size++;
                    childroot.getChildren().add(Undid.item);
                    Render();
                    text.moveRight();
                    updateCursor(Undid.item.getX() + Undid.item.getLayoutBounds().getWidth(), Undid.item.getY());
                } else if (Undid.operation > 0) {
                    Undid.prev.next = Undid.next;
                    Undid.next.prev = Undid.prev;
                    text.size--;
                    childroot.getChildren().remove(Undid.item);
                    Render();
                    updateCursor(Undid.item.getX(), Undid.item.getY());
                    text.moveLeft();
                }
            }
        }
    }

    private void Redo() {
        if (stacks.RedoStack.peek() != null) {
            LinkedListDeque.Node Redid = stacks.RemoveRedo();
            stacks.AddUndo(Redid);
            if (Redid != null) {
                if (Redid.operation > 0) {
                    Redid.prev.next = Redid;
                    Redid.next.prev = Redid;
                    text.size++;
                    childroot.getChildren().add(Redid.item);
                    Render();
                    text.moveRight();
                    updateCursor(Redid.item.getX() + Redid.item.getLayoutBounds().getWidth(), Redid.item.getY());
                } else if (Redid.operation < 0) {
                    Redid.prev.next = Redid.next;
                    Redid.next.prev = Redid.prev;
                    text.size--;
                    childroot.getChildren().remove(Redid.item);
                    Render();
                    updateCursor(Redid.item.getX(), Redid.item.getY());
                    text.moveLeft();
                }
            }
        }
    }

    private void Render() {
        LowestWordPos = 0;
        MaxLineNum = 0;
        Lines = new ArrayLine<>();
        LinkedListDeque.Node CurrentNode = text.sentinel.next;
        Lines.add(CurrentNode);
        int TEMP_TEXT_POSITION_X = 5;
        int TEMP_TEXT_POSITION_Y = 0;
        LinkedListDeque.Node LastWord;
        int WordLength = (int) Math.round(CurrentNode.item.getLayoutBounds().getWidth());
        LastWord = CurrentNode;
        while (CurrentNode != text.sentinel) {
            Text letter = CurrentNode.item;
            WordLength += Math.round(letter.getLayoutBounds().getWidth());
            letter.setFont(Font.font(font, FSize));
            letter.setTextOrigin(VPos.TOP);
            if (letter.getText().equals(" ")) {
                LastWord = CurrentNode;
                WordLength = 0;
            }
            if (letter.getText().equals("\r")) {
                letter.setX(TEMP_TEXT_POSITION_X);
                letter.setY(TEMP_TEXT_POSITION_Y);
                TEMP_TEXT_POSITION_X = 5;
                TEMP_TEXT_POSITION_Y += fontHeight;
                WordLength = 0;
                Lines.add(CurrentNode.next);
                MaxLineNum++;
            }
            if (TEMP_TEXT_POSITION_X + Math.round(letter.getLayoutBounds().getWidth()) > XRightMargin) {
                TEMP_TEXT_POSITION_X = 5;
                TEMP_TEXT_POSITION_Y += fontHeight;
                if (WordLength + 5 < XRightMargin) {
                    if (LastWord != CurrentNode) {
                        LastWord = LastWord.next;
                        Lines.add(LastWord);
                        MaxLineNum++;
                        while (LastWord.item != letter) {
                            LastWord.item.setX(TEMP_TEXT_POSITION_X);
                            LastWord.item.setY(TEMP_TEXT_POSITION_Y);
                            TEMP_TEXT_POSITION_X += Math.round(LastWord.item.getLayoutBounds().getWidth());
                            LastWord = LastWord.next;
                        }
                    }

                } else {
                    Lines.add(CurrentNode);
                    MaxLineNum++;
                }
            }
                letter.setX(TEMP_TEXT_POSITION_X);
                letter.setY(TEMP_TEXT_POSITION_Y);
                TEMP_TEXT_POSITION_X += Math.round(letter.getLayoutBounds().getWidth());


            CurrentNode = CurrentNode.next;
        }
        LowestWordPos = (int) Math.round(CurrentNode.prev.item.getY());
        updateScrollBarLength();
    }


    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors = {Color.BLACK, Color.WHITE};

        RectangleBlinkEventHandler() {
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    private void updateWindow() {
        if (WINDOW_HEIGHT < childroot.getLayoutY() + cursor.getY() + cursor.getHeight()) {
            childroot.setLayoutY(WINDOW_HEIGHT - cursor.getHeight() - cursor.getY());
        } else if (cursor.getY() + childroot.getLayoutY() < 0) {
            childroot.setLayoutY(cursor.getY() * -1);
        }
    }

    public void makeRectangleColorChange() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {

            double mousePressedX = mouseEvent.getX();
            int mousePressedY = (int) mouseEvent.getY();
            int TargetY = (int) Math.round((mousePressedY / fontHeight) - childroot.getLayoutY());
            if (Lines.size() != 0) {
                if (TargetY > MaxLineNum) {
                    text.currentNode = Lines.get(MaxLineNum);
                } else {
                    text.currentNode = Lines.get(TargetY);
                }
                if (mousePressedX <= 5) {
                    updateCursor(5, TargetY * fontHeight);
                    text.currentNode = text.currentNode.prev;
                } else {
                    int CountWidth = 5;
                    double TargetHeight = text.currentNode.item.getY();
                    while (CountWidth < mousePressedX
                            && text.currentNode.next.item.getY() == TargetHeight
                            && text.currentNode.next.item.getX() > text.currentNode.item.getX()) {
                        CountWidth += text.currentNode.item.getLayoutBounds().getWidth();
                        text.currentNode = text.currentNode.next;
                    }
                    if (text.currentNode.next.item.getX() < text.currentNode.item.getX()) {
                        if (text.currentNode.item.getX() +
                                (text.currentNode.item.getLayoutBounds().getWidth() / 2) > mousePressedX) {
                            text.moveLeft();
                        }
                        updateCursor(text.currentNode.item.getX() +
                                text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                    } else {
                        FindClosest(mousePressedX);
                        text.currentNode = text.currentNode.prev;
                        updateCursor(text.currentNode.item.getX() +
                                text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                    }
                }
            }
        }
    }

    public void FindClosest(double CursorLoc) {
        double LeftBound = text.currentNode.prev.item.getX();
        double RightBound = text.currentNode.item.getX();
        if (Math.abs(CursorLoc - LeftBound) < Math.abs(CursorLoc - RightBound)) {
            text.currentNode = text.currentNode.prev;
        }
    }

    public void Open(String f) {
        try {
            File inputFile = new File(f);
            if (!inputFile.exists()) {
                System.out.println("Unable to open because file with name " + f
                        + " does not exist");
                return;
            }
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);


            int intRead = -1;

            while ((intRead = bufferedReader.read()) != -1) {
                char charRead = (char) intRead;
                Text letter = new Text(0, 0, String.valueOf(charRead));
                text.addLast(letter);
                childroot.getChildren().add(letter);
            }
            if (text.size() != 0) {
                Render();
            }

            bufferedReader.close();
        } catch (IOException ioException) {
            System.out.println("Error when opening; exception was: " + ioException);
        }
    }

    public void updateScrollBarLength() {
        if (LowestWordPos - WINDOW_HEIGHT > 0) {
            scrollBar.setMax(LowestWordPos);
        } else {
            scrollBar.setMax(0);
        }
    }

    @Override
    public void start(Stage primaryStage) {

    	Group root = new Group();
    	Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
    	EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        primaryStage.setScene(scene);
        primaryStage.show();
        childroot.getChildren().add(cursor);
        makeRectangleColorChange();
        scene.setOnMouseClicked(new MouseClickEventHandler());
        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        scrollBar.setMin(0);
        scrollBar.setMax(0);
        ScrollBarWidth = (int) Math.round(scrollBar.getLayoutBounds().getWidth());
        root.getChildren().add(scrollBar);
        double usableScreenWidth = WINDOW_WIDTH - ScrollBarWidth;
        scrollBar.setLayoutX(usableScreenWidth);
        Group textRoot = new Group();
        root.getChildren().add(textRoot);
        XRightMargin = WINDOW_WIDTH - 5 - ScrollBarWidth;

        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observableValue,
                                Number oldValue, Number newValue) {
                childroot.setLayoutY(-newValue.doubleValue());
            }
        });

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue,
                                          Number oldSceneWidth, Number newSceneWidth) {
                WINDOW_WIDTH = newSceneWidth.intValue();
                XRightMargin = WINDOW_WIDTH - 5 - (int) Math.round(scrollBar.getLayoutBounds().getWidth());
                double usableScreenWidth = WINDOW_WIDTH - ScrollBarWidth;
                scrollBar.setLayoutX(usableScreenWidth);
                if (text.size() > 0) {
                    Render();
                    updateCursor(text.currentNode.item.getX() +
                            text.currentNode.item.getLayoutBounds().getWidth(), text.currentNode.item.getY());
                }
            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue,
                                          Number oldSceneHeight, Number newSceneHeight) {
                WINDOW_HEIGHT = newSceneHeight.intValue();
            }
        });

        Open(fileName);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Expected usage: CopyFile <source filename> <optional: debug>");
            System.exit(1);
        }
        fileName = args[0];
        if (args.length == 2) {
            String secondArg = args[1];
            if (secondArg.equals("debug")) {
                System.out.println("debugging");
            }
        }
        launch(args);
    }
}