package editor;
import java.util.ArrayList;

/**
 * Created by shivmpatel12 on 3/4/16.
 */
public class ArrayLine<Item>  extends ArrayList<Item> {

    ArrayList<LinkedListDeque.Node> arrayLine;
    int MaxLineNum;

    public ArrayLine() {
        arrayLine = new ArrayList<LinkedListDeque.Node>();
        MaxLineNum = 0;
    }


}
